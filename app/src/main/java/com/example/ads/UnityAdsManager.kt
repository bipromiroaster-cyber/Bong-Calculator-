package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsLoadOptions
import com.unity3d.ads.UnityAdsShowOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

enum class UnityInitState {
    NOT_CONFIGURED,
    INITIALIZING,
    INITIALIZED,
    FAILED
}

/**
 * Lifecycle-safe manager for Unity Ads initialization, interstitial ads (natural breaks only),
 * and optional rewarded ads. Holds no Activity references across lifecycles.
 */
object UnityAdsManager {
    private const val TAG = "UnityAdsManager"

    private val initRequested = AtomicBoolean(false)

    private val _initState = MutableStateFlow(
        if (UnityAdsConfig.isGameIdConfigured()) UnityInitState.INITIALIZING else UnityInitState.NOT_CONFIGURED
    )
    val initState: StateFlow<UnityInitState> = _initState.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady.asStateFlow()

    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    private val _bannersPausedForSession = MutableStateFlow(false)
    val bannersPausedForSession: StateFlow<Boolean> = _bannersPausedForSession.asStateFlow()

    @Volatile
    private var completedCalculationsSinceAd: Int = 0

    @Volatile
    private var lastInterstitialShownElapsedMs: Long = 0L

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        if (!UnityAdsConfig.isGameIdConfigured()) {
            _initState.value = UnityInitState.NOT_CONFIGURED
            Log.i(TAG, "Unity Ads Game ID is still a placeholder; skipping SDK initialization.")
            return
        }
        if (UnityAds.isInitialized) {
            _initState.value = UnityInitState.INITIALIZED
            preloadAdsIfConfigured()
            return
        }
        if (!initRequested.compareAndSet(false, true)) {
            return
        }

        _initState.value = UnityInitState.INITIALIZING
        UnityAds.initialize(
            appContext,
            UnityAdsConfig.UNITY_GAME_ID,
            UnityAdsConfig.TEST_MODE,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads initialization complete (testMode=${UnityAdsConfig.TEST_MODE})")
                    _initState.value = UnityInitState.INITIALIZED
                    preloadAdsIfConfigured()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Ads initialization failed: $error - $message")
                    _initState.value = UnityInitState.FAILED
                    initRequested.set(false)
                }
            }
        )
    }

    private fun preloadAdsIfConfigured() {
        if (UnityAdsConfig.isInterstitialConfigured()) {
            loadInterstitial()
        }
        if (UnityAdsConfig.isRewardedConfigured()) {
            loadRewarded()
        }
    }

    fun recordCompletedCalculation() {
        completedCalculationsSinceAd += 1
    }

    fun loadInterstitial() {
        if (!UnityAds.isInitialized || !UnityAdsConfig.isInterstitialConfigured()) return
        UnityAds.load(
            UnityAdsConfig.INTERSTITIAL_AD_UNIT_ID,
            UnityAdsLoadOptions(),
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String?) {
                    Log.d(TAG, "Interstitial loaded: $placementId")
                    _isInterstitialReady.value = true
                }

                override fun onUnityAdsFailedToLoad(
                    placementId: String?,
                    error: UnityAds.UnityAdsLoadError?,
                    message: String?
                ) {
                    Log.w(TAG, "Interstitial failed to load ($placementId): $error - $message")
                    _isInterstitialReady.value = false
                }
            }
        )
    }

    /**
     * Shows an interstitial ad only at natural navigation breaks (e.g., closing History or Settings),
     * subject to minimum completed calculations and cooldown pacing. Never invoked on keypad presses.
     */
    fun maybeShowInterstitialAtNaturalBreak(activity: Activity?): Boolean {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return false
        if (_bannersPausedForSession.value) return false
        if (!UnityAds.isInitialized || !UnityAdsConfig.isInterstitialConfigured()) return false
        if (!_isInterstitialReady.value) {
            loadInterstitial()
            return false
        }

        val now = SystemClock.elapsedRealtime()
        val elapsedSinceLast = now - lastInterstitialShownElapsedMs
        val cooldownSatisfied = lastInterstitialShownElapsedMs == 0L ||
            elapsedSinceLast >= UnityAdsConfig.INTERSTITIAL_COOLDOWN_MS
        val enoughCalculations = completedCalculationsSinceAd >= UnityAdsConfig.MIN_CALCULATIONS_BEFORE_INTERSTITIAL

        if (!cooldownSatisfied || !enoughCalculations) {
            return false
        }

        _isInterstitialReady.value = false
        UnityAds.show(
            activity,
            UnityAdsConfig.INTERSTITIAL_AD_UNIT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    Log.w(TAG, "Interstitial show failed ($placementId): $error - $message")
                    loadInterstitial()
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    Log.d(TAG, "Interstitial show started: $placementId")
                    lastInterstitialShownElapsedMs = SystemClock.elapsedRealtime()
                    completedCalculationsSinceAd = 0
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    Log.d(TAG, "Interstitial clicked: $placementId")
                }

                override fun onUnityAdsShowComplete(
                    placementId: String?,
                    state: UnityAds.UnityAdsShowCompletionState?
                ) {
                    Log.d(TAG, "Interstitial completed ($placementId): $state")
                    loadInterstitial()
                }
            }
        )
        return true
    }

    fun loadRewarded() {
        if (!UnityAds.isInitialized || !UnityAdsConfig.isRewardedConfigured()) return
        UnityAds.load(
            UnityAdsConfig.REWARDED_AD_UNIT_ID,
            UnityAdsLoadOptions(),
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String?) {
                    Log.d(TAG, "Rewarded ad loaded: $placementId")
                    _isRewardedReady.value = true
                }

                override fun onUnityAdsFailedToLoad(
                    placementId: String?,
                    error: UnityAds.UnityAdsLoadError?,
                    message: String?
                ) {
                    Log.w(TAG, "Rewarded ad failed to load ($placementId): $error - $message")
                    _isRewardedReady.value = false
                }
            }
        )
    }

    /**
     * Shows an optional rewarded video ad and invokes [onRewardEarned] only when
     * UnityAdsShowCompletionState.COMPLETED is received.
     */
    fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onNotReadyOrFailed: (String) -> Unit
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onNotReadyOrFailed("Activity unavailable")
            return
        }
        if (!UnityAdsConfig.isRewardedConfigured()) {
            onNotReadyOrFailed("Unity Rewarded Ad Unit ID is not configured yet.")
            return
        }
        if (!UnityAds.isInitialized || !_isRewardedReady.value) {
            loadRewarded()
            onNotReadyOrFailed("Rewarded ad is still loading. Please try again in a moment.")
            return
        }

        _isRewardedReady.value = false
        UnityAds.show(
            activity,
            UnityAdsConfig.REWARDED_AD_UNIT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    Log.w(TAG, "Rewarded show failed ($placementId): $error - $message")
                    onNotReadyOrFailed(message ?: "Unable to display rewarded ad.")
                    loadRewarded()
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    Log.d(TAG, "Rewarded ad started: $placementId")
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    Log.d(TAG, "Rewarded ad clicked: $placementId")
                }

                override fun onUnityAdsShowComplete(
                    placementId: String?,
                    state: UnityAds.UnityAdsShowCompletionState?
                ) {
                    Log.d(TAG, "Rewarded ad complete ($placementId): $state")
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        _bannersPausedForSession.value = true
                        onRewardEarned()
                    }
                    loadRewarded()
                }
            }
        )
    }
}

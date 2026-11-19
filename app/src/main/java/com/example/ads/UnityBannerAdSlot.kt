package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.LocalBongColors
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

private const val BANNER_TAG = "UnityBannerAdSlot"

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Lifecycle-safe native Unity Ads Banner container fixed at 50.dp height below the keypad,
 * ensuring it never overlaps calculator buttons or obstructs calculations.
 */
@Composable
fun UnityBannerAdSlot(
    modifier: Modifier = Modifier
) {
    val bongColors = LocalBongColors.current
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val initState by UnityAdsManager.initState.collectAsStateWithLifecycle()
    val bannersPaused by UnityAdsManager.bannersPausedForSession.collectAsStateWithLifecycle()

    if (bannersPaused) {
        return
    }

    val shape = RoundedCornerShape(14.dp)
    val shouldLoadBanner = activity != null &&
        UnityAdsConfig.isBannerConfigured() &&
        initState == UnityInitState.INITIALIZED

    var isBannerLoaded by remember { mutableStateOf(false) }

    val bannerView = remember(activity, shouldLoadBanner) {
        if (shouldLoadBanner && activity != null) {
            BannerView(
                activity,
                UnityAdsConfig.BANNER_AD_UNIT_ID,
                UnityBannerSize(320, 50)
            ).apply {
                listener = object : BannerView.IListener {
                    override fun onBannerLoaded(bannerAdView: BannerView?) {
                        Log.d(BANNER_TAG, "Banner loaded: ${UnityAdsConfig.BANNER_AD_UNIT_ID}")
                        isBannerLoaded = true
                    }

                    override fun onBannerShown(bannerAdView: BannerView?) {
                        Log.d(BANNER_TAG, "Banner shown")
                    }

                    override fun onBannerClick(bannerAdView: BannerView?) {
                        Log.d(BANNER_TAG, "Banner clicked")
                    }

                    override fun onBannerFailedToLoad(
                        bannerAdView: BannerView?,
                        errorInfo: BannerErrorInfo?
                    ) {
                        Log.w(BANNER_TAG, "Banner failed to load: ${errorInfo?.errorMessage}")
                        isBannerLoaded = false
                    }

                    override fun onBannerLeftApplication(bannerAdView: BannerView?) {
                        Log.d(BANNER_TAG, "Banner left application")
                    }
                }
                load()
            }
        } else {
            null
        }
    }

    DisposableEffect(lifecycleOwner, bannerView) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                bannerView?.destroy()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bannerView?.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(bongColors.keySurface.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = bongColors.glassBorderSubtle,
                shape = shape
            )
            .padding(horizontal = 8.dp)
            .testTag("unity_banner_reserved_slot"),
        contentAlignment = Alignment.Center
    ) {
        if (bannerView != null) {
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        val parent = bannerView.parent
                        if (parent is android.view.ViewGroup) {
                            parent.removeView(bannerView)
                        }
                        addView(
                            bannerView,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!isBannerLoaded) {
            Text(
                text = if (UnityAdsConfig.isBannerConfigured()) {
                    stringResource(R.string.unity_banner_loading_label)
                } else {
                    stringResource(R.string.unity_banner_reserved_label)
                },
                style = MaterialTheme.typography.labelSmall,
                color = bongColors.textSecondary.copy(alpha = 0.65f)
            )
        }
    }
}

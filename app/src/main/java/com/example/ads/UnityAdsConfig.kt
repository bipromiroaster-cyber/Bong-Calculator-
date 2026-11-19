package com.example.ads

/**
 * Centralized Unity Ads configuration file for Bong Calculator.
 *
 * Unity Dashboard -> Monetization -> Ad Units / Project Settings:
 * - UNITY_GAME_ID: Android Game ID ("800444930")
 * - BANNER_AD_UNIT_ID: Android Banner Ad Unit ID ("BP_Banner_Android")
 * - INTERSTITIAL_AD_UNIT_ID: Android Interstitial Ad Unit ID ("BP_Interstitial_Android")
 * - REWARDED_AD_UNIT_ID: Android Rewarded Ad Unit ID ("BP_Rewarded_Android")
 */
object UnityAdsConfig {

    const val UNITY_GAME_ID: String = "800444930"

    const val BANNER_AD_UNIT_ID: String = "BP_Banner_Android"

    const val INTERSTITIAL_AD_UNIT_ID: String = "BP_Interstitial_Android"

    const val REWARDED_AD_UNIT_ID: String = "BP_Rewarded_Android"

    /**
     * Enables Unity Ads test mode during development/testing.
     */
    const val TEST_MODE: Boolean = true

    /**
     * Minimum time between interstitial ads (90 seconds) so ads only appear
     * at natural breaks and never interrupt active calculations.
     */
    const val INTERSTITIAL_COOLDOWN_MS: Long = 90_000L

    /**
     * Minimum completed calculations before an interstitial is eligible at a natural break.
     */
    const val MIN_CALCULATIONS_BEFORE_INTERSTITIAL: Int = 3

    fun isGameIdConfigured(): Boolean =
        UNITY_GAME_ID.isNotBlank() && !UNITY_GAME_ID.startsWith("YOUR_")

    fun isBannerConfigured(): Boolean =
        isGameIdConfigured() &&
            BANNER_AD_UNIT_ID.isNotBlank() &&
            !BANNER_AD_UNIT_ID.startsWith("YOUR_")

    fun isInterstitialConfigured(): Boolean =
        isGameIdConfigured() &&
            INTERSTITIAL_AD_UNIT_ID.isNotBlank() &&
            !INTERSTITIAL_AD_UNIT_ID.startsWith("YOUR_")

    fun isRewardedConfigured(): Boolean =
        isGameIdConfigured() &&
            REWARDED_AD_UNIT_ID.isNotBlank() &&
            !REWARDED_AD_UNIT_ID.startsWith("YOUR_")
}

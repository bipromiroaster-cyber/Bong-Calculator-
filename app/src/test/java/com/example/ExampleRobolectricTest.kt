package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches Bong Calculator`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bong Calculator", appName)
    }

    @Test
    fun `basic calculation and scientific toggle flow`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("app_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unity_banner_reserved_slot").assertIsDisplayed()

        // Calculate 7 + 8 = 15
        composeTestRule.onNodeWithTag("btn_7").performClick()
        composeTestRule.onNodeWithTag("btn_add").performClick()
        composeTestRule.onNodeWithTag("btn_8").performClick()
        composeTestRule.onNodeWithTag("btn_equals").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("display_primary_text").assertTextEquals("15")

        // Toggle scientific mode and compute sin(30) = 0.5
        composeTestRule.onNodeWithTag("btn_scientific_toggle").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNodeWithTag("btn_sin").assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithTag("btn_sin").performClick()
        composeTestRule.onNodeWithTag("btn_3").performClick()
        composeTestRule.onNodeWithTag("btn_0").performClick()
        composeTestRule.onNodeWithTag("btn_right_paren").performClick()
        composeTestRule.onNodeWithTag("btn_equals").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("display_primary_text").assertTextEquals("0.5")
    }

    @Test
    fun `unity ads configuration uses real ids with test mode enabled and settings shows rewarded ad button`() {
        assertEquals("800444930", com.example.ads.UnityAdsConfig.UNITY_GAME_ID)
        assertEquals("BP_Banner_Android", com.example.ads.UnityAdsConfig.BANNER_AD_UNIT_ID)
        assertEquals("BP_Interstitial_Android", com.example.ads.UnityAdsConfig.INTERSTITIAL_AD_UNIT_ID)
        assertEquals("BP_Rewarded_Android", com.example.ads.UnityAdsConfig.REWARDED_AD_UNIT_ID)
        org.junit.Assert.assertTrue(com.example.ads.UnityAdsConfig.TEST_MODE)
        org.junit.Assert.assertTrue(com.example.ads.UnityAdsConfig.isGameIdConfigured())
        org.junit.Assert.assertTrue(com.example.ads.UnityAdsConfig.isBannerConfigured())
        org.junit.Assert.assertTrue(com.example.ads.UnityAdsConfig.isInterstitialConfigured())
        org.junit.Assert.assertTrue(com.example.ads.UnityAdsConfig.isRewardedConfigured())

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("top_settings_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNodeWithTag("watch_rewarded_ad_button").assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithTag("watch_rewarded_ad_button")
            .assertTextContains("Watch Ad & Unlock Premium Theme")
        composeTestRule.onNodeWithTag("premium_theme_switch").assertDoesNotExist()
    }
}

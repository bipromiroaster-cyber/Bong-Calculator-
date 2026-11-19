package com.example.ads

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalBongColors

/**
 * Configuration contract prepared for future native Unity Ads SDK integration
 * (e.g., com.unity3d.ads.UnityAds & com.unity3d.services.banners.BannerView).
 *
 * Integration guide when adding Unity Ads SDK later:
 * 1. Add `com.unity3d.ads:unity-ads` dependency to `app/build.gradle.kts`.
 * 2. Initialize `UnityAds.initialize(context, gameId, testMode, listener)` in Application/Activity.
 * 3. Replace the reserved container inside [UnityBannerAdSlot] with an `AndroidView` hosting
 *    `BannerView(activity, placementId, UnityBannerSize(320, 50))`.
 */
data class UnityBannerConfig(
    val gameId: String = "",
    val bannerPlacementId: String = "Banner_Android",
    val bannerWidthDp: Int = 320,
    val bannerHeightDp: Int = 50,
    val isSdkAttached: Boolean = false
)

/**
 * Dedicated reserved bottom slot for future Unity Ads Banner integration.
 * Contains no fake advertisements and no AdMob references.
 */
@Composable
fun UnityBannerAdSlot(
    modifier: Modifier = Modifier,
    config: UnityBannerConfig = UnityBannerConfig()
) {
    val bongColors = LocalBongColors.current
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(config.bannerHeightDp.dp)
            .clip(shape)
            .background(bongColors.keySurface.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = bongColors.glassBorderSubtle,
                shape = shape
            )
            .padding(horizontal = 12.dp)
            .testTag("unity_banner_reserved_slot"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.unity_banner_reserved_label),
            style = MaterialTheme.typography.labelSmall,
            color = bongColors.textSecondary.copy(alpha = 0.65f)
        )
    }
}

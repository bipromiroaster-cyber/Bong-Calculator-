package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.AngleUnit
import com.example.ui.theme.LocalBongColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isDarkTheme: Boolean,
    angleUnit: AngleUnit,
    hapticEnabled: Boolean,
    bannersPaused: Boolean,
    isRewardedReady: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSetAngleUnit: (AngleUnit) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onWatchRewardedAd: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bongColors = LocalBongColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bongColors.sheetSurface,
        contentColor = bongColors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = bongColors.textPrimary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cd_close_sheet),
                        tint = bongColors.textSecondary
                    )
                }
            }

            // Light / Dark Theme Switch
            SettingRowCard(
                icon = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                title = stringResource(R.string.setting_dark_theme),
                subtitle = if (isDarkTheme) {
                    stringResource(R.string.setting_dark_theme_subtitle)
                } else {
                    stringResource(R.string.setting_light_theme_subtitle)
                }
            ) {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onToggleDarkTheme,
                    modifier = Modifier.testTag("theme_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = bongColors.accentBlue,
                        uncheckedThumbColor = bongColors.accentPurple,
                        uncheckedTrackColor = bongColors.scientificSurface
                    )
                )
            }

            // Angle Unit (DEG / RAD)
            SettingRowCard(
                icon = Icons.Outlined.Straighten,
                title = stringResource(R.string.setting_angle_unit),
                subtitle = stringResource(R.string.setting_angle_unit_subtitle)
            ) {
                SingleChoiceSegmentedButtonRow {
                    AngleUnit.entries.forEachIndexed { index, unit ->
                        SegmentedButton(
                            selected = angleUnit == unit,
                            onClick = { onSetAngleUnit(unit) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = AngleUnit.entries.size
                            ),
                            modifier = Modifier.testTag("setting_angle_${unit.name.lowercase()}")
                        ) {
                            Text(
                                text = unit.name,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Haptic Feedback Switch
            SettingRowCard(
                icon = Icons.Outlined.Vibration,
                title = stringResource(R.string.setting_haptics),
                subtitle = stringResource(R.string.setting_haptics_subtitle)
            ) {
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onToggleHapticFeedback,
                    modifier = Modifier.testTag("haptic_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = bongColors.accentPurple
                    )
                )
            }

            // Optional Unity Rewarded Ad to pause banners for current session
            SettingRowCard(
                icon = Icons.Outlined.AdsClick,
                title = stringResource(R.string.setting_ads_slot),
                subtitle = if (bannersPaused) {
                    stringResource(R.string.reward_banners_already_paused)
                } else {
                    stringResource(R.string.setting_ads_slot_subtitle)
                }
            ) {
                if (!bannersPaused) {
                    FilledTonalButton(
                        onClick = onWatchRewardedAd,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isRewardedReady) {
                                bongColors.accentBlue.copy(alpha = 0.22f)
                            } else {
                                bongColors.scientificSurface
                            },
                            contentColor = bongColors.accentBlue
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("watch_rewarded_ad_button")
                    ) {
                        Text(
                            text = stringResource(R.string.watch_rewarded_ad),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SettingRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    val bongColors = LocalBongColors.current
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bongColors.keySurface.copy(alpha = 0.75f))
            .border(width = 1.dp, color = bongColors.glassBorderSubtle, shape = shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = bongColors.accentBlue,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = bongColors.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = bongColors.textSecondary
            )
        }
        trailing()
    }
}

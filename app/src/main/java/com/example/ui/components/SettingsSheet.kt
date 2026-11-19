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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
    isPremiumThemeUnlocked: Boolean,
    usePremiumTheme: Boolean,
    isRewardedReady: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSetAngleUnit: (AngleUnit) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onSetUsePremiumTheme: (Boolean) -> Unit,
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

            // Premium Theme Section — Unlocked Toggle or Clearly Visible Rewarded Ad Unlock Button
            if (isPremiumThemeUnlocked) {
                SettingRowCard(
                    icon = Icons.Outlined.AutoAwesome,
                    title = stringResource(R.string.setting_premium_theme),
                    subtitle = stringResource(R.string.setting_premium_theme_unlocked_subtitle)
                ) {
                    Switch(
                        checked = usePremiumTheme,
                        onCheckedChange = onSetUsePremiumTheme,
                        modifier = Modifier.testTag("premium_theme_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = bongColors.accentBlue
                        )
                    )
                }
            } else {
                val cardShape = RoundedCornerShape(18.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(bongColors.scientificSurface.copy(alpha = 0.72f))
                        .border(width = 1.dp, color = bongColors.glassBorderHighlight.copy(alpha = 0.45f), shape = cardShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = bongColors.accentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.setting_premium_theme),
                                style = MaterialTheme.typography.titleMedium,
                                color = bongColors.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.setting_premium_theme_locked_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = bongColors.textSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onWatchRewardedAd,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRewardedReady) bongColors.accentBlue else bongColors.accentPurple,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("watch_rewarded_ad_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.watch_ad_unlock_premium_theme),
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

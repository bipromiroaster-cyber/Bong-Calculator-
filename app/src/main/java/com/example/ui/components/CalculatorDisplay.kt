package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AngleUnit
import com.example.domain.CalculatorError
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalBongColors
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun CalculatorDisplay(
    expression: String,
    displayResult: String,
    livePreview: String,
    error: CalculatorError?,
    justEvaluated: Boolean,
    isScientificMode: Boolean,
    angleUnit: AngleUnit,
    onCopyResult: () -> Unit,
    onToggleAngleUnit: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val bongColors = LocalBongColors.current
    val shape = RoundedCornerShape(if (isCompact) 22.dp else 28.dp)
    val exprScroll = rememberScrollState()
    val resultScroll = rememberScrollState()

    LaunchedEffect(expression) {
        exprScroll.animateScrollTo(exprScroll.maxValue)
    }
    LaunchedEffect(displayResult) {
        resultScroll.animateScrollTo(resultScroll.maxValue)
    }

    val errorMessage = when (error) {
        CalculatorError.DIVIDE_BY_ZERO -> stringResource(R.string.error_divide_by_zero)
        CalculatorError.DOMAIN_ERROR -> stringResource(R.string.error_domain)
        CalculatorError.INVALID_EXPRESSION -> stringResource(R.string.error_invalid_expression)
        CalculatorError.OVERFLOW -> stringResource(R.string.error_overflow)
        null -> null
    }

    val mainText = errorMessage ?: displayResult
    val baseMaxFont = if (isCompact) 38.sp else 48.sp
    val mainFontSize = when {
        errorMessage != null -> 22.sp
        mainText.length <= 9 -> baseMaxFont
        mainText.length <= 13 -> if (isCompact) 32.sp else 38.sp
        mainText.length <= 18 -> 28.sp
        else -> 22.sp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        bongColors.displaySurface,
                        bongColors.keySurface.copy(alpha = if (bongColors.isDark) 0.55f else 0.78f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        bongColors.glassBorderHighlight,
                        bongColors.glassBorderSubtle
                    )
                ),
                shape = shape
            )
            .padding(
                horizontal = 18.dp,
                vertical = if (isCompact) 10.dp else 14.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status & quick copy row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onToggleAngleUnit,
                        shape = RoundedCornerShape(10.dp),
                        color = bongColors.operatorSurface,
                        modifier = Modifier.testTag("angle_unit_chip")
                    ) {
                        Text(
                            text = if (angleUnit == AngleUnit.DEG) {
                                stringResource(R.string.angle_deg)
                            } else {
                                stringResource(R.string.angle_rad)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = bongColors.accentBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isScientificMode,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bongColors.scientificSurface
                        ) {
                            Text(
                                text = stringResource(R.string.sci_mode_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = bongColors.accentPurple,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onCopyResult,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("copy_result_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.cd_copy_button),
                        tint = bongColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 8.dp))

            // Secondary expression line (shown when evaluated or when live preview is active)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 22.dp else 28.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val topLineText = when {
                    justEvaluated -> "$expression ="
                    errorMessage != null -> expression
                    livePreview.isNotEmpty() -> expression
                    else -> ""
                }
                if (topLineText.isNotEmpty()) {
                    Text(
                        text = topLineText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 18.sp
                        ),
                        color = bongColors.textSecondary,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .horizontalScroll(exprScroll)
                            .testTag("display_expression_line")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Primary calculation display
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = mainFontSize,
                        lineHeight = mainFontSize * 1.12f
                    ),
                    color = if (errorMessage != null) bongColors.dangerAccent else bongColors.textPrimary,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .horizontalScroll(resultScroll)
                        .testTag("display_primary_text")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Live evaluation preview line (before pressing =)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (!justEvaluated && errorMessage == null && livePreview.isNotEmpty()) {
                    Text(
                        text = "= $livePreview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 16.sp
                        ),
                        color = bongColors.accentBlue,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("display_live_preview")
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AngleUnit

@Composable
fun CalculatorKeypad(
    isScientificMode: Boolean,
    angleUnit: AngleUnit,
    hapticEnabled: Boolean,
    buttonHeight: Dp,
    sciButtonHeight: Dp,
    spacing: Dp,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onLeftParen: () -> Unit,
    onRightParen: () -> Unit,
    onPercent: () -> Unit,
    onToggleSign: () -> Unit,
    onAllClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onScientificFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onPostfixOp: (String) -> Unit,
    onReciprocal: () -> Unit,
    onToggleScientificMode: () -> Unit,
    onToggleAngleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Quick Utility Bar: SCI toggle, DEG/RAD, (, )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalcButton(
                label = stringResource(R.string.sci_mode_label),
                variant = if (isScientificMode) CalcButtonVariant.EQUALS else CalcButtonVariant.SCIENTIFIC,
                onClick = onToggleScientificMode,
                testTag = "btn_scientific_toggle",
                contentDescriptionText = stringResource(R.string.cd_scientific_toggle),
                hapticEnabled = hapticEnabled,
                cornerRadius = 16.dp,
                textStyle = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Functions,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.sci_mode_label),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }

            GlassCalcButton(
                label = if (angleUnit == AngleUnit.DEG) "DEG" else "RAD",
                variant = CalcButtonVariant.SCIENTIFIC,
                onClick = onToggleAngleUnit,
                testTag = "btn_angle_toggle",
                hapticEnabled = hapticEnabled,
                cornerRadius = 16.dp,
                textStyle = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            GlassCalcButton(
                label = "(",
                variant = CalcButtonVariant.OPERATOR,
                onClick = onLeftParen,
                testTag = "btn_left_paren",
                hapticEnabled = hapticEnabled,
                cornerRadius = 16.dp,
                textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            GlassCalcButton(
                label = ")",
                variant = CalcButtonVariant.OPERATOR,
                onClick = onRightParen,
                testTag = "btn_right_paren",
                hapticEnabled = hapticEnabled,
                cornerRadius = 16.dp,
                textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )
        }

        // Expandable Scientific Panel
        AnimatedVisibility(
            visible = isScientificMode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                val sciTextStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp)
                // Sci Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    GlassCalcButton(
                        label = "sin",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onScientificFunction("sin(") },
                        testTag = "btn_sin",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "cos",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onScientificFunction("cos(") },
                        testTag = "btn_cos",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "tan",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onScientificFunction("tan(") },
                        testTag = "btn_tan",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "ln",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onScientificFunction("ln(") },
                        testTag = "btn_ln",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "log",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onScientificFunction("log(") },
                        testTag = "btn_log",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "1/x",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = onReciprocal,
                        testTag = "btn_reciprocal",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                }

                // Sci Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    GlassCalcButton(
                        label = "√",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onScientificFunction("√(") },
                        testTag = "btn_sqrt",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "x²",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onPostfixOp("²") },
                        testTag = "btn_square",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "xʸ",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onOperator("^") },
                        testTag = "btn_power",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "n!",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onPostfixOp("!") },
                        testTag = "btn_factorial",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "π",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onConstant("π") },
                        testTag = "btn_pi",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                    GlassCalcButton(
                        label = "e",
                        variant = CalcButtonVariant.SCIENTIFIC,
                        onClick = { onConstant("e") },
                        testTag = "btn_e",
                        hapticEnabled = hapticEnabled,
                        cornerRadius = 16.dp,
                        textStyle = sciTextStyle,
                        modifier = Modifier
                            .weight(1f)
                            .height(sciButtonHeight)
                    )
                }
            }
        }

        // Standard 4x5 Calculator Grid
        val numTextStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 22.sp)
        val opTextStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp)

        // Row 1: AC, DEL, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalcButton(
                label = "AC",
                variant = CalcButtonVariant.ACTION_DANGER,
                onClick = onAllClear,
                testTag = "btn_ac",
                hapticEnabled = hapticEnabled,
                textStyle = numTextStyle.copy(fontSize = 18.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
            GlassCalcButton(
                label = "DEL",
                variant = CalcButtonVariant.ACTION_DANGER,
                onClick = onBackspace,
                testTag = "btn_del",
                contentDescriptionText = stringResource(R.string.cd_backspace_button),
                hapticEnabled = hapticEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = stringResource(R.string.cd_backspace_button),
                    modifier = Modifier.size(22.dp)
                )
            }
            GlassCalcButton(
                label = "%",
                variant = CalcButtonVariant.OPERATOR,
                onClick = onPercent,
                testTag = "btn_percent",
                hapticEnabled = hapticEnabled,
                textStyle = opTextStyle.copy(fontSize = 20.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
            GlassCalcButton(
                label = "÷",
                variant = CalcButtonVariant.OPERATOR,
                onClick = { onOperator("÷") },
                testTag = "btn_divide",
                hapticEnabled = hapticEnabled,
                textStyle = opTextStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            listOf("7", "8", "9").forEach { digit ->
                GlassCalcButton(
                    label = digit,
                    variant = CalcButtonVariant.NUMERIC,
                    onClick = { onDigit(digit) },
                    testTag = "btn_$digit",
                    hapticEnabled = hapticEnabled,
                    textStyle = numTextStyle,
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                )
            }
            GlassCalcButton(
                label = "×",
                variant = CalcButtonVariant.OPERATOR,
                onClick = { onOperator("×") },
                testTag = "btn_multiply",
                hapticEnabled = hapticEnabled,
                textStyle = opTextStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            listOf("4", "5", "6").forEach { digit ->
                GlassCalcButton(
                    label = digit,
                    variant = CalcButtonVariant.NUMERIC,
                    onClick = { onDigit(digit) },
                    testTag = "btn_$digit",
                    hapticEnabled = hapticEnabled,
                    textStyle = numTextStyle,
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                )
            }
            GlassCalcButton(
                label = "−",
                variant = CalcButtonVariant.OPERATOR,
                onClick = { onOperator("−") },
                testTag = "btn_subtract",
                hapticEnabled = hapticEnabled,
                textStyle = opTextStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            listOf("1", "2", "3").forEach { digit ->
                GlassCalcButton(
                    label = digit,
                    variant = CalcButtonVariant.NUMERIC,
                    onClick = { onDigit(digit) },
                    testTag = "btn_$digit",
                    hapticEnabled = hapticEnabled,
                    textStyle = numTextStyle,
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                )
            }
            GlassCalcButton(
                label = "+",
                variant = CalcButtonVariant.OPERATOR,
                onClick = { onOperator("+") },
                testTag = "btn_add",
                hapticEnabled = hapticEnabled,
                textStyle = opTextStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
        }

        // Row 5: +/−, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalcButton(
                label = "+/−",
                variant = CalcButtonVariant.NUMERIC,
                onClick = onToggleSign,
                testTag = "btn_plus_minus",
                hapticEnabled = hapticEnabled,
                textStyle = numTextStyle.copy(fontSize = 18.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
            GlassCalcButton(
                label = "0",
                variant = CalcButtonVariant.NUMERIC,
                onClick = { onDigit("0") },
                testTag = "btn_0",
                hapticEnabled = hapticEnabled,
                textStyle = numTextStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
            GlassCalcButton(
                label = ".",
                variant = CalcButtonVariant.NUMERIC,
                onClick = onDecimal,
                testTag = "btn_decimal",
                hapticEnabled = hapticEnabled,
                textStyle = numTextStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
            GlassCalcButton(
                label = "=",
                variant = CalcButtonVariant.EQUALS,
                onClick = onEquals,
                testTag = "btn_equals",
                hapticEnabled = hapticEnabled,
                textStyle = opTextStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            )
        }
    }
}

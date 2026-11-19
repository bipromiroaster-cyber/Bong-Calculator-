package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalBongColors

enum class CalcButtonVariant {
    NUMERIC,
    OPERATOR,
    ACTION_DANGER,
    SCIENTIFIC,
    EQUALS
}

@Composable
fun GlassCalcButton(
    label: String,
    variant: CalcButtonVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
    contentDescriptionText: String = label,
    hapticEnabled: Boolean = true,
    cornerRadius: Dp = 22.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    iconContent: (@Composable () -> Unit)? = null
) {
    val bongColors = LocalBongColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_press_scale"
    )

    val shape: Shape = RoundedCornerShape(cornerRadius)

    val backgroundBrush = when (variant) {
        CalcButtonVariant.NUMERIC -> Brush.linearGradient(
            colors = listOf(
                bongColors.keySurface,
                bongColors.keySurface.copy(alpha = (bongColors.keySurface.alpha * 0.72f).coerceIn(0f, 1f))
            )
        )
        CalcButtonVariant.OPERATOR -> Brush.linearGradient(
            colors = listOf(
                bongColors.operatorSurface,
                bongColors.accentBlue.copy(alpha = if (bongColors.isDark) 0.16f else 0.22f)
            )
        )
        CalcButtonVariant.SCIENTIFIC -> Brush.linearGradient(
            colors = listOf(
                bongColors.scientificSurface,
                bongColors.accentPurple.copy(alpha = if (bongColors.isDark) 0.16f else 0.20f)
            )
        )
        CalcButtonVariant.ACTION_DANGER -> Brush.linearGradient(
            colors = listOf(
                bongColors.dangerSurface,
                bongColors.dangerAccent.copy(alpha = if (bongColors.isDark) 0.14f else 0.18f)
            )
        )
        CalcButtonVariant.EQUALS -> Brush.linearGradient(
            colors = listOf(
                bongColors.accentBlue,
                bongColors.accentPurple
            )
        )
    }

    val borderBrush = if (variant == CalcButtonVariant.EQUALS) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                bongColors.accentPurple.copy(alpha = 0.45f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                bongColors.glassBorderHighlight,
                bongColors.glassBorderSubtle
            )
        )
    }

    val contentColor = when (variant) {
        CalcButtonVariant.NUMERIC -> bongColors.textPrimary
        CalcButtonVariant.OPERATOR -> bongColors.accentBlue
        CalcButtonVariant.SCIENTIFIC -> if (bongColors.isDark) Color(0xFFD8B4FE) else bongColors.accentPurple
        CalcButtonVariant.ACTION_DANGER -> bongColors.dangerAccent
        CalcButtonVariant.EQUALS -> Color.White
    }

    val elevationModifier = if (variant == CalcButtonVariant.EQUALS) {
        Modifier.shadow(
            elevation = if (isPressed) 4.dp else 12.dp,
            shape = shape,
            ambientColor = bongColors.accentPurple,
            spotColor = bongColors.accentBlue
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(elevationModifier)
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                role = Role.Button,
                onClick = {
                    if (hapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onClick()
                }
            )
            .padding(contentPadding)
            .semantics { contentDescription = contentDescriptionText }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (iconContent != null) {
                iconContent()
            } else {
                Text(
                    text = label,
                    style = textStyle,
                    fontWeight = if (variant == CalcButtonVariant.EQUALS || variant == CalcButtonVariant.OPERATOR) {
                        FontWeight.Bold
                    } else {
                        textStyle.fontWeight
                    },
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

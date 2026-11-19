package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ads.UnityBannerAdSlot
import com.example.domain.AngleUnit
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.HistoryPanelContent
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.theme.LocalBongColors
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bongColors = LocalBongColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val copiedMessage = stringResource(R.string.copied_to_clipboard)
    val nothingToCopyMessage = stringResource(R.string.nothing_to_copy)

    fun copyToClipboard(text: String?) {
        if (text.isNullOrBlank()) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(nothingToCopyMessage)
            }
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Bong Calculator Result", text))
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("$copiedMessage: $text")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        bongColors.bgTop,
                        bongColors.bgMid,
                        bongColors.bgBottom
                    )
                )
            )
            .drawBehind {
                // Ambient neon blue & purple glow orbs
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bongColors.glowBlue, Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.14f),
                        radius = size.maxDimension * 0.45f
                    ),
                    radius = size.maxDimension * 0.45f,
                    center = Offset(size.width * 0.15f, size.height * 0.14f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bongColors.glowPurple, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.48f),
                        radius = size.maxDimension * 0.42f
                    ),
                    radius = size.maxDimension * 0.42f,
                    center = Offset(size.width * 0.88f, size.height * 0.48f)
                )
            }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = bongColors.sheetSurface,
                        contentColor = bongColors.textPrimary,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        ) { _ ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                val isWideLayout = maxWidth >= 720.dp
                val availHeight = maxHeight

                // Compute responsive button heights so display + keypad + reserved banner fit cleanly
                val spacing = if (availHeight < 660.dp) 7.dp else 10.dp
                val targetButtonHeight = when {
                    uiState.isScientificMode && availHeight < 700.dp -> 48.dp
                    uiState.isScientificMode && availHeight < 820.dp -> 54.dp
                    uiState.isScientificMode -> 60.dp
                    availHeight < 660.dp -> 52.dp
                    availHeight < 780.dp -> 60.dp
                    else -> 68.dp
                }
                val sciButtonHeight = when {
                    availHeight < 700.dp -> 48.dp
                    else -> 50.dp
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top App Bar: Bong Calculator + History & Settings icons
                    CalculatorTopBar(
                        onHistoryClick = { showHistorySheet = true },
                        onSettingsClick = { showSettingsSheet = true },
                        showHistoryIcon = !isWideLayout
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isWideLayout) {
                        // Expanded / Tablet Adaptive Layout: Side-by-side Calculator + History Pane
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1.35f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                CalculatorDisplay(
                                    expression = uiState.expression,
                                    displayResult = uiState.displayResult,
                                    livePreview = uiState.livePreview,
                                    error = uiState.error,
                                    justEvaluated = uiState.justEvaluated,
                                    isScientificMode = uiState.isScientificMode,
                                    angleUnit = uiState.angleUnit,
                                    onCopyResult = { copyToClipboard(viewModel.getCopyableResult()) },
                                    onToggleAngleUnit = viewModel::onToggleAngleUnit,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                CalculatorKeypad(
                                    isScientificMode = uiState.isScientificMode,
                                    angleUnit = uiState.angleUnit,
                                    hapticEnabled = uiState.hapticFeedbackEnabled,
                                    buttonHeight = targetButtonHeight,
                                    sciButtonHeight = sciButtonHeight,
                                    spacing = spacing,
                                    onDigit = viewModel::onDigit,
                                    onDecimal = viewModel::onDecimal,
                                    onOperator = viewModel::onOperator,
                                    onLeftParen = viewModel::onLeftParen,
                                    onRightParen = viewModel::onRightParen,
                                    onPercent = viewModel::onPercent,
                                    onToggleSign = viewModel::onToggleSign,
                                    onAllClear = viewModel::onAllClear,
                                    onBackspace = viewModel::onBackspace,
                                    onEquals = viewModel::onEquals,
                                    onScientificFunction = viewModel::onScientificFunction,
                                    onConstant = viewModel::onConstant,
                                    onPostfixOp = viewModel::onPostfixOp,
                                    onReciprocal = viewModel::onReciprocal,
                                    onToggleScientificMode = viewModel::onToggleScientificMode,
                                    onToggleAngleUnit = viewModel::onToggleAngleUnit
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(0.95f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(bongColors.displaySurface)
                                    .border(
                                        width = 1.dp,
                                        color = bongColors.glassBorderSubtle,
                                        shape = RoundedCornerShape(28.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                HistoryPanelContent(
                                    history = uiState.history,
                                    onSelectEntry = { viewModel.onHistoryItemSelected(it) },
                                    onCopyResult = { copyToClipboard(it) },
                                    onDeleteEntry = viewModel::onDeleteHistoryItem,
                                    onClearHistory = viewModel::onClearHistory,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        // Phone / Compact & Medium Layout
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 540.dp)
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            CalculatorDisplay(
                                expression = uiState.expression,
                                displayResult = uiState.displayResult,
                                livePreview = uiState.livePreview,
                                error = uiState.error,
                                justEvaluated = uiState.justEvaluated,
                                isScientificMode = uiState.isScientificMode,
                                angleUnit = uiState.angleUnit,
                                onCopyResult = { copyToClipboard(viewModel.getCopyableResult()) },
                                onToggleAngleUnit = viewModel::onToggleAngleUnit,
                                modifier = Modifier.weight(1f, fill = true)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            CalculatorKeypad(
                                isScientificMode = uiState.isScientificMode,
                                angleUnit = uiState.angleUnit,
                                hapticEnabled = uiState.hapticFeedbackEnabled,
                                buttonHeight = targetButtonHeight,
                                sciButtonHeight = sciButtonHeight,
                                spacing = spacing,
                                onDigit = viewModel::onDigit,
                                onDecimal = viewModel::onDecimal,
                                onOperator = viewModel::onOperator,
                                onLeftParen = viewModel::onLeftParen,
                                onRightParen = viewModel::onRightParen,
                                onPercent = viewModel::onPercent,
                                onToggleSign = viewModel::onToggleSign,
                                onAllClear = viewModel::onAllClear,
                                onBackspace = viewModel::onBackspace,
                                onEquals = viewModel::onEquals,
                                onScientificFunction = viewModel::onScientificFunction,
                                onConstant = viewModel::onConstant,
                                onPostfixOp = viewModel::onPostfixOp,
                                onReciprocal = viewModel::onReciprocal,
                                onToggleScientificMode = viewModel::onToggleScientificMode,
                                onToggleAngleUnit = viewModel::onToggleAngleUnit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom reserved area for future Unity Ads Banner SDK integration
                    UnityBannerAdSlot(
                        modifier = Modifier
                            .widthIn(max = 540.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    if (showHistorySheet) {
        HistoryBottomSheet(
            history = uiState.history,
            onSelectEntry = { viewModel.onHistoryItemSelected(it) },
            onCopyResult = { copyToClipboard(it) },
            onDeleteEntry = viewModel::onDeleteHistoryItem,
            onClearHistory = viewModel::onClearHistory,
            onDismiss = { showHistorySheet = false }
        )
    }

    if (showSettingsSheet) {
        SettingsBottomSheet(
            isDarkTheme = uiState.isDarkTheme,
            angleUnit = uiState.angleUnit,
            hapticEnabled = uiState.hapticFeedbackEnabled,
            onToggleDarkTheme = viewModel::onSetDarkTheme,
            onSetAngleUnit = { unit: AngleUnit ->
                if (uiState.angleUnit != unit) {
                    viewModel.onToggleAngleUnit()
                }
            },
            onToggleHapticFeedback = viewModel::onSetHapticFeedback,
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@Composable
private fun CalculatorTopBar(
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showHistoryIcon: Boolean
) {
    val bongColors = LocalBongColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(bongColors.accentBlue, bongColors.accentPurple)
                        )
                    )
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = bongColors.textPrimary,
                modifier = Modifier.testTag("app_title")
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showHistoryIcon) {
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(bongColors.keySurface.copy(alpha = 0.5f))
                        .border(1.dp, bongColors.glassBorderSubtle, CircleShape)
                        .testTag("top_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = stringResource(R.string.cd_history_button),
                        tint = bongColors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(bongColors.keySurface.copy(alpha = 0.5f))
                    .border(1.dp, bongColors.glassBorderSubtle, CircleShape)
                    .testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.cd_settings_button),
                    tint = bongColors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.R
import com.example.data.local.CalculationHistoryEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalBongColors
import com.example.ui.theme.SpaceGroteskFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    history: List<CalculationHistoryEntity>,
    onSelectEntry: (CalculationHistoryEntity) -> Unit,
    onCopyResult: (String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearHistory: () -> Unit,
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
        HistoryPanelContent(
            history = history,
            onSelectEntry = {
                onSelectEntry(it)
                onDismiss()
            },
            onCopyResult = onCopyResult,
            onDeleteEntry = onDeleteEntry,
            onClearHistory = onClearHistory,
            onClose = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 520.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun HistoryPanelContent(
    history: List<CalculationHistoryEntity>,
    onSelectEntry: (CalculationHistoryEntity) -> Unit,
    onCopyResult: (String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bongColors = LocalBongColors.current
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = bongColors.sheetSurface,
            titleContentColor = bongColors.textPrimary,
            textContentColor = bongColors.textSecondary,
            title = { Text(stringResource(R.string.clear_history_confirm_title)) },
            text = { Text(stringResource(R.string.clear_history_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearHistory()
                    },
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        color = bongColors.dangerAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = bongColors.textSecondary
                    )
                }
            }
        )
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.titleLarge,
                color = bongColors.textPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = bongColors.dangerAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = stringResource(R.string.clear_history),
                            color = bongColors.dangerAccent,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if (onClose != null) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.cd_close_sheet),
                            tint = bongColors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 40.dp)
                    .testTag("history_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = bongColors.accentBlue.copy(alpha = 0.7f),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = stringResource(R.string.history_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = bongColors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.history_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = bongColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(history, key = { it.id }) { entry ->
                    HistoryItemCard(
                        entry = entry,
                        onClick = { onSelectEntry(entry) },
                        onCopy = { onCopyResult(entry.result) },
                        onDelete = { onDeleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    entry: CalculationHistoryEntity,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val bongColors = LocalBongColors.current
    val shape = RoundedCornerShape(18.dp)
    val timeText = remember(entry.timestamp) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        bongColors.keySurface,
                        bongColors.displaySurface
                    )
                )
            )
            .border(
                width = 1.dp,
                color = bongColors.glassBorderSubtle,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
            .testTag("history_item_${entry.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = bongColors.textSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.expression,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily
                ),
                color = bongColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "= ${entry.result}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = bongColors.accentBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onCopy,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("history_copy_${entry.id}")
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = stringResource(R.string.copy_result),
                tint = bongColors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("history_delete_${entry.id}")
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = stringResource(R.string.cd_delete_history_item),
                tint = bongColors.dangerAccent.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

package com.oleksii.pipecut.ui.result

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oleksii.pipecut.core.model.DevPoint
import java.util.Locale

/**
 * Adds the result panel to a parent `LazyColumn`. The whole screen is
 * one scrollable list — no nested scrolls. The table header is a real
 * sticky header.
 *
 * Stateless: takes a [ResultUiState] and writes items.
 */
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.resultPanel(state: ResultUiState) {
    when (state) {
        ResultUiState.Empty -> item("result-hint-empty") {
            HintBlock(text = ResultStrings.EMPTY_HINT)
        }
        ResultUiState.SaddleNotImplemented -> item("result-hint-saddle") {
            HintBlock(text = ResultStrings.SADDLE_NOT_IMPLEMENTED)
        }
        is ResultUiState.Computed -> {
            stickyHeader(key = "result-table-header") { TableHeader() }
            items(
                items = state.development.points,
                key = { point -> "row-${point.phiDeg}" },
            ) { point ->
                ResultRow(point)
            }
        }
        is ResultUiState.Error -> {
            item("result-error-banner") { ErrorBanner(message = state.message) }
            if (state.previous != null) {
                stickyHeader(key = "result-table-header-after-error") { TableHeader() }
                items(
                    items = state.previous.points,
                    key = { point -> "prev-row-${point.phiDeg}" },
                ) { point ->
                    ResultRow(point)
                }
            }
        }
    }
}

@Composable
private fun HintBlock(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TableHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        // Sticky header sits above scrolling rows; a divider underneath
        // keeps it visually attached when stuck to the top edge.
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = ResultStrings.TABLE_HEADER_PHI,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = ResultStrings.TABLE_HEADER_LENGTH,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun ResultRow(point: DevPoint) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = formatPhi(point.phiDeg))
        Text(text = formatLength(point.lengthMm))
    }
    HorizontalDivider()
}

internal fun formatPhi(deg: Double): String =
    String.format(Locale.US, "%.1f", deg)

internal fun formatLength(mm: Double): String =
    String.format(Locale.US, "%.2f", mm)

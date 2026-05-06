package com.oleksii.pipecut.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.oleksii.pipecut.core.model.Development

/**
 * Stateless result panel. The header row always shows; the body
 * depends on [state].
 */
@Composable
fun ResultTable(
    state: ResultUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (state) {
            ResultUiState.Empty ->
                HintBlock(text = ResultStrings.EMPTY_HINT)
            ResultUiState.SaddleNotImplemented ->
                HintBlock(text = ResultStrings.SADDLE_NOT_IMPLEMENTED)
            is ResultUiState.Computed ->
                DevelopmentTable(development = state.development)
            is ResultUiState.Error -> {
                ErrorBanner(message = state.message)
                if (state.previous != null) {
                    DevelopmentTable(development = state.previous)
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
private fun DevelopmentTable(development: Development) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TableHeader()
        HorizontalDivider()
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = development.points) { point ->
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
        }
    }
}

@Composable
private fun TableHeader() {
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
}

private fun formatPhi(deg: Double): String = "%.1f".format(deg)
private fun formatLength(mm: Double): String = "%.2f".format(mm)

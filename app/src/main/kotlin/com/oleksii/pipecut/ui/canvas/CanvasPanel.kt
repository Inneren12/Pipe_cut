package com.oleksii.pipecut.ui.canvas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.ui.result.ResultUiState

@Suppress("DEPRECATION") // ResultUiState.SaddleNotImplemented removed in PR12.
fun LazyListScope.canvasPanel(resultState: ResultUiState) {
    val pair: Pair<CutRequest, Development>? = when (resultState) {
        is ResultUiState.Computed -> resultState.request to resultState.development
        is ResultUiState.Error -> {
            val req = resultState.previousRequest
            val dev = resultState.previous
            if (req != null && dev != null) req to dev else null
        }
        ResultUiState.Empty,
        ResultUiState.SaddleNotImplemented -> null
    }
    if (pair == null) return
    val (request, development) = pair
    item("canvas-panel") {
        CanvasPanelContent(development = development, request = request)
    }
}

@Composable
private fun CanvasPanelContent(
    development: Development,
    request: CutRequest,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = CanvasStrings.SECTION_TITLE,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = CanvasStrings.DEV_2D_LABEL,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
        DevelopmentCanvas2D(
            development = development,
            pipe = request.pipe,
        )
        Text(
            text = CanvasStrings.LEGEND_AXIS_PHI,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = CanvasStrings.LEGEND_AXIS_LENGTH,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = CanvasStrings.PREVIEW_3D_LABEL,
                    style = MaterialTheme.typography.labelMedium,
                )
                PipePreviewCanvas3D(
                    development = development,
                    pipe = request.pipe,
                )
            }
        }
    }
}

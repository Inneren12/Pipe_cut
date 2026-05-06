package com.oleksii.pipecut.ui.input

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oleksii.pipecut.ui.vm.InputViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    viewModel: InputViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(text = "Pipe_cut") })
        },
    ) { innerPadding ->
        InputForm(
            state = state,
            onDiameterChange = viewModel::onDiameterChange,
            onTiltChange = viewModel::onTiltChange,
            onClockingChange = viewModel::onClockingChange,
            onOffsetChange = viewModel::onOffsetChange,
            onPointCountChange = viewModel::onPointCountChange,
            onSaddleEnabledChange = viewModel::onSaddleEnabledChange,
            onPartnerDiameterChange = viewModel::onPartnerDiameterChange,
            onIntersectionAngleChange = viewModel::onIntersectionAngleChange,
            onSaddleClockingChange = viewModel::onSaddleClockingChange,
            onSaddleOffsetChange = viewModel::onSaddleOffsetChange,
            onCalculateClicked = viewModel::onCalculateClicked,
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
        )
    }
}

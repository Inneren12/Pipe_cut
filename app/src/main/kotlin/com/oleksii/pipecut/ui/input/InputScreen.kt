package com.oleksii.pipecut.ui.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oleksii.pipecut.ui.result.ResultTable
import com.oleksii.pipecut.ui.vm.InputViewModel
import com.oleksii.pipecut.ui.vm.ResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    inputViewModel: InputViewModel = viewModel(),
    resultViewModel: ResultViewModel = viewModel(),
) {
    val state by inputViewModel.uiState.collectAsStateWithLifecycle()
    val lastValidRequest by inputViewModel.lastValidRequest.collectAsStateWithLifecycle()
    val resultState by resultViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(lastValidRequest) {
        resultViewModel.submit(lastValidRequest)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(text = "Pipe_cut") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            InputForm(
                state = state,
                onDiameterChange = inputViewModel::onDiameterChange,
                onTiltChange = inputViewModel::onTiltChange,
                onClockingChange = inputViewModel::onClockingChange,
                onOffsetChange = inputViewModel::onOffsetChange,
                onPointCountChange = inputViewModel::onPointCountChange,
                onSaddleEnabledChange = inputViewModel::onSaddleEnabledChange,
                onPartnerDiameterChange = inputViewModel::onPartnerDiameterChange,
                onIntersectionAngleChange = inputViewModel::onIntersectionAngleChange,
                onSaddleClockingChange = inputViewModel::onSaddleClockingChange,
                onSaddleOffsetChange = inputViewModel::onSaddleOffsetChange,
                onCalculateClicked = inputViewModel::onCalculateClicked,
                contentPadding = PaddingValues(16.dp),
            )
            ResultTable(
                state = resultState,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

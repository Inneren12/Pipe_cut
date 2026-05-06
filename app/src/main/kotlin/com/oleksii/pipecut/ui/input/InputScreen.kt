package com.oleksii.pipecut.ui.input

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.oleksii.pipecut.PipeCutApplication
import com.oleksii.pipecut.ui.presets.Preset
import com.oleksii.pipecut.ui.presets.PresetDeleteDialog
import com.oleksii.pipecut.ui.presets.PresetSaveDialog
import com.oleksii.pipecut.ui.presets.PresetsStrings
import com.oleksii.pipecut.ui.presets.presetsBar
import com.oleksii.pipecut.ui.result.resultPanel
import com.oleksii.pipecut.ui.vm.InputViewModel
import com.oleksii.pipecut.ui.vm.PresetsViewModel
import com.oleksii.pipecut.ui.vm.ResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    inputViewModel: InputViewModel = viewModel(),
    resultViewModel: ResultViewModel = viewModel(),
    presetsViewModel: PresetsViewModel = run {
        val app = LocalContext.current.applicationContext as PipeCutApplication
        viewModel(
            factory = viewModelFactory {
                initializer { PresetsViewModel(app.presetsRepository) }
            },
        )
    },
) {
    val state by inputViewModel.uiState.collectAsStateWithLifecycle()
    val lastValidRequest by inputViewModel.lastValidRequest.collectAsStateWithLifecycle()
    val resultState by resultViewModel.uiState.collectAsStateWithLifecycle()
    val presets by presetsViewModel.presets.collectAsStateWithLifecycle()
    val lastSaveError by presetsViewModel.lastSaveError.collectAsStateWithLifecycle()

    LaunchedEffect(lastValidRequest) {
        resultViewModel.submit(lastValidRequest)
    }

    var saveDialogOpen by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("") }
    var deleteCandidate by remember { mutableStateOf<Preset?>(null) }
    val previousCount = remember { mutableStateOf(presets.size) }

    LaunchedEffect(presets.size) {
        if (presets.size > previousCount.value) {
            saveDialogOpen = false
            saveName = ""
        }
        previousCount.value = presets.size
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(text = "Pipe_cut") })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            presetsBar(
                presets = presets,
                onLoad = { preset -> inputViewModel.applyPreset(preset) },
                onSaveClicked = {
                    saveName = ""
                    presetsViewModel.acknowledgeError()
                    saveDialogOpen = true
                },
                onDeleteRequested = { preset -> deleteCandidate = preset },
            )
            item(key = "input-form") {
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
            }
            resultPanel(resultState)
        }
    }

    if (saveDialogOpen) {
        PresetSaveDialog(
            name = saveName,
            onNameChange = {
                saveName = it
                presetsViewModel.acknowledgeError()
            },
            onConfirm = {
                presetsViewModel.trySave(saveName, lastValidRequest)
            },
            onDismiss = {
                saveDialogOpen = false
                presetsViewModel.acknowledgeError()
            },
            errorMessage = lastSaveError?.let { PresetsStrings.message(it) },
        )
    }

    deleteCandidate?.let { preset ->
        PresetDeleteDialog(
            presetName = preset.name,
            onConfirm = {
                presetsViewModel.delete(preset.name)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null },
        )
    }
}


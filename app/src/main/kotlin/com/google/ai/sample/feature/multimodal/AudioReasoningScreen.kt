package com.google.ai.sample.feature.multimodal

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.sample.BuildConfig
import com.google.ai.sample.GenerativeViewModelFactory

@Composable
fun AudioReasoningRoute(
    viewModel: AudioReasoningViewModel = viewModel(factory = GenerativeViewModelFactory)
) {
    val uiState: AudioReasoningUiState by viewModel.uiState.collectAsState()
    AudioReasoningScreen(
        uiState = uiState,
        onProcessAudio = { uri, contentResolver, prompt -> viewModel.processAudio(uri, contentResolver, prompt)

        },
        onPromptChanged = { newPrompt ->
            // Update the prompt in the ViewModel if needed


        },
    )
}


@Composable
fun AudioReasoningScreen(
    uiState: AudioReasoningUiState,
    onPromptChanged: (String) -> Unit = {},
    onProcessAudio: (Uri, ContentResolver, String) -> Unit,
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var textPrompt by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedAudioUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Button to choose an audio file
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch("audio/*")
                } else {
                    launcher.launch("audio/mp3")
                }
            }
        ) {
            Text("Select Audio File")
        }

        // Show the selected audio URI
        if (selectedAudioUri != null) {
            Text(
                text = "Selected Audio URI: $selectedAudioUri",
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Button to process the audio
        Button(
            onClick = {
                if (selectedAudioUri != null && textPrompt.isNotEmpty()) {

                    onProcessAudio(selectedAudioUri!!, contentResolver, textPrompt)
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Process Audio")
        }
        when (uiState) {
            is AudioReasoningUiState.Initial -> Text(
                text = "Initial",
                modifier = Modifier.padding(top = 16.dp)
            )

            is AudioReasoningUiState.Loading -> Text(
                text = "Loading...",
                modifier = Modifier.padding(top = 16.dp)
            )

            is AudioReasoningUiState.Success -> Text(
                text = uiState.content,
                modifier = Modifier.padding(top = 16.dp)
            )

            is AudioReasoningUiState.Error -> Text(
                text = "Error: ${uiState.message}",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
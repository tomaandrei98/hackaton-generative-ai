/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.sample.feature.multimodal

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AudioReasoningViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<AudioReasoningUiState> =
        MutableStateFlow(AudioReasoningUiState.Initial)
    val uiState: StateFlow<AudioReasoningUiState> =
        _uiState.asStateFlow()

    fun reason(
        userInput: String,
        selectedImages: List<Bitmap>
    ) {
        _uiState.value = AudioReasoningUiState.Loading
        val prompt = "Look at the image(s), and then answer the following question: $userInput"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputContent = content {
                    for (bitmap in selectedImages) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                var outputContent = ""

               //TODO call the model here and update UI
                generativeModel.generateContentStream(inputContent)
                    .collect { response ->
                        outputContent += response.text
                        _uiState.value = AudioReasoningUiState.Success(outputContent)
                    }

                generativeModel.generateContentStream()

            } catch (e: Exception) {
                _uiState.value = AudioReasoningUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
    fun processAudio(uri: Uri, contentResolver: ContentResolver, prompt: String) {
        viewModelScope.launch {
            _uiState.update { AudioReasoningUiState.Loading }
            //TODO: Add logic here to process the audio file with the given URI and content resolver.
            //This should also update the uiState
            try {
                val response = "Response from AI, using prompt: $prompt" // This would be an actual response from the AI
                _uiState.update { AudioReasoningUiState.Success(response) }
            } catch (e: Exception) {
                _uiState.update { AudioReasoningUiState.Error(e.message ?: "Unknown error") }
            }
        }
    }


}

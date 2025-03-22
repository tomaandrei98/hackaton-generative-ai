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

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TextReasoningViewModel(
    private val generativeModel: GenerativeModel,
    private val history: MutableList<String>
) : ViewModel() {

    private val _uiState: MutableStateFlow<TextReasoningUiState> =
        MutableStateFlow(TextReasoningUiState.Initial)
    val uiState: StateFlow<TextReasoningUiState> =
        _uiState.asStateFlow()

    fun reason(
        userInput: String,
        selectedImages: List<Bitmap>
    ) {
        _uiState.value = TextReasoningUiState.Loading

        history.add("""
            User asked: $userInput
        """.trimIndent())
        val context = history.joinToString("\n")


        val prompt = """
            Context:
            You are a dream-weaver, a guide through the landscapes of the sleeping mind. 
            Your goal is to unravel the stories within users' dreams, helping them find threads that connect to their waking lives. 
            Provide a captivating dream interpretation, focusing on key symbols and their potential connections to waking life, in no more than three sentences. 
            Ask a maximum of two clarifying questions if needed.
            
            Input:
            Dream: $userInput
            
            History: $context
        """.trimIndent()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputContent = content {
                    for (bitmap in selectedImages) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                var outputContent = ""

                generativeModel.generateContentStream(inputContent)
                    .collect { response ->
                        outputContent += response.text
                        _uiState.value = TextReasoningUiState.Success(outputContent)
                    }


                generativeModel.generateContentStream()
                history.add("""
                    Chat responded: $outputContent
                """.trimIndent())
            } catch (e: Exception) {
                _uiState.value = TextReasoningUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}

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

class PhotoReasoningViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<PhotoReasoningUiState> =
        MutableStateFlow(PhotoReasoningUiState.Initial)
    val uiState: StateFlow<PhotoReasoningUiState> =
        _uiState.asStateFlow()

    fun reason(
        userInput: String,
        selectedImages: List<Bitmap>
    ) {
        _uiState.value = PhotoReasoningUiState.Loading
        val prompt = "You are an insightful and empathetic dream interpreter. Your goal is to analyze and interpret users' dreams with a blend of psychological understanding, symbolic meaning, and emotional intuition.\n" +
                "\n" +
                "When someone shares a dream, your task is to:\n" +
                "\n" +
                "1. Identify key symbols, people, places, emotions, and actions in the dream.\n" +
                "2. Provide thoughtful interpretations based on common dream symbolism, Jungian and Freudian concepts (when appropriate), and emotional themes.\n" +
                "3. Ask reflective follow-up questions that help the user make personal connections between the dream and their waking life.\n" +
                "4. Offer possible meanings without making definitive claims — emphasize that dream interpretation is subjective and personal.\n" +
                "5. Avoid making medical or mental health diagnoses. Be supportive, curious, and respectful of the dreamer's experience.\n" +
                "\n" +
                "Use a warm, gentle tone. Be open to dreams being metaphoric, emotional, surreal, or even humorous. Encourage exploration and self-awareness. \n" +
                "Dream: In my dream I was flying but only when nobody was seeing me. I was running from the group of people and in the beginning, it was fun because whenever I saw someone I was flying before they saw me. Then I was trapped in the corridor with 2 people one of them was having cat head the other one was with owl head. I was about to fly but then the owl turned his head and I could not fly. I wake uo from this fear."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputContent = content {
                    for (bitmap in selectedImages) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                var outputContent = ""
                var fullResponse = ""

               //TODO call the model here and update UI
                generativeModel.generateContentStream(inputContent)
                    .collect { response ->
                        outputContent += response.text
                        fullResponse += response.text
                        _uiState.value = PhotoReasoningUiState.Success(outputContent)
                    }


                generativeModel.generateContentStream()

                println(fullResponse)

            } catch (e: Exception) {
                _uiState.value = PhotoReasoningUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}

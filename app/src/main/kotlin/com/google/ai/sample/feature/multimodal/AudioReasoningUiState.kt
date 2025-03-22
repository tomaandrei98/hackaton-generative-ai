package com.google.ai.sample.feature.multimodal

/**
 * A sealed interface describing the state of the audio reasoning.
 */
sealed interface AudioReasoningUiState {
    /**
     * Empty state when the screen is first shown
     */
    data object Initial : AudioReasoningUiState

    /**
     * Still loading
     */
    data object Loading : AudioReasoningUiState

    /**
     * Text has been generated
     */
    data class Success(val content: String) : AudioReasoningUiState

    /**
     * There was an error generating text
     */
    data class Error(val message: String) : AudioReasoningUiState
}
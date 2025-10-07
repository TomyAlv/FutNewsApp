package com.example.espnapp.ui.results

sealed class ResultsUiState {
    object Idle : ResultsUiState()
    object Loading : ResultsUiState()
    data class Success(val items: List<ScoreItem>) : ResultsUiState()
    data class Empty(val message: String) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}

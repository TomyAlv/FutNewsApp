package com.example.espnapp.ui.news

import com.example.espnapp.model.espn.Article

sealed class NewsUiState {
    object Idle : NewsUiState()
    object Loading : NewsUiState()
    data class Success(val articles: List<Article>) : NewsUiState()
    data class Empty(val message: String) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

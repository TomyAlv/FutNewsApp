package com.example.espnapp.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NewsViewModel(
    private val repo: NewsRepository = NewsRepository()
) : ViewModel() {

    private val _state = MutableLiveData<NewsUiState>(NewsUiState.Idle)
    val state: LiveData<NewsUiState> = _state

    //Today's news from the top leagues.
    fun loadTodaySoccerNews(leagues: List<String>, tz: ZoneId) {
        _state.postValue(NewsUiState.Loading)

        repo.getLeaguesNews(leagues) { result ->
            result.onSuccess { all ->
                val today = LocalDate.now(tz)
                val fmt = DateTimeFormatter.ISO_DATE_TIME

                val todayNews = all.filter { a ->
                    a.published?.let {
                        try {
                            val date = OffsetDateTime.parse(it, fmt)
                                .toInstant().atZone(tz).toLocalDate()
                            date == today
                        } catch (_: Exception) { false }
                    } ?: false
                }
                    .distinctBy { it.headline?.trim()?.lowercase() }
                    .sortedByDescending { it.published }

                if (todayNews.isEmpty()) {
                    _state.postValue(NewsUiState.Empty("No hay noticias de hoy."))
                } else {
                    _state.postValue(NewsUiState.Success(todayNews))
                }
            }.onFailure {
                _state.postValue(NewsUiState.Error("Error cargando noticias"))
            }
        }
    }
}

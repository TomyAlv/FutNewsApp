package com.example.espnapp.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SportNewsViewModel(
    private val repo: NewsRepository = NewsRepository(),
    private val tz: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")
) : ViewModel() {

    private val _state = MutableLiveData<NewsUiState>(NewsUiState.Idle)
    val state: LiveData<NewsUiState> = _state

    private val iso = DateTimeFormatter.ISO_DATE_TIME
    private val pretty = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun loadForSport(candidates: List<String>, emptyLabel: String) {
        _state.postValue(NewsUiState.Loading)

        repo.getNewsFromCandidates(candidates) { result ->
            result.onSuccess { raw ->
                val mapped = raw
                    .sortedByDescending { it.published }
                    .map {
                        it.copy(published = it.published?.let { s ->
                            try {
                                val odt = OffsetDateTime.parse(s, iso)
                                pretty.format(odt.atZoneSameInstant(tz))
                            } catch (_: Exception) { s }
                        })
                    }

                if (mapped.isEmpty()) {
                    _state.postValue(NewsUiState.Empty("No hay noticias de $emptyLabel."))
                } else {
                    _state.postValue(NewsUiState.Success(mapped))
                }
            }.onFailure {
                _state.postValue(NewsUiState.Error("Error cargando noticias de $emptyLabel"))
            }
        }
    }
}

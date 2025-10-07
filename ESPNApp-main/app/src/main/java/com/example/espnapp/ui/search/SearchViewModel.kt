package com.example.espnapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.espnapp.model.espn.Team
import com.example.espnapp.network.TeamsResponse
import com.example.espnapp.network.espnApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val teams: List<Team>) : SearchUiState()
    data class Empty(val message: String) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel : ViewModel() {

    private val _state = MutableLiveData<SearchUiState>(SearchUiState.Idle)
    val state: LiveData<SearchUiState> = _state

    private val currentCalls = mutableListOf<Call<TeamsResponse>>()

    // Leagues to search in
    private val leagues = listOf("eng.1", "esp.1", "ita.1", "ger.1", "fra.1", "arg.1")

    fun search(query: String) {
        val q = query.trim()
        if (q.isEmpty()) {
            _state.value = SearchUiState.Empty("Escribí un equipo para buscar")
            return
        }

        // Cancel any in-flight requests
        currentCalls.forEach { it.cancel() }
        currentCalls.clear()

        _state.value = SearchUiState.Loading

        val buffer = mutableListOf<Team>()
        var pending = leagues.size

        fun finishIfDone() {
            pending--
            if (pending == 0) {
                val list = buffer
                    .distinctBy { (it.displayName ?: it.abbreviation ?: "").lowercase() }
                    .sortedBy { (it.displayName ?: it.abbreviation ?: "").lowercase() }

                _state.value = if (list.isEmpty())
                    SearchUiState.Empty("Sin resultados para \"$q\"")
                else
                    SearchUiState.Success(list)
            }
        }

        leagues.forEach { lg ->
            val call = espnApi.getLeagueTeams(lg)
            currentCalls += call
            call.enqueue(object : Callback<TeamsResponse> {
                override fun onResponse(call: Call<TeamsResponse>, response: Response<TeamsResponse>) {
                    response.body()?.sports
                        ?.firstOrNull()
                        ?.leagues?.firstOrNull()
                        ?.teams.orEmpty()
                        .mapNotNull { it.team }
                        .filter { t ->
                            val name = (t.displayName ?: "") + " " +
                                    (t.shortDisplayName ?: "") + " " +
                                    (t.abbreviation ?: "")
                            name.lowercase().contains(q.lowercase())
                        }
                        .forEach { buffer += it }

                    finishIfDone()
                }

                override fun onFailure(call: Call<TeamsResponse>, t: Throwable) {
                    finishIfDone() // continue with the remaining leagues
                }
            })
        }
    }

    override fun onCleared() {
        currentCalls.forEach { it.cancel() }
        currentCalls.clear()
        super.onCleared()
    }
}

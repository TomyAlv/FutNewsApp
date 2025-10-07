package com.example.espnapp.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.espnapp.model.espn.Competition
import com.example.espnapp.model.espn.ScoreboardResponse
import com.example.espnapp.model.espn.Status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ResultsViewModel(
    private val repo: ResultsRepository = ResultsRepository()
) : ViewModel() {

    private val _state = MutableLiveData<ResultsUiState>(ResultsUiState.Idle)
    val state: LiveData<ResultsUiState> = _state

    private val tz: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")
    private val inputIso = DateTimeFormatter.ISO_DATE_TIME
    private val hourFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val ymdFmt = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun loadToday(leagues: Map<String, String>) {
        _state.postValue(ResultsUiState.Loading)
        val utcToday = LocalDate.now(ZoneOffset.UTC).format(ymdFmt)

        repo.getManyLeaguesScoreboards(leagues, utcToday) { result ->
            result.onSuccess { map ->
                val items = mutableListOf<ScoreItem>()
                // code -> body, we need the title:
                map.forEach { (code, body) ->
                    val title = leagues[code] ?: code
                    val mapped = body?.let { mapToItems(code, title, it) }.orEmpty()
                    if (mapped.isNotEmpty()) items += mapped
                }
                if (items.isEmpty()) {
                    _state.postValue(ResultsUiState.Empty("No hay partidos para hoy."))
                } else {
                    _state.postValue(ResultsUiState.Success(items))
                }
            }.onFailure {
                _state.postValue(ResultsUiState.Error("Error cargando resultados"))
            }
        }
    }

    // ---- mapping helpers (they used to be in the Fragment) ----
    private fun mapToItems(code: String, title: String, body: ScoreboardResponse): List<ScoreItem> {
        val events = buildList {
            addAll(body.events.orEmpty())
            body.leagues?.forEach { addAll(it.events.orEmpty()) }
        }
        val pairs = events.mapNotNull { ev ->
            val comp: Competition = ev.competitions?.firstOrNull() ?: return@mapNotNull null
            val meta = formatMeta(comp.status, ev.date)
            comp to meta
        }
        if (pairs.isEmpty()) return emptyList()
        val out = mutableListOf<ScoreItem>()
        out += ScoreItem.Section(code, title)
        pairs.forEach { (c, meta) -> out += ScoreItem.Match(c, meta) }
        return out
    }

    private fun formatMeta(status: Status?, isoDate: String?): String {
        val state = status?.type?.state
        return when (state) {
            "pre" -> {
                try {
                    val odt = OffsetDateTime.parse(isoDate, inputIso)
                    hourFmt.format(odt.atZoneSameInstant(tz))
                } catch (_: Exception) {
                    status?.type?.shortDetail ?: "Programado"
                }
            }
            "in" -> {
                val clock = status?.displayClock?.takeIf { it.isNotBlank() } ?: ""
                if (clock.isNotEmpty()) "EN VIVO • $clock" else "EN VIVO"
            }
            "post" -> "FINAL"
            else -> status?.type?.shortDetail ?: ""
        }
    }
}

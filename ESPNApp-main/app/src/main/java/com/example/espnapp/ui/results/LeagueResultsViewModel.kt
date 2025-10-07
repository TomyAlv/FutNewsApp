package com.example.espnapp.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.espnapp.model.espn.ScoreboardResponse
import com.example.espnapp.model.espn.Status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class LeagueResultsViewModel(
    private val repo: ResultsRepository = ResultsRepository()
) : ViewModel() {

    private val _state = MutableLiveData<ResultsUiState>(ResultsUiState.Idle)
    val state: LiveData<ResultsUiState> = _state

    private val _selectedDateUtc = MutableLiveData(LocalDate.now(ZoneOffset.UTC))
    val selectedDateUtc: LiveData<LocalDate> = _selectedDateUtc

    private val tz: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")
    private val inputIso = DateTimeFormatter.ISO_DATE_TIME
    private val hourFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val ymdFmt = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val ddMMyyyyFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun setDateUtc(date: LocalDate) { _selectedDateUtc.value = date }

    fun load(leagueCode: String, dateUtc: LocalDate) {
        _state.postValue(ResultsUiState.Loading)
        val yyyymmdd = dateUtc.format(ymdFmt)

        repo.getLeagueScoreboard(leagueCode, yyyymmdd) { result ->
            result.onSuccess { body ->
                val items = mapToItems(leagueCode, "", body) // the Fragment supplies the title
                if (items.isEmpty()) {
                    _state.postValue(
                        ResultsUiState.Empty("No hay partidos para ${dateUtc.format(ddMMyyyyFmt)}")
                    )
                } else {
                    _state.postValue(ResultsUiState.Success(items))
                }
            }.onFailure {
                _state.postValue(ResultsUiState.Error("Error cargando resultados"))
            }
        }
    }

    private fun mapToItems(code: String, title: String, body: ScoreboardResponse?): List<ScoreItem> {
        if (body == null) return emptyList()
        val events = buildList {
            addAll(body.events.orEmpty())
            body.leagues?.forEach { addAll(it.events.orEmpty()) }
        }
        val pairs = events.mapNotNull { ev ->
            val comp = ev.competitions?.firstOrNull() ?: return@mapNotNull null
            val meta = formatMeta(comp.status, ev.date)
            comp to meta
        }
        val out = mutableListOf<ScoreItem>()
        if (pairs.isNotEmpty()) {
            // the Fragment can complete the Section with the title if you prefer
            out += ScoreItem.Section(code, title)
            pairs.forEach { (c, meta) -> out += ScoreItem.Match(c, meta) }
        }
        return out
    }

    private fun formatMeta(status: Status?, isoDate: String?): String {
        val state = status?.type?.state
        return when (state) {
            "pre" -> {
                try {
                    val odt = OffsetDateTime.parse(isoDate, inputIso)
                    "${hourFmt.format(odt.atZoneSameInstant(tz))} hs"
                } catch (_: Exception) {
                    status?.type?.shortDetail ?: "Programado"
                }
            }
            "in" -> {
                val clock = status?.displayClock?.takeIf { !it.isNullOrBlank() } ?: ""
                if (clock.isNotEmpty()) "EN VIVO • $clock" else "EN VIVO"
            }
            "post" -> "FINAL"
            else -> status?.type?.shortDetail ?: "Por definir"
        }
    }
}

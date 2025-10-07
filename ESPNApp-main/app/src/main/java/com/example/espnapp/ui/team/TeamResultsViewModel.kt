package com.example.espnapp.ui.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.espnapp.model.espn.Competition
import com.example.espnapp.model.espn.ScoreboardResponse
import com.example.espnapp.model.espn.Status
import com.example.espnapp.ui.results.ResultsRepository
import com.example.espnapp.ui.results.ResultsUiState
import com.example.espnapp.ui.results.ScoreItem
import java.text.Normalizer
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TeamResultsViewModel(
    private val repo: ResultsRepository = ResultsRepository()
) : ViewModel() {

    private val _state = MutableLiveData<ResultsUiState>(ResultsUiState.Idle)
    val state: LiveData<ResultsUiState> = _state

    private val tz: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")
    private val inputIso = DateTimeFormatter.ISO_DATE_TIME
    private val hourFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val ymdFmt = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val ddMMyyyyFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun loadForTeam(
        leagues: Map<String, String>,        // code -> title
        dateUtc: LocalDate,
        teamAbbr: String?,
        teamName: String?
    ) {
        _state.postValue(ResultsUiState.Loading)
        val yyyymmdd = dateUtc.format(ymdFmt)

        val keyAbbr = teamAbbr?.trim().orEmpty()
        val keyName = teamName?.trim().orEmpty()

        repo.getManyLeaguesScoreboards(leagues, yyyymmdd) { result ->
            result.onSuccess { map ->
                val items = mutableListOf<ScoreItem>()

                map.forEach { (leagueCode, body) ->
                    val title = leagues[leagueCode] ?: leagueCode
                    val leagueItems = body?.let { filterLeagueForTeam(it, keyAbbr, keyName) }.orEmpty()
                    if (leagueItems.isNotEmpty()) {
                        items += ScoreItem.Section(leagueCode, title)
                        items += leagueItems
                    }
                }

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

    private fun filterLeagueForTeam(
        body: ScoreboardResponse,
        keyAbbr: String,
        keyName: String
    ): List<ScoreItem.Match> {
        val matches = mutableListOf<ScoreItem.Match>()

        val events = buildList {
            addAll(body.events.orEmpty())
            body.leagues?.forEach { addAll(it.events.orEmpty()) }
        }

        for (ev in events) {
            val comp: Competition = ev.competitions?.firstOrNull() ?: continue
            val hasTeam = comp.competitors?.any { compo ->
                val t = compo.team
                val abbr = t?.abbreviation.orEmpty()
                val short = t?.shortDisplayName.orEmpty()
                val name = t?.displayName.orEmpty()

                // "Friendly" comparisons
                fun norm(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD)
                    .replace(Regex("\\p{Mn}+"), "")
                    .lowercase()

                val hitAbbr = keyAbbr.isNotBlank() && abbr.equals(keyAbbr, ignoreCase = true)
                val hitName = keyName.isNotBlank() &&
                        (norm(short).contains(norm(keyName)) || norm(name).contains(norm(keyName)))
                hitAbbr || hitName
            } ?: false

            if (hasTeam) {
                val meta = formatMeta(comp.status, ev.date)
                matches += ScoreItem.Match(comp, meta)
            }
        }
        return matches
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

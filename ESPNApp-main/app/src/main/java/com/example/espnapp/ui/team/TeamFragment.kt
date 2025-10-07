// TeamFragment.kt
package com.example.espnapp.ui.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.bumptech.glide.Glide
import com.example.espnapp.databinding.FragmentTeamBinding
import com.example.espnapp.model.espn.Team
import com.example.espnapp.ui.results.DateStripAdapter
import com.example.espnapp.ui.results.ResultsAdapter
import com.example.espnapp.ui.results.ResultsUiState
import com.example.espnapp.ui.results.ScoreItem
import com.google.gson.Gson
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TeamFragment : Fragment() {

    private var _b: FragmentTeamBinding? = null
    private val b get() = _b!!

    private val vm: TeamResultsViewModel by viewModels()

    private lateinit var dateAdapter: DateStripAdapter
    private lateinit var resultsAdapter: ResultsAdapter

    // same leagues used in Search/Results
    private val leagues = mapOf(
        "arg.1" to "Liga Profesional Argentina",
        "eng.1" to "Premier League",
        "esp.1" to "LaLiga",
        "ita.1" to "Serie A",
        "ger.1" to "Bundesliga",
        "fra.1" to "Ligue 1"
    )

    private var selectedUtcDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private val PAST_DAYS = 7
    private val FUTURE_DAYS = 30
    private val ddMMyyyyFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private var teamAbbr: String? = null
    private var teamName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentTeamBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ---- header data
        val nameFromArgs = requireArguments().getString("teamName").orEmpty()
        val logo = requireArguments().getString("teamLogoUrl")
        val teamJson = requireArguments().getString("teamJson")
        val team = teamJson?.let { runCatching { Gson().fromJson(it, Team::class.java) }.getOrNull() }

        b.tvName.text = nameFromArgs
        if (!logo.isNullOrBlank()) Glide.with(this).load(logo).into(b.ivLogo)

        teamAbbr = requireArguments().getString("teamAbbr") ?: team?.abbreviation
        teamName = team?.shortDisplayName ?: team?.displayName ?: nameFromArgs

        // ---- date strip
        dateAdapter = DateStripAdapter { picked ->
            dateAdapter.setSelected(picked)
            selectedUtcDate = picked
            vm.loadForTeam(leagues, picked, teamAbbr, teamName)
            scrollDateTo(selectedUtcDate)
        }
        b.rvDateStripTeam.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
            LinearSnapHelper().attachToRecyclerView(this)
            clipToPadding = false
            setPadding(24, paddingTop, 24, paddingBottom)
        }
        val todayUtc = LocalDate.now(ZoneOffset.UTC)
        val start = todayUtc.minusDays(PAST_DAYS.toLong())
        val end = todayUtc.plusDays(FUTURE_DAYS.toLong())
        val range = generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
        dateAdapter.submit(range, initiallySelected = selectedUtcDate)

        // ---- match list
        resultsAdapter = ResultsAdapter(onSeeAll = { _, _ -> /* no-op on the team view */ })
        b.rvTeamResults.layoutManager = LinearLayoutManager(requireContext())
        b.rvTeamResults.adapter = resultsAdapter

        // ---- observe state
        vm.state.observe(viewLifecycleOwner) { st ->
            when (st) {
                is ResultsUiState.Loading -> {
                    b.progressTeam.isVisible = true
                    b.tvEmptyTeam.isVisible = false
                }
                is ResultsUiState.Success -> {
                    b.progressTeam.isVisible = false
                    b.tvEmptyTeam.isVisible = false
                    // if the first item is not a section, add one for neatness
                    val withSection = if (st.items.isNotEmpty() && st.items.first() !is ScoreItem.Section) {
                        listOf(ScoreItem.Section("", "Partidos")) + st.items
                    } else st.items
                    resultsAdapter.submit(withSection)
                }
                is ResultsUiState.Empty -> {
                    b.progressTeam.isVisible = false
                    b.tvEmptyTeam.text = st.message
                    b.tvEmptyTeam.isVisible = true
                    resultsAdapter.submit(emptyList())
                }
                is ResultsUiState.Error -> {
                    b.progressTeam.isVisible = false
                    b.tvEmptyTeam.text = st.message
                    b.tvEmptyTeam.isVisible = true
                    resultsAdapter.submit(emptyList())
                }
                ResultsUiState.Idle -> Unit
            }
        }

        // ---- initial load
        vm.loadForTeam(leagues, selectedUtcDate, teamAbbr, teamName)
        b.rvDateStripTeam.post { scrollDateTo(selectedUtcDate) }
    }

    private fun scrollDateTo(date: LocalDate) {
        val lm = b.rvDateStripTeam.layoutManager as? LinearLayoutManager ?: return
        val pos = dateAdapter.indexOf(date)
        if (pos >= 0) lm.scrollToPositionWithOffset(pos, b.rvDateStripTeam.width / 2)
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

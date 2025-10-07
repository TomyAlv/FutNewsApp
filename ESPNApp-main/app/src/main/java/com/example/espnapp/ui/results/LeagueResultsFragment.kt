package com.example.espnapp.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.espnapp.databinding.FragmentLeagueResultsBinding
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class LeagueResultsFragment : Fragment() {

    private var _binding: FragmentLeagueResultsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeagueResultsViewModel by viewModels()

    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var dateAdapter: DateStripAdapter

    private val leagueCode by lazy { requireArguments().getString("leagueCode") ?: "eng.1" }
    private val leagueTitle by lazy { requireArguments().getString("leagueTitle") ?: "Premier League" }

    private val ddMMyyyyFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private var selectedUtcDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private val PAST_DAYS = 7
    private val FUTURE_DAYS = 30

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeagueResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = "$leagueTitle • Resultados"

        /// Horizontal strip
        dateAdapter = DateStripAdapter { picked ->
            dateAdapter.setSelected(picked)
            selectedUtcDate = picked
            viewModel.setDateUtc(picked)
            viewModel.load(leagueCode, picked)
            scrollDateTo(selectedUtcDate)
        }
        binding.rvDateStrip.apply {
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

        // Match list
        resultsAdapter = ResultsAdapter(onSeeAll = { _, _ -> })
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = resultsAdapter

        observeState()

        // Initial load
        viewModel.setDateUtc(selectedUtcDate)
        viewModel.load(leagueCode, selectedUtcDate)
        binding.rvDateStrip.post { scrollDateTo(selectedUtcDate) }

        // Edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, status.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { st ->
            when (st) {
                is ResultsUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                }
                is ResultsUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                    // Insert a section with the title (the ViewModel does not know it)
                    val withSection = if (st.items.isNotEmpty() && st.items.first() !is ScoreItem.Section) {
                        listOf(ScoreItem.Section(leagueCode, leagueTitle)) + st.items
                    } else st.items
                    resultsAdapter.submit(withSection)
                }
                is ResultsUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = st.message
                    binding.tvEmpty.visibility = View.VISIBLE
                    resultsAdapter.submit(emptyList())
                }
                is ResultsUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = st.message
                    binding.tvEmpty.visibility = View.VISIBLE
                    resultsAdapter.submit(emptyList())
                }
                ResultsUiState.Idle -> { /* no-op */ }
            }
        }
    }

    private fun scrollDateTo(date: LocalDate) {
        val lm = binding.rvDateStrip.layoutManager as? LinearLayoutManager ?: return
        val pos = dateAdapter.indexOf(date)
        if (pos >= 0) lm.scrollToPositionWithOffset(pos, binding.rvDateStrip.width / 2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

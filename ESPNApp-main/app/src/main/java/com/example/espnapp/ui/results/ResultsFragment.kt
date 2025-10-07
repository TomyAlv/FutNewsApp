package com.example.espnapp.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.espnapp.R
import com.example.espnapp.databinding.FragmentResultsBinding

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResultsViewModel by viewModels()
    private lateinit var adapter: ResultsAdapter

    private val leagues = mapOf(
        "arg.1" to "Liga Profesional Argentina",
        "eng.1" to "Premier League",
        "esp.1" to "LaLiga",
        "ita.1" to "Serie A",
        "ger.1" to "Bundesliga",
        "fra.1" to "Ligue 1"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ResultsAdapter { leagueCode, leagueTitle ->
            val args = Bundle().apply {
                putString("leagueCode", leagueCode)
                putString("leagueTitle", leagueTitle)
            }
            findNavController().navigate(R.id.leagueResultsFragment, args)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        observeState()
        viewModel.loadToday(leagues)

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
                    adapter.submit(st.items)
                }
                is ResultsUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = st.message
                    binding.tvEmpty.visibility = View.VISIBLE
                    adapter.submit(emptyList())
                }
                is ResultsUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = st.message
                    binding.tvEmpty.visibility = View.VISIBLE
                    adapter.submit(emptyList())
                }
                ResultsUiState.Idle -> { /* no-op */ }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

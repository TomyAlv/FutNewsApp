package com.example.espnapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.espnapp.R
import com.example.espnapp.databinding.FragmentSearchBinding
import com.example.espnapp.model.espn.Team
import com.google.gson.Gson


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: SearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SearchAdapter { team -> openTeam(team) }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnSearch.setOnClickListener {
            viewModel.search(binding.etSearch.text.toString())
        }

        /// Observe the ViewModel state
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    adapter.submit(emptyList())
                }
                is SearchUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    adapter.submit(emptyList())
                }
                is SearchUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    adapter.submit(state.teams)
                }
                is SearchUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                    adapter.submit(emptyList())
                }
                is SearchUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                    adapter.submit(emptyList())
                }
            }
        }
    }

    private fun openTeam(team: Team) {
        val args = Bundle().apply {
            putString("teamJson", Gson().toJson(team))
            putString("teamName", team.displayName ?: team.shortDisplayName ?: team.abbreviation)
            putString("teamLogoUrl", team.logos?.firstOrNull()?.href)
            putString("teamAbbr", team.abbreviation)
        }
        findNavController().navigate(R.id.teamFragment, args)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

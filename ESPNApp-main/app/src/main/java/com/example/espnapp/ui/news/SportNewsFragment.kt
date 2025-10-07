package com.example.espnapp.ui.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.espnapp.databinding.FragmentNewsBinding

class SportNewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SportNewsViewModel by viewModels()
    private lateinit var adapter: NewsAdapter

    private val sportKey by lazy { requireArguments().getString("sportKey") ?: "soccer" }
    private val sportTitle by lazy { requireArguments().getString("sportTitle") ?: "Noticias" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = sportTitle
        setupRecycler()
        observeState()
        viewModel.loadForSport(endpointsForSport(sportKey), sportTitle)
    }

    private fun setupRecycler() {
        adapter = NewsAdapter { article ->
            val url = article.links?.web?.href ?: article.links?.mobile?.href
            if (!url.isNullOrBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } else {
                Toast.makeText(requireContext(), "Sin enlace disponible", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { st ->
            when (st) {
                is NewsUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is NewsUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    adapter.submit(st.articles)
                }
                is NewsUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = st.message
                    binding.tvError.visibility = View.VISIBLE
                    adapter.submit(emptyList())
                }
                is NewsUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = st.message
                    binding.tvError.visibility = View.VISIBLE
                    adapter.submit(emptyList())
                }
                NewsUiState.Idle -> { /* no-op */ }
            }
        }
    }

    private fun endpointsForSport(key: String): List<String> = when (key) {
        "soccer" -> listOf(
            "apis/site/v2/sports/soccer/news",
            "apis/site/v2/sports/soccer/fifa.world/news"
        )
        "basketball" -> listOf(
            "apis/site/v2/sports/basketball/nba/news",
            "apis/site/v2/sports/basketball/mens-college-basketball/news"
        )
        "football" -> listOf(
            "apis/site/v2/sports/football/nfl/news",
            "apis/site/v2/sports/football/college-football/news"
        )
        "baseball" -> listOf("apis/site/v2/sports/baseball/mlb/news")
        "hockey" -> listOf("apis/site/v2/sports/hockey/nhl/news")
        "tennis" -> listOf(
            "apis/site/v2/sports/tennis/news",
            "apis/site/v2/sports/tennis/atp/news",
            "apis/site/v2/sports/tennis/wta/news"
        )
        "mma" -> listOf(
            "apis/site/v2/sports/mma/news",
            "apis/site/v2/sports/mma/ufc/news",
            "apis/site/v2/sports/boxing/news"
        )
        "f1" -> listOf(
            "apis/site/v2/sports/racing/f1/news",
            "apis/site/v2/sports/racing/news"
        )
        else -> listOf("apis/site/v2/sports/$key/news")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

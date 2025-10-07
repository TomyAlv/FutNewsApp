package com.example.espnapp.ui.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.espnapp.databinding.FragmentNewsBinding
import java.time.ZoneId

class NewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsViewModel by viewModels()
    private lateinit var adapter: NewsAdapter

    private val leagues = listOf("eng.1", "esp.1", "ita.1", "ger.1", "fra.1", "arg.1")
    private val tz: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        observeState()
        viewModel.loadTodaySoccerNews(leagues, tz)
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
                NewsUiState.Idle -> Unit
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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

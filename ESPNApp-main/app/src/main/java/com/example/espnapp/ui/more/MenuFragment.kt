package com.example.espnapp.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.espnapp.R
import com.example.espnapp.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels()
    private lateinit var adapter: SportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SportsAdapter { sport ->
            val args = bundleOf(
                "sportKey" to sport.key,
                "sportTitle" to sport.title
            )
            findNavController().navigate(R.id.sportNewsFragment, args)
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MenuFragment.adapter
        }

        // Observe the list from the ViewModel
        viewModel.sports.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        // If you add a search box in the future:
        // binding.searchEdit.doAfterTextChanged { viewModel.filter(it?.toString())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

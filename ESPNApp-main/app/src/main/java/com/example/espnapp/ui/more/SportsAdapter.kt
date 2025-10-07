package com.example.espnapp.ui.more

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.espnapp.databinding.ItemSportRowBinding

class SportsAdapter(
    private val onClick: (SportItem) -> Unit
) : ListAdapter<SportItem, SportsAdapter.SportVH>(Diff) {

    object Diff : DiffUtil.ItemCallback<SportItem>() {
        override fun areItemsTheSame(oldItem: SportItem, newItem: SportItem) =
            oldItem.key == newItem.key
        override fun areContentsTheSame(oldItem: SportItem, newItem: SportItem) =
            oldItem == newItem
    }

    inner class SportVH(private val binding: ItemSportRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SportItem) {
            binding.tvSportName.text = item.title
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportVH {
        val inf = LayoutInflater.from(parent.context)
        return SportVH(ItemSportRowBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: SportVH, position: Int) {
        holder.bind(getItem(position))
    }
}

package com.example.espnapp.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.espnapp.databinding.ItemTeamBinding
import com.example.espnapp.model.espn.Team

// Adapter kept simple for beginners: full replace + notifyDataSetChanged
class SearchAdapter(
    private val onClick: (Team) -> Unit
) : RecyclerView.Adapter<SearchAdapter.VH>() {

    private val items = mutableListOf<Team>()

    fun submit(list: List<Team>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemTeamBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: Team) {
            b.tvName.text = t.displayName ?: t.shortDisplayName ?: t.abbreviation ?: "Team"
            val logo = t.logos?.firstOrNull()?.href
            if (!logo.isNullOrBlank()) {
                Glide.with(b.root).load(logo).into(b.ivLogo)
            } else {
                b.ivLogo.setImageDrawable(null)
            }
            b.root.setOnClickListener { onClick(t) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemTeamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(vb)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}

package com.example.espnapp.ui.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.espnapp.databinding.ItemMatchRowBinding
import com.example.espnapp.databinding.ItemScoreSectionBinding
import com.example.espnapp.model.espn.Competition
import com.example.espnapp.model.espn.Competitor

// ---- MODELS ----
sealed class ScoreItem {
    data class Section(val leagueCode: String, val leagueTitle: String) : ScoreItem()
    data class Match(val comp: Competition, val meta: String) : ScoreItem()
}

// ---- ADAPTER ----
class ResultsAdapter(
    private val onSeeAll: (leagueCode: String, leagueTitle: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<ScoreItem>()

    fun submit(items: List<ScoreItem>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is ScoreItem.Section -> 0
        is ScoreItem.Match -> 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SectionVH(
                ItemScoreSectionBinding.inflate(inflater, parent, false),
                onSeeAll
            )
            else -> MatchVH(ItemMatchRowBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = data[position]) {
            is ScoreItem.Section -> (holder as SectionVH).bind(item)
            is ScoreItem.Match -> (holder as MatchVH).bind(item.comp, item.meta)
        }
    }

    // ---- SECTION ----
    class SectionVH(
        private val b: ItemScoreSectionBinding,
        private val onSeeAll: (String, String) -> Unit
    ) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ScoreItem.Section) {
            b.tvLeague.text = item.leagueTitle
            b.tvSeeAll.setOnClickListener { onSeeAll(item.leagueCode, item.leagueTitle) }
        }
    }

    // ---- MATCH ----
    class MatchVH(private val b: ItemMatchRowBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(c: Competition, meta: String) {
            val home = c.competitors?.firstOrNull { it.homeAway == "home" }
            val away = c.competitors?.firstOrNull { it.homeAway == "away" }
            bindTeam(b, home, isHome = true)
            bindTeam(b, away, isHome = false)

            b.tvMeta.text = meta
        }

        private fun bindTeam(b: ItemMatchRowBinding, comp: Competitor?, isHome: Boolean) {
            val teamName = comp?.team?.shortDisplayName ?: comp?.team?.displayName ?: "—"
            val score = comp?.score ?: ""

            if (isHome) {
                b.tvHomeName.text = teamName
                b.tvHomeScore.text = score
                Glide.with(b.root).load(comp?.team?.logos?.firstOrNull()?.href).into(b.ivHome)
            } else {
                b.tvAwayName.text = teamName
                b.tvAwayScore.text = score
                Glide.with(b.root).load(comp?.team?.logos?.firstOrNull()?.href).into(b.ivAway)
            }
        }
    }
}

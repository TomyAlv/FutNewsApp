// ui/news/NewsAdapter.kt
package com.example.espnapp.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.espnapp.databinding.ItemNewsBinding
import com.example.espnapp.model.espn.Article
import com.squareup.picasso.Picasso

class NewsAdapter(
    private val onClick: (Article) -> Unit
) : RecyclerView.Adapter<NewsAdapter.VH>() {

    private val data = mutableListOf<Article>()

    fun submit(list: List<Article>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemNewsBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(a: Article) {
            b.tvHeadline.text = a.headline ?: "(Sin título)"
            b.tvDate.text = (a.published ?: "").replace('T', ' ').replace('Z', ' ')
            val img = a.images?.firstOrNull { !it.url.isNullOrBlank() }?.url
            if (!img.isNullOrBlank()) {
                Picasso.get().load(img).into(b.imgThumb)
            } else {
                b.imgThumb.setImageDrawable(null)
            }
            b.root.setOnClickListener { onClick(a) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(data[position])
}

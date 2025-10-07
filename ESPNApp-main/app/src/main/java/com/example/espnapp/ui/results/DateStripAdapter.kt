package com.example.espnapp.ui.results

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.espnapp.databinding.ItemDateDayBinding
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateStripAdapter(
    private val onDateSelected: (LocalDate) -> Unit
) : RecyclerView.Adapter<DateStripAdapter.VH>() {

    private val items = mutableListOf<LocalDate>()
    private var selected: LocalDate = LocalDate.now(ZoneOffset.UTC)

    private val dowFmt   = DateTimeFormatter.ofPattern("EEE", Locale("es"))
    private val monthFmt = DateTimeFormatter.ofPattern("MMM d", Locale("es"))

    fun submit(range: List<LocalDate>, initiallySelected: LocalDate) {
        items.clear()
        items.addAll(range)
        selected = initiallySelected
        notifyDataSetChanged()
    }

    fun setSelected(date: LocalDate) {
        val old = selected
        selected = date
        val oldPos = items.indexOf(old)
        val newPos = items.indexOf(selected)
        if (oldPos >= 0) notifyItemChanged(oldPos)
        if (newPos >= 0) notifyItemChanged(newPos)
    }

    fun indexOf(date: LocalDate): Int = items.indexOf(date)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemDateDayBinding.inflate(inf, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], items[position] == selected, onDateSelected)
    }

    class VH(private val b: ItemDateDayBinding) : RecyclerView.ViewHolder(b.root) {

        private val white = Color.WHITE
        private val defaultText = b.tvDow.currentTextColor

        private fun up(s: String) = s.uppercase(Locale("es"))

        fun bind(date: LocalDate, isSelected: Boolean, onClick: (LocalDate) -> Unit) {
            b.tvDow.text      = up(date.format(DateTimeFormatter.ofPattern("EEE", Locale("es"))))
            b.tvDayMonth.text = up(date.format(DateTimeFormatter.ofPattern("MMM d", Locale("es"))))

            b.root.isSelected = isSelected
            val color = if (isSelected) white else defaultText
            b.tvDow.setTextColor(color)
            b.tvDayMonth.setTextColor(color)

            b.root.setOnClickListener { onClick(date) }
        }
    }
}

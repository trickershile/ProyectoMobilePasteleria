package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ejemploprueba.databinding.ItemBannerBinding

data class PromoBanner(val imageUrl: String?, val text: String)

class BannerAdapter : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {
    private val items = mutableListOf<PromoBanner>()
    fun submit(list: List<PromoBanner>) { items.clear(); items.addAll(list); notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    class ViewHolder(private val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PromoBanner) {
            binding.tvBannerText.text = item.text
            val safe = item.imageUrl?.takeIf { it.isNotBlank() }
            Glide.with(itemView.context)
                .load(safe ?: android.R.drawable.ic_menu_report_image)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivBanner)
        }
    }
}

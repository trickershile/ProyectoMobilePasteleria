package com.example.ejemploprueba.Ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.ejemploprueba.Model.Producto
import com.example.ejemploprueba.databinding.ItemProductBinding

class ProductoAdapter(
    private val onProductClick: (Producto) -> Unit,
    private val onAddToCart: ((Producto, android.view.View) -> Unit)? = null
) : ListAdapter<Producto, ProductoAdapter.ViewHolder>(DiffCallback()) {
    var topIds: Set<Int> = emptySet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onProductClick, onAddToCart)

    inner class ViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(producto: Producto, onClick: (Producto) -> Unit, onAddCart: ((Producto, android.view.View) -> Unit)?) {
            binding.tvProductName.text = producto.nombre
            binding.tvProductPrice.text = "$${producto.precio}"
            try { binding.shimmerContainer.startShimmer() } catch (_: Exception) {}
            val src = producto.imagen.takeIf { it.isNotBlank() && isLoadableImage(it) } ?: android.R.drawable.ic_menu_report_image
            Glide.with(itemView.context)
                .load(src)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        try { binding.shimmerContainer.stopShimmer(); binding.shimmerContainer.visibility = android.view.View.GONE } catch (_: Exception) {}
                        return false
                    }
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        try { binding.shimmerContainer.stopShimmer(); binding.shimmerContainer.visibility = android.view.View.GONE } catch (_: Exception) {}
                        return false
                    }
                })
                .into(binding.ivProductImage)

            binding.tvBadge.visibility = if (topIds.contains(producto.id)) android.view.View.VISIBLE else android.view.View.GONE

            binding.root.setOnClickListener { onClick(producto) }

            if (onAddCart != null) {
                binding.btnAddToCart.visibility = android.view.View.VISIBLE
                binding.btnAddToCart.setOnClickListener {
                    binding.btnAddToCart.animate().scaleX(0.95f).scaleY(0.95f).setDuration(120).withEndAction {
                        binding.btnAddToCart.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    }.start()
                    onAddCart(producto, binding.ivProductImage)
                }
            } else {
                binding.btnAddToCart.visibility = android.view.View.GONE
            }
        }
    }

    private fun isLoadableImage(s: String): Boolean {
        val lower = s.lowercase()
        if (lower.startsWith("data:")) return false
        if (lower.startsWith("http://") || lower.startsWith("https://")) return true
        if (lower.startsWith("file://") || lower.startsWith("content://")) return true
        return false
    }

    class DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem == newItem
    }
}

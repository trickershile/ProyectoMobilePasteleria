package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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

            Glide.with(itemView.context)
                .load(producto.imagen)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_delete)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
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

    class DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem == newItem
    }
}
package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ejemploprueba.Model.Producto
import com.example.ejemploprueba.databinding.ItemAdminProductoBinding

class AdminProductoAdapter(
    private val onEdit: (Producto) -> Unit,
    private val onDelete: (Producto) -> Unit
) : ListAdapter<Producto, AdminProductoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemAdminProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onEdit, onDelete)

    class ViewHolder(private val binding: ItemAdminProductoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(producto: Producto, onEdit: (Producto) -> Unit, onDelete: (Producto) -> Unit) {
            binding.tvNombre.text = producto.nombre
            binding.tvPrecio.text = "$${producto.precio}"
            binding.tvStock.text = "Stock: ${producto.stock}"
            binding.tvCategoria.text = producto.categoria

            val src = producto.imagen.takeIf { it.isNotBlank() && isLoadableImage(it) } ?: android.R.drawable.ic_menu_report_image
            Glide.with(itemView.context)
                .load(src)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivProducto)

            binding.btnEditar.setOnClickListener { onEdit(producto) }
            binding.btnEliminar.setOnClickListener { onDelete(producto) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem == newItem
    }

    companion object {
        fun isLoadableImage(s: String): Boolean {
            val lower = s.lowercase()
            if (lower.startsWith("data:")) return false
            if (lower.startsWith("http://") || lower.startsWith("https://")) return true
            if (lower.startsWith("file://") || lower.startsWith("content://")) return true
            return false
        }
    }
}

package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ejemploprueba.Model.CarritoItem
import com.example.ejemploprueba.databinding.ItemCarritoBinding

class CarritoAdapter(
    private val onCantidadChanged: (Int, Int) -> Unit,
    private val onEliminar: (Int) -> Unit
) : ListAdapter<CarritoItem, CarritoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onCantidadChanged, onEliminar)

    class ViewHolder(private val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CarritoItem, onCantChanged: (Int, Int) -> Unit, onDelete: (Int) -> Unit) {
            binding.tvNombre.text = item.nombre
            binding.tvPrecio.text = "$${item.precio}"
            binding.tvCantidad.text = item.cantidad.toString()
            binding.tvSubtotal.text = String.format("$%.2f", item.precio.toDouble() * item.cantidad)

            val safe = item.imagen.takeIf { it.isNotBlank() && CarritoAdapter.isLoadableImage(it) } ?: android.R.drawable.ic_menu_report_image
            Glide.with(itemView.context)
                .load(safe)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivProducto)

            binding.btnMenos.setOnClickListener {
                if (item.cantidad > 1) {
                    onCantChanged(item.detalleId, item.cantidad - 1)
                }
            }

            binding.btnMas.setOnClickListener {
                onCantChanged(item.detalleId, item.cantidad + 1)
            }

            binding.btnEliminar.setOnClickListener {
                onDelete(item.detalleId)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CarritoItem>() {
        override fun areItemsTheSame(oldItem: CarritoItem, newItem: CarritoItem) =
            oldItem.detalleId == newItem.detalleId

        override fun areContentsTheSame(oldItem: CarritoItem, newItem: CarritoItem) =
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

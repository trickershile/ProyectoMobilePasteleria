package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.InventarioItemDTO
import com.example.ejemploprueba.databinding.ItemInventarioBinding

class InventarioAdapter(
    private val onAgregar: (InventarioItemDTO) -> Unit,
    private val onReducir: (InventarioItemDTO) -> Unit,
    private val onDisponibilidad: (InventarioItemDTO, Int) -> Unit
) : ListAdapter<InventarioItemDTO, InventarioAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemInventarioBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onAgregar, onReducir, onDisponibilidad)

    class ViewHolder(private val binding: ItemInventarioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InventarioItemDTO, onAgregar: (InventarioItemDTO) -> Unit, onReducir: (InventarioItemDTO) -> Unit, onDisponibilidad: (InventarioItemDTO, Int) -> Unit) {
            binding.tvNombre.text = item.nombre
            binding.tvStock.text = "Stock: ${item.stock}"
            binding.tvMinimo.text = "Mínimo: ${item.stockMinimo}"
            binding.btnAgregar.setOnClickListener { onAgregar(item) }
            binding.btnReducir.setOnClickListener { onReducir(item) }
            binding.btnDisponibilidad.setOnClickListener {
                val cantidadText = binding.etCantidad.text?.toString()?.trim()
                val cantidad = cantidadText?.toIntOrNull() ?: 1
                onDisponibilidad(item, cantidad)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InventarioItemDTO>() {
        override fun areItemsTheSame(oldItem: InventarioItemDTO, newItem: InventarioItemDTO) =
            oldItem.productoId == newItem.productoId

        override fun areContentsTheSame(oldItem: InventarioItemDTO, newItem: InventarioItemDTO) =
            oldItem == newItem
    }
}
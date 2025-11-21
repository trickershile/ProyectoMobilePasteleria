package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.TopProductoDTO
import com.example.ejemploprueba.databinding.ItemTopProductoBinding

class TopProductosAdapter : ListAdapter<TopProductoDTO, TopProductosAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTopProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder(private val binding: ItemTopProductoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TopProductoDTO) {
            binding.tvNombre.text = item.nombre
            binding.tvCantidad.text = item.cantidadVendida.toString()
            binding.tvIngresos.text = item.ingresos
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TopProductoDTO>() {
        override fun areItemsTheSame(oldItem: TopProductoDTO, newItem: TopProductoDTO) =
            oldItem.productoId == newItem.productoId

        override fun areContentsTheSame(oldItem: TopProductoDTO, newItem: TopProductoDTO) =
            oldItem == newItem
    }
}
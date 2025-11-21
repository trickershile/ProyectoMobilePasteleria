package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.SerieIngresoDTO
import com.example.ejemploprueba.databinding.ItemIngresoSerieBinding

class IngresosAdapter : ListAdapter<SerieIngresoDTO, IngresosAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemIngresoSerieBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder(private val binding: ItemIngresoSerieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SerieIngresoDTO) {
            binding.tvFecha.text = item.fecha
            binding.tvMonto.text = item.monto
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SerieIngresoDTO>() {
        override fun areItemsTheSame(oldItem: SerieIngresoDTO, newItem: SerieIngresoDTO) =
            oldItem.fecha == newItem.fecha

        override fun areContentsTheSame(oldItem: SerieIngresoDTO, newItem: SerieIngresoDTO) =
            oldItem == newItem
    }
}
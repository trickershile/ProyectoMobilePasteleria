package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.PedidoDTO
import com.example.ejemploprueba.databinding.ItemOrdenBinding

class OrdenesAdapter : ListAdapter<PedidoDTO, OrdenesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemOrdenBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder(private val binding: ItemOrdenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orden: PedidoDTO) {
            binding.tvOrdenId.text = "Pedido #${orden.id}"
            binding.tvFecha.text = orden.fecha
            binding.tvTotal.text = "$${orden.total}"
            binding.tvEstado.text = orden.estado
            binding.tvDireccion.visibility = android.view.View.GONE

            // Cambiar color según estado
            when (orden.estado) {
                "Pendiente" -> {
                    binding.tvEstado.setBackgroundColor(0xFFFFF3E0.toInt())
                    binding.tvEstado.setTextColor(0xFFF57C00.toInt())
                }
                "Aceptado" -> {
                    binding.tvEstado.setBackgroundColor(0xFFE3F2FD.toInt())
                    binding.tvEstado.setTextColor(0xFF2196F3.toInt())
                }
                "Enviado" -> {
                    binding.tvEstado.setBackgroundColor(0xFFE8F5E9.toInt())
                    binding.tvEstado.setTextColor(0xFF4CAF50.toInt())
                }
                "Rechazado" -> {
                    binding.tvEstado.setBackgroundColor(0xFFFFEBEE.toInt())
                    binding.tvEstado.setTextColor(0xFFF44336.toInt())
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PedidoDTO>() {
        override fun areItemsTheSame(oldItem: PedidoDTO, newItem: PedidoDTO) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PedidoDTO, newItem: PedidoDTO) =
            oldItem == newItem
    }
}
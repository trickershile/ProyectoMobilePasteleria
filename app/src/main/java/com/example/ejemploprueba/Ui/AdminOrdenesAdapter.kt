package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.PedidoDTO
import com.example.ejemploprueba.databinding.ItemAdminOrdenBinding

class AdminOrdenesAdapter(
    private val onAccept: (PedidoDTO) -> Unit,
    private val onReject: (PedidoDTO) -> Unit,
    private val onShip: (PedidoDTO) -> Unit
) : ListAdapter<PedidoDTO, AdminOrdenesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemAdminOrdenBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onAccept, onReject, onShip)

    class ViewHolder(private val binding: ItemAdminOrdenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orden: PedidoDTO, onAccept: (PedidoDTO) -> Unit, onReject: (PedidoDTO) -> Unit, onShip: (PedidoDTO) -> Unit) {
            binding.tvOrdenId.text = "Pedido #${orden.id}"
            binding.tvFecha.text = orden.fecha
            binding.tvTotal.text = "$${orden.total}"
            binding.tvEstado.text = orden.estado
            binding.tvDireccion.visibility = View.GONE

            when (orden.estado) {
                "Pendiente" -> {
                    binding.btnAceptar.visibility = View.VISIBLE
                    binding.btnRechazar.visibility = View.VISIBLE
                    binding.btnEnviar.visibility = View.GONE
                }
                "Aceptado" -> {
                    binding.btnAceptar.visibility = View.GONE
                    binding.btnRechazar.visibility = View.GONE
                    binding.btnEnviar.visibility = View.VISIBLE
                }
                else -> {
                    binding.btnAceptar.visibility = View.GONE
                    binding.btnRechazar.visibility = View.GONE
                    binding.btnEnviar.visibility = View.GONE
                }
            }

            binding.btnAceptar.setOnClickListener { onAccept(orden) }
            binding.btnRechazar.setOnClickListener { onReject(orden) }
            binding.btnEnviar.setOnClickListener { onShip(orden) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PedidoDTO>() {
        override fun areItemsTheSame(oldItem: PedidoDTO, newItem: PedidoDTO) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PedidoDTO, newItem: PedidoDTO) =
            oldItem == newItem
    }
}
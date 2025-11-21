package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.PagoDTO
import com.example.ejemploprueba.databinding.ItemPagoBinding

class AdminPagosAdapter(
    private val onAprobar: (PagoDTO) -> Unit,
    private val onRechazar: (PagoDTO) -> Unit
) : ListAdapter<PagoDTO, AdminPagosAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPagoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onAprobar, onRechazar)
    }

    class ViewHolder(private val binding: ItemPagoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pago: PagoDTO, onAprobar: (PagoDTO) -> Unit, onRechazar: (PagoDTO) -> Unit) {
            binding.tvPagoId.text = "Pago #${pago.id}"
            binding.tvPedidoId.text = "Pedido #${pago.pedidoId}"
            binding.tvMonto.text = pago.monto
            binding.tvEstado.text = pago.estado
            binding.btnAprobar.setOnClickListener { onAprobar(pago) }
            binding.btnRechazar.setOnClickListener { onRechazar(pago) }
        }
    }

    class Diff : DiffUtil.ItemCallback<PagoDTO>() {
        override fun areItemsTheSame(oldItem: PagoDTO, newItem: PagoDTO) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PagoDTO, newItem: PagoDTO) = oldItem == newItem
    }
}
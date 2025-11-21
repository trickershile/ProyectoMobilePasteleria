package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.CategoriaResponseDTO
import com.example.ejemploprueba.databinding.ItemCategoriaBinding

class CategoriasAdapter(
    private val onEdit: (CategoriaResponseDTO) -> Unit,
    private val onToggle: (CategoriaResponseDTO) -> Unit
) : ListAdapter<CategoriaResponseDTO, CategoriasAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoriaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onEdit, onToggle)

    class ViewHolder(private val binding: ItemCategoriaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categoria: CategoriaResponseDTO, onEdit: (CategoriaResponseDTO) -> Unit, onToggle: (CategoriaResponseDTO) -> Unit) {
            binding.tvNombre.text = categoria.nombre
            binding.tvEstado.text = if (categoria.activa) "Activa" else "Inactiva"
            binding.tvEstado.setTextColor(if (categoria.activa) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            binding.btnEditar.setOnClickListener { onEdit(categoria) }
            binding.btnToggle.text = if (categoria.activa) "Desactivar" else "Activar"
            binding.btnToggle.setOnClickListener { onToggle(categoria) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CategoriaResponseDTO>() {
        override fun areItemsTheSame(oldItem: CategoriaResponseDTO, newItem: CategoriaResponseDTO) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CategoriaResponseDTO, newItem: CategoriaResponseDTO) =
            oldItem == newItem
    }
}
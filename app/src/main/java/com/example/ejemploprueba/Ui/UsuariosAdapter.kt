package com.example.ejemploprueba.Ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemploprueba.Model.Usuario
import com.example.ejemploprueba.databinding.ItemUsuarioBinding

class UsuariosAdapter(
    private val onBlock: (Usuario) -> Unit,
    private val onCrearCliente: (Usuario) -> Unit,
    private val onEditar: (Usuario) -> Unit,
    private val onEliminar: (Usuario) -> Unit
) : ListAdapter<Usuario, UsuariosAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onBlock, onCrearCliente, onEditar, onEliminar)

    class ViewHolder(private val binding: ItemUsuarioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: Usuario, onBlock: (Usuario) -> Unit, onCrearCliente: (Usuario) -> Unit, onEditar: (Usuario) -> Unit, onEliminar: (Usuario) -> Unit) {
            binding.tvNombre.text = usuario.nombre
            binding.tvEmail.text = usuario.email
            binding.tvRole.text = usuario.role

            // Estado de bloqueo
            if (usuario.bloqueado) {
                binding.tvEstado.text = "Inactivo"
                binding.tvEstado.setTextColor(0xFFF44336.toInt())
                binding.btnBloquear.text = "Activar"
            } else {
                binding.tvEstado.text = "Activo"
                binding.tvEstado.setTextColor(0xFF4CAF50.toInt())
                binding.btnBloquear.text = "Desactivar"
            }

            binding.btnBloquear.setOnClickListener { onBlock(usuario) }

            val esCliente = usuario.role.equals("Cliente", true)
            binding.btnCrearCliente.visibility = if (esCliente) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnCrearCliente.setOnClickListener { onCrearCliente(usuario) }
            binding.btnEditar.setOnClickListener { onEditar(usuario) }
            binding.btnEliminar.setOnClickListener { onEliminar(usuario) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Usuario>() {
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario) =
            oldItem == newItem
    }
}
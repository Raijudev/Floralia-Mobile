package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioAdapter(
    private val usuarios: List<Usuario>,
    private val onClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.textViewNombreUsuario)
        val rol: TextView = view.findViewById(R.id.textViewRolUsuario)
        val imagenPerfil: ImageView = view.findViewById(R.id.imageViewRol)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(usuarios[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_card, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]

        holder.nombre.text = formatNombreCompleto(usuario.nombre, usuario.apellido)
        holder.rol.text = usuario.rol ?: ""

        when (usuario.rol?.lowercase()) {
            "administrador" -> holder.imagenPerfil.setImageResource(R.drawable.administrador)
            "empleado" -> holder.imagenPerfil.setImageResource(R.drawable.usuario_empleado_cliente)
            "cliente" -> holder.imagenPerfil.setImageResource(R.drawable.usuario_empleado_cliente)
            else -> holder.imagenPerfil.setImageResource(R.drawable.usuario_empleado_cliente)
        }
    }

    override fun getItemCount(): Int = usuarios.size

    private fun formatNombreCompleto(nombre: String?, apellido: String?): String {
        val nom = nombre?.replaceFirstChar { it.uppercaseChar() } ?: ""
        val ape = apellido?.replaceFirstChar { it.uppercaseChar() } ?: ""
        return "$nom $ape".trim()
    }
}

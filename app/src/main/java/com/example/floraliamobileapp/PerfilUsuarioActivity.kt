package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PerfilUsuarioActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        val tvNombre = findViewById<TextView>(R.id.tvNombrePerfil)
        val tvRol = findViewById<TextView>(R.id.tvRolPerfil)
        val imagePerfil = findViewById<ImageView>(R.id.imagePerfil)
        val imageBack = findViewById<ImageView>(R.id.imageViewBack)

        // Recuperar datos enviados desde el menú
        val nombre = intent.getStringExtra("nombre") ?: ""
        val apellido = intent.getStringExtra("apellido") ?: ""
        val rol = intent.getStringExtra("rol") ?: ""

        // Mostrar en los TextView
        tvNombre.text = "Nombre: $nombre $apellido"
        tvRol.text = "Rol: $rol"

        // Cambiar imagen según rol
        when (rol.lowercase()) {
            "administrador" -> imagePerfil.setImageResource(R.drawable.administrador)
            "empleado", "repartidor", "cliente" -> imagePerfil.setImageResource(R.drawable.usuario_empleado_cliente)
            else -> imagePerfil.setImageResource(R.drawable.usuario_empleado_cliente)
        }

        // Regresar a la pantalla anterior
        imageBack.setOnClickListener {
            finish()
        }
    }
}

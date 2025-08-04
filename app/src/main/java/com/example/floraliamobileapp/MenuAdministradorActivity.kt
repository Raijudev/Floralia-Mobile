package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuAdministradorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_administrador)

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val imageViewPerfil = findViewById<ImageView>(R.id.imageViewPerfil)
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombre") ?: ""
                        val apellido = document.getString("apellido") ?: ""
                        val rol = document.getString("rol") ?: ""

                        // Mostrar saludo personalizado
                        tvBienvenida.text = "Bienvenido, $nombre $apellido!"

                        // Asignar imagen según el rol
                        when (rol.lowercase()) {
                            "administrador" -> imageViewPerfil.setImageResource(R.drawable.administrador)
                            "empleado", "repartidor", "cliente" -> imageViewPerfil.setImageResource(R.drawable.usuario_empleado_cliente)
                            else -> imageViewPerfil.setImageResource(R.drawable.usuario_empleado_cliente)
                        }

                        // Al hacer clic en la imagen de perfil, abrir perfil con datos
                        imageViewPerfil.setOnClickListener {
                            val intent = Intent(this, PerfilUsuarioActivity::class.java)
                            intent.putExtra("nombre", nombre)
                            intent.putExtra("apellido", apellido)
                            intent.putExtra("rol", rol)
                            startActivity(intent)
                        }
                    } else {
                        tvBienvenida.text = "Hola, Usuario!"
                    }
                }
                .addOnFailureListener {
                    tvBienvenida.text = "Hola, Usuario!"
                    Toast.makeText(this, "Error al obtener nombre", Toast.LENGTH_SHORT).show()
                }
        }

        // --- Manejo de clics en tarjetas ---
        findViewById<CardView>(R.id.CardViewAgregarUsuario).setOnClickListener {
            startActivity(Intent(this, AgregarUsuarioActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewInventario).setOnClickListener {
            startActivity(Intent(this, InventarioActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewHistorialdePedidos).setOnClickListener {
            startActivity(Intent(this, HistorialPedidosActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewGestiondeUsuarios).setOnClickListener {
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewCortesdeCaja).setOnClickListener {
            startActivity(Intent(this, CortesDeCajaActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewInformaciondelaAplicacion).setOnClickListener {
            startActivity(Intent(this, InfoAppActivity::class.java))
        }

        // Botón de cerrar sesión
        findViewById< TextView>(R.id.tvCerrarSesion).setOnClickListener {
            // ⚠️ **Importante:** Asegúrate de que este TextView no se toque accidentalmente.
            // Considera su posición y tamaño en el diseño.

            // Crea un AlertDialog para confirmar el cierre de sesión
            android.app.AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión") // Título del diálogo
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?") // Mensaje de confirmación
                .setPositiveButton("Sí") { dialog, which ->
                    // Si el usuario presiona "Sí", procede con el cierre de sesión
                    auth.signOut() // Realiza el cierre de sesión en Firebase
                    val intent = Intent(this, LoginActivity::class.java)
                    // Estas flags aseguran que el historial de actividades se limpie y el usuario no pueda volver atrás
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent) // Inicia la actividad de Login
                    finish() // Cierra la actividad actual (MenuAdministradorActivity)
                }
                .setNegativeButton("No", null) // Si el usuario presiona "No", no hace nada (el diálogo se cierra)
                .show() // Muestra el diálogo al usuario
        }
    }
}

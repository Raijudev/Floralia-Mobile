package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog

class MenuEmpleadoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_empleado)

        // Inicializar Firebase Auth y Firestore
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

                        // Mostrar mensaje personalizado
                        tvBienvenida.text = "¡Bienvenido, $nombre $apellido!"

                        // Asignar imagen de perfil según el rol
                        when (rol.lowercase()) {
                            "administrador" -> imageViewPerfil.setImageResource(R.drawable.administrador)
                            "empleado" -> imageViewPerfil.setImageResource(R.drawable.usuario_empleado_cliente)
                            else -> imageViewPerfil.setImageResource(R.drawable.usuario_empleado_cliente)
                        }

                        // Clic en la imagen de perfil abre la vista de perfil
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
                    // Mensaje Toast mejorado para ser más descriptivo
                    Toast.makeText(this, "Error al cargar los datos del usuario.", Toast.LENGTH_SHORT).show()
                }
        }

        // --- Navegación de tarjetas ---
        findViewById<CardView>(R.id.CardViewInventario).setOnClickListener {
            startActivity(Intent(this, InventarioActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewHistorialdePedidos).setOnClickListener {
            startActivity(Intent(this, HistorialPedidosActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewCortesdeCaja).setOnClickListener {
            startActivity(Intent(this, CortesDeCajaActivity::class.java))
        }

        findViewById<CardView>(R.id.CardViewInformaciondelaAplicacion).setOnClickListener {
            startActivity(Intent(this, InfoAppActivity::class.java))
        }

        // --- Cerrar sesión con confirmación ---
        findViewById<Button>(R.id.buttonCerrarSesion).setOnClickListener {
            // Crea un AlertDialog para confirmar el cierre de sesión
            AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí") { dialog, which ->
                    // Si el usuario confirma, procede con el cierre de sesión
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null) // Si el usuario cancela, no hace nada
                .show() // Muestra el diálogo
        }
    }
}

package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class MenuRepartidorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_repartidor)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Carta para ir al módulo de pedidos pendientes
        val cardPedidosPendientes = findViewById<CardView>(R.id.CardViewPedidosPendientes)
        cardPedidosPendientes.setOnClickListener {
            val intent = Intent(this, HistorialPedidosActivity::class.java)
            startActivity(intent)
        }

        // Carta para ir al módulo información de la aplicación
        val cardInfoApp = findViewById<CardView>(R.id.CardViewInformaciondelaAplicacion)
        cardInfoApp.setOnClickListener {
            val intent = Intent(this, InfoAppActivity::class.java)
            startActivity(intent)
        }

        // Ver imagen puesto para cerrar sesión
        val imgCerrarSesion = findViewById<ImageView>(R.id.ImageViewAbrirAgregarProducto)
        imgCerrarSesion.setOnClickListener {
            auth.signOut()  // Cierra sesión de Firebase

            // Redirige al login (LoginActivity) y limpia el historial
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}

package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    // Firebase variables para autenticación y base de datos
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Animaciones del logo y texto
        val logo = findViewById<ImageView>(R.id.logoImage)
        val text = findViewById<TextView>(R.id.textView)

        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim)
        val textAnim = AnimationUtils.loadAnimation(this, R.anim.text_anim)

        logo.startAnimation(logoAnim)
        text.startAnimation(textAnim)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Esperar 2.5 segundos antes de redirigir
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // 🔐 Usuario ya autenticado, obtener su rol desde Firestore
                firestore.collection("usuarios")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val rol = document.getString("rol")
                            when (rol) {
                                "Administrador" -> {
                                    startActivity(
                                        Intent(
                                            this,
                                            MenuAdministradorActivity::class.java
                                        )
                                    )
                                }
                                "Empleado" -> {
                                    startActivity(Intent(this, MenuEmpleadoActivity::class.java))
                                }
                                "Repartidor" -> {
                                    startActivity(Intent(this, MenuRepartidorActivity::class.java))
                                }
                                else -> {
                                    // Rol no válido o no asignado, ir al login
                                    startActivity(Intent(this, LoginActivity::class.java))
                                }
                            }
                        } else {
                            // Documento de usuario no existe
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                        finish() // Cierra SplashActivity
                    }
                    .addOnFailureListener { e ->
                        Log.e("SplashActivity", "Error al obtener usuario: ${e.message}")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
            } else {
                // 🔓 No hay sesión activa, ir al LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 2500) // Tiempo de duración del splash: 2.5 segundos
    }
}

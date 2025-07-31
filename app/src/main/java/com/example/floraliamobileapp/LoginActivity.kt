package com.example.floraliamobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // Variable para la autenticación con Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establecer el diseño en XML como layout principal
        setContentView(R.layout.activity_login)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Referencias a los elementos XML
        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress1)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword1)
        val loginButton = findViewById<Button>(R.id.buttonContinuar)

        // Botón de inicio de sesión
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Iniciar sesión con Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Obtener UID del usuario autenticado
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            // Consultar Firestore para obtener el rol
                            firestore.collection("usuarios")
                                .document(uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val rol = document.getString("rol")

                                        when (rol) {
                                            "Administrador" -> {
                                                startActivity(Intent(this, MenuAdministradorActivity::class.java))
                                                finish()
                                            }
                                            "Empleado" -> {
                                                startActivity(Intent(this, MenuEmpleadoActivity::class.java))
                                                finish()
                                            }
                                            "Repartidor" -> {
                                                startActivity(Intent(this, MenuRepartidorActivity::class.java))
                                                finish()
                                            }
                                            else -> {
                                                Toast.makeText(this, "Rol no válido o no asignado", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al obtener el rol: ${e.message}", Toast.LENGTH_LONG).show()
                                    Log.e("Firestore", "Error al obtener documento", e)
                                }
                        }
                    } else {
                        // Error en el login
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        Log.e("FirebaseAuth", "signInWithEmailAndPassword", task.exception)
                    }
                }
        }
    }
}

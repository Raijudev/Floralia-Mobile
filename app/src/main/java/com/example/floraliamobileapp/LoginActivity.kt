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

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress1)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword1)
        val loginButton = findViewById<Button>(R.id.buttonContinuar)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            // Limpia los errores anteriores
            emailEditText.error = null
            passwordEditText.error = null

            // Validación de campos vacíos
            var isValid = true
            if (email.isEmpty()) {
                emailEditText.error = "Campo obligatorio"
                isValid = false
            }
            if (password.isEmpty()) {
                passwordEditText.error = "Campo obligatorio"
                isValid = false
            }

            if (!isValid) {
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
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
                                                Toast.makeText(this, "El rol asignado no es válido.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "Usuario no encontrado en la base de datos.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al obtener la información del usuario: ${e.message}.", Toast.LENGTH_LONG).show()
                                    Log.e("Firestore", "Error al obtener documento.", e)
                                }
                        }
                    } else {
                        // En caso de fallo, se muestra el error en el campo de contraseña
                        passwordEditText.error = "Correo o contraseña incorrectos"
                        Toast.makeText(this, "Error al iniciar sesión: ${task.exception?.message}.", Toast.LENGTH_LONG).show()
                        Log.e("FirebaseAuth", "signInWithEmailAndPassword.", task.exception)
                    }
                }
        }
    }
}

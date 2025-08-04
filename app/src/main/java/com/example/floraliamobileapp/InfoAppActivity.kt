package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout

class InfoAppActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_app)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val terminosUso = findViewById<TextView>(R.id.TerminosUso)

        btnBack.setOnClickListener {
            finish()
        }

        // --- Inicio del fragmento de código del menú lateral ---
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val imageViewMenu = findViewById<ImageView>(R.id.imageViewMenu)
        val imageViewLogoMenu = findViewById<ImageView>(R.id.imageViewMenuLogo)

        val closeDrawer = {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        // Abrir menú lateral al dar clic en el ImageView del logo
        imageViewMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Opciones del menú lateral
        val menuAgregarUsuario = findViewById<TextView>(R.id.menuAgregarUsuario)
        val menuProductos = findViewById<TextView>(R.id.menuProductos)
        val menuPedidos = findViewById<TextView>(R.id.menuPedidos)
        val menuUsuarios = findViewById<TextView>(R.id.menuUsuarios)
        val menuCortesdeCaja = findViewById<TextView>(R.id.menuCortesdeCaja)
        val menuInfoApp = findViewById<TextView>(R.id.menuInfoApp)

        imageViewLogoMenu.setOnClickListener { closeDrawer() }

        // --- Lógica de validación de rol para el menú ---
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val rol = document.getString("rol")
                        if (rol == "Administrador") {
                            menuUsuarios.visibility = View.VISIBLE
                        } else if (rol == "Empleado") {
                            menuUsuarios.visibility = View.GONE
                        }
                    } else {
                        // Documento del usuario no existe, ocultar por seguridad
                        menuUsuarios.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    // Error al obtener el rol, ocultar por seguridad
                    println("Error al obtener el rol del usuario: $exception")
                    menuUsuarios.visibility = View.GONE
                }
        } else {
            // No hay usuario logeado, ocultar por seguridad
            menuUsuarios.visibility = View.GONE
        }
        // --- Fin de la lógica de validación de rol ---

        // --- Resaltar la opción del menú actual (NUEVO CÓDIGO) ---
        // Primero, restablece todos los colores a su estado normal
        val defaultColor = resources.getColor(R.color.black, theme) // O el color por defecto de tu texto
        menuAgregarUsuario.setTextColor(defaultColor)
        menuProductos.setTextColor(defaultColor)
        // Agrega aquí todas las opciones de menú que tengas
        menuPedidos.setTextColor(defaultColor)
        menuUsuarios.setTextColor(defaultColor)
        menuCortesdeCaja.setTextColor(defaultColor)
        menuInfoApp.setTextColor(defaultColor)

        // Luego, aplica el color gris bajo a la opción de la actividad actual
        val highlightColor = resources.getColor(R.color.gray_light, theme)

        when (this) {
            is AgregarUsuarioActivity -> menuAgregarUsuario.setTextColor(highlightColor)
            is InventarioActivity -> menuProductos.setTextColor(highlightColor) // Asumiendo que InventarioActivity es "Productos"
            is HistorialPedidosActivity -> menuPedidos.setTextColor(highlightColor)
            is GestionUsuariosActivity -> menuUsuarios.setTextColor(highlightColor)
            is CortesDeCajaActivity -> menuCortesdeCaja.setTextColor(highlightColor)
            is InfoAppActivity -> menuInfoApp.setTextColor(highlightColor)
            // Agrega más casos para cada una de tus actividades de menú
        }
        // --- Fin de la lógica de resaltado ---

        menuAgregarUsuario.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, AgregarUsuarioActivity::class.java))
            finish()
        }

        menuProductos.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, InventarioActivity::class.java))
            finish()
        }

        menuPedidos.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, HistorialPedidosActivity::class.java))
            finish()
        }

        menuUsuarios.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
            finish()
        }

        menuCortesdeCaja.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, CortesDeCajaActivity::class.java))
            finish()
        }

        menuInfoApp.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            // Ya estás en esta pantalla, solo cierra el menú
        }
        // --- Fin del fragmento de código del menú lateral ---

        terminosUso.setOnClickListener {
            mostrarPopupTerminos()
        }
    }

    private fun mostrarPopupTerminos() {
        val mensajeTerminos = """
        • Al usar Floralia Mobile, aceptas estos términos. Si no estás de acuerdo, no utilices la app.

        • Floralia Mobile ayuda a florerías a gestionar productos, pedidos, cortes de caja y usuarios desde el celular.

        • Algunas funciones están limitadas según tu rol:
        - Administrador: acceso completo.
        - Empleado: acceso restringido.

        • Debes tener una cuenta registrada. El acceso es por correo y contraseña.

        • Se almacenan datos personales como nombre, dirección, CURP y rol. Solo se usan dentro de la app.

        • No puedes cambiar tu correo ni contraseña desde la app. Para hacerlo, contacta a soporte.

        • Puedes asignar tarjetas NFC, siempre que estén disponibles. Las tarjetas ya asignadas no se reutilizan.

        • Toda la app (diseño, contenidos, logos) pertenece al cliente titular. No puedes copiar ni distribuir sin permiso.

        • Floralia no se hace responsable por:
        - Pérdida de datos por mal uso.
        - Fallos del sistema por problemas externos.
        - Accesos indebidos por descuido del usuario.

        • Los términos pueden cambiar en cualquier momento. Usar la app implica aceptar los cambios.

        • ¿Dudas? Contacta al administrador o al correo oficial de Floralia.
""".trimIndent()

        // Inflar el layout personalizado
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.popup_terminos_uso, null)

        val tvContenido = dialogView.findViewById<TextView>(R.id.tvContenidoTerminos)
        val btnCerrar = dialogView.findViewById<TextView>(R.id.btnCerrarPopup)

        tvContenido.text = mensajeTerminos

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

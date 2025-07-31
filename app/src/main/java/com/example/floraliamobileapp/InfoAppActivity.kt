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
        val menuAgregarProducto = findViewById<TextView>(R.id.menuAgregarProducto)
        val menuPedidos = findViewById<TextView>(R.id.menuPedidos)
        val menuUsuarios = findViewById<TextView>(R.id.menuUsuarios)
        val menuCortesdeCaja = findViewById<TextView>(R.id.menuCortesdeCaja)
        val menuInfoApp = findViewById<TextView>(R.id.menuInfoApp)

        imageViewLogoMenu.setOnClickListener { closeDrawer() }

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

        menuAgregarProducto.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, AgregarProductoActivity::class.java))
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

        • Floralia Mobile no se hace responsable por:
        - Pérdida de datos por mal uso.
        - Fallos del sistema por problemas externos.
        - Accesos indebidos por descuido del usuario.

        • Los términos pueden cambiar en cualquier momento. Usar la app implica aceptar los cambios.

        • ¿Dudas? Contacta al administrador o al correo oficial de Floralia Mobile.
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

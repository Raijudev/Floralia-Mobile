package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextCantidad: EditText
    private lateinit var editTextPrecioUnitario: EditText
    private lateinit var imageViewProducto: ImageView
    private lateinit var buttonCargarImagen: Button
    private lateinit var buttonAgregarProducto: Button
    private lateinit var imageViewBack: ImageView

    private var imagenUri: Uri? = null
    private val REQUEST_IMAGE_PICK = 1001
    private val firestore = FirebaseFirestore.getInstance()
    private var imagenBase64: String? = null

    // Nuevo: ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)

        // Referencias UI
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextCantidad = findViewById(R.id.editTextCantidad)
        editTextPrecioUnitario = findViewById(R.id.editTextPrecioUnitario)
        imageViewProducto = findViewById(R.id.imageViewProducto)
        buttonCargarImagen = findViewById(R.id.buttonCargarImagen)
        buttonAgregarProducto = findViewById(R.id.buttonAgregarProducto)
        imageViewBack = findViewById(R.id.imageViewBack)

        // Inicializar ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Agregando Producto...")
        progressDialog.setCancelable(false)

        imageViewBack.setOnClickListener {
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
            // Ya estás en esta pantalla, solo cierra el menú
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
            startActivity(Intent(this, InfoAppActivity::class.java))
            finish()
        }

        configurarFormatoPrecioMXN()

        buttonCargarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        buttonAgregarProducto.setOnClickListener {
            val nombre = editTextNombre.text.toString().trim()
            val cantidad = editTextCantidad.text.toString().trim()
            val precioTexto = editTextPrecioUnitario.text.toString().trim()

            if (nombre.isEmpty() || cantidad.isEmpty() || precioTexto.isEmpty() || imagenBase64 == null) {
                Toast.makeText(this, "Completa todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show()
            } else {
                val precio = obtenerPrecioComoDouble(precioTexto)
                if (precio == null) {
                    Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show()
                } else {
                    guardarProductoEnFirestore(nombre, cantidad, precio)
                }
            }
        }
    }

    private fun configurarFormatoPrecioMXN() {
        editTextPrecioUnitario.addTextChangedListener(object : TextWatcher {
            private var actualizando = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (actualizando) return
                actualizando = true

                var texto = s.toString().replace("[^\\d.]".toRegex(), "")
                val partes = texto.split(".")
                texto = if (partes.size > 2) partes[0] + "." + partes[1] else texto
                if (partes.size == 2 && partes[1].length > 2) {
                    texto = partes[0] + "." + partes[1].substring(0, 2)
                }
                if (texto.isNotEmpty()) texto = "$$texto"

                editTextPrecioUnitario.setText(texto)
                editTextPrecioUnitario.setSelection(texto.length)
                actualizando = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun obtenerPrecioComoDouble(precioTexto: String): Double? {
        return try {
            val limpio = precioTexto.replace("[^\\d.]".toRegex(), "")
            limpio.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    @Deprecated("Usa Activity Result API en el futuro")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            imagenUri = data.data
            imageViewProducto.setImageURI(imagenUri)

            imagenUri?.let { uri ->
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imagenBase64 = convertirABase64(bitmap)
            }
        }
    }

    private fun convertirABase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun guardarProductoEnFirestore(nombre: String, cantidad: String, precio: Double) {
        progressDialog.show() // <-- Mostrar mientras guarda

        val producto = hashMapOf(
            "nombre" to nombre,
            "cantidad" to cantidad.toInt(),
            "precioUnitario" to precio,
            "imagenBase64" to imagenBase64,
            "fechaCreacion" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("productos")
            .add(producto)
            .addOnSuccessListener {
                progressDialog.dismiss() // <-- Ocultar al terminar
                Toast.makeText(this, "Producto agregado con éxito", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                limpiarCampos()
            }
            .addOnFailureListener {
                progressDialog.dismiss() // <-- Ocultar en caso de error también
                Toast.makeText(this, "Error al guardar producto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        editTextNombre.text.clear()
        editTextCantidad.text.clear()
        editTextPrecioUnitario.text.clear()
        imageViewProducto.setImageResource(R.drawable.logo_floralia)
        imagenUri = null
        imagenBase64 = null
    }
}

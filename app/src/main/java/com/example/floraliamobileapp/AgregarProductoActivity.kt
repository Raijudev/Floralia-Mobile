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
import com.google.firebase.auth.FirebaseAuth
import android.view.View
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

        // --- Inicio del fragmento de código del menú lateral ---
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val imageViewMenu = findViewById<ImageView>(R.id.imageViewMenu)
        val imageViewLogoMenu = findViewById<ImageView>(R.id.imageViewMenuLogo)

        val closeDrawer = {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        imageViewMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val menuAgregarUsuario = findViewById<TextView>(R.id.menuAgregarUsuario)
        val menuProductos = findViewById<TextView>(R.id.menuProductos)
        val menuPedidos = findViewById<TextView>(R.id.menuPedidos)
        val menuUsuarios = findViewById<TextView>(R.id.menuUsuarios)
        val menuCortesdeCaja = findViewById<TextView>(R.id.menuCortesdeCaja)
        val menuInfoApp = findViewById<TextView>(R.id.menuInfoApp)

        imageViewLogoMenu.setOnClickListener { closeDrawer() }

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
                        menuUsuarios.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error al obtener el rol del usuario: $exception.")
                    menuUsuarios.visibility = View.GONE
                }
        } else {
            menuUsuarios.visibility = View.GONE
        }

        val defaultColor = resources.getColor(R.color.black, theme)
        menuAgregarUsuario.setTextColor(defaultColor)
        menuProductos.setTextColor(defaultColor)
        menuPedidos.setTextColor(defaultColor)
        menuUsuarios.setTextColor(defaultColor)
        menuCortesdeCaja.setTextColor(defaultColor)
        menuInfoApp.setTextColor(defaultColor)

        val highlightColor = resources.getColor(R.color.gray_light, theme)

        when (this) {
            is AgregarUsuarioActivity -> menuAgregarUsuario.setTextColor(highlightColor)
            is InventarioActivity -> menuProductos.setTextColor(highlightColor)
            is HistorialPedidosActivity -> menuPedidos.setTextColor(highlightColor)
            is GestionUsuariosActivity -> menuUsuarios.setTextColor(highlightColor)
            is CortesDeCajaActivity -> menuCortesdeCaja.setTextColor(highlightColor)
            is InfoAppActivity -> menuInfoApp.setTextColor(highlightColor)
        }

        menuAgregarUsuario.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, AgregarUsuarioActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        menuProductos.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, InventarioActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        menuPedidos.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, HistorialPedidosActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        menuUsuarios.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, GestionUsuariosActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        menuCortesdeCaja.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, CortesDeCajaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        menuInfoApp.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, InfoAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // --- Fin del fragmento de código del menú lateral ---

        configurarFormatoPrecioMXN()

        buttonCargarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        buttonAgregarProducto.setOnClickListener {
            // Limpia los errores anteriores
            editTextNombre.error = null
            editTextCantidad.error = null
            editTextPrecioUnitario.error = null

            val nombre = editTextNombre.text.toString().trim()
            val cantidadStr = editTextCantidad.text.toString().trim()
            val precioTexto = editTextPrecioUnitario.text.toString().trim()

            var isValid = true

            if (nombre.isEmpty()) {
                editTextNombre.error = "El nombre no puede estar vacío"
                isValid = false
            }

            val cantidad = cantidadStr.toIntOrNull()
            if (cantidadStr.isEmpty()) {
                editTextCantidad.error = "La cantidad no puede estar vacía"
                isValid = false
            } else if (cantidad == null || cantidad < 0) {
                editTextCantidad.error = "Debe ser un número entero positivo"
                isValid = false
            }

            val precio = obtenerPrecioComoDouble(precioTexto)
            if (precioTexto.isEmpty()) {
                editTextPrecioUnitario.error = "El precio no puede estar vacío"
                isValid = false
            } else if (precio == null || precio < 0) {
                editTextPrecioUnitario.error = "Formato de precio no válido"
                isValid = false
            }

            if (imagenBase64 == null) {
                Toast.makeText(this, "Por favor, selecciona una imagen.", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (isValid) {
                guardarProductoEnFirestore(nombre, cantidad!!, precio!!)
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

    private fun guardarProductoEnFirestore(nombre: String, cantidad: Int, precio: Double) {
        progressDialog.show()

        val producto = hashMapOf(
            "nombre" to nombre,
            "cantidad" to cantidad,
            "precioUnitario" to precio,
            "imagenBase64" to imagenBase64,
            "fechaCreacion" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("productos")
            .add(producto)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "El producto ha sido agregado correctamente.", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al agregar el producto: ${e.message}.", Toast.LENGTH_SHORT).show()
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

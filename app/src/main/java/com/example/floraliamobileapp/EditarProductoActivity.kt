package com.example.floraliamobileapp

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var idProducto: String

    private lateinit var editTextNombre: EditText
    private lateinit var editTextCantidad: EditText
    private lateinit var editTextPrecioUnitario: EditText
    private lateinit var imageViewProducto: ImageView
    private lateinit var buttonGuardarCambios: Button
    private lateinit var buttonCargarImagen: Button
    private lateinit var imageViewBack: ImageView
    private lateinit var imageViewMenu: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var imageViewMenuLogo: ImageView

    private lateinit var menuAgregarUsuario: TextView
    private lateinit var menuProductos: TextView
    private lateinit var menuPedidos: TextView
    private lateinit var menuUsuarios: TextView
    private lateinit var menuCortesdeCaja: TextView
    private lateinit var menuInfoApp: TextView

    private var imagenBase64: String? = null
    private val REQUEST_IMAGE_PICK = 101

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_producto)

        db = FirebaseFirestore.getInstance()
        idProducto = intent.getStringExtra("idProducto") ?: run {
            Toast.makeText(this, "No se pudo cargar la información del producto. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inicializar vistas
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextCantidad = findViewById(R.id.editTextCantidad)
        editTextPrecioUnitario = findViewById(R.id.editTextPrecioUnitario)
        imageViewProducto = findViewById(R.id.imageViewProducto)
        buttonGuardarCambios = findViewById(R.id.buttonGuardarCambios)
        buttonCargarImagen = findViewById(R.id.buttonCargarImagen)
        imageViewBack = findViewById(R.id.imageViewBack)
        drawerLayout = findViewById(R.id.drawerLayout)
        imageViewMenu = findViewById(R.id.imageViewMenu)
        imageViewMenuLogo = findViewById(R.id.imageViewMenuLogo)

        menuAgregarUsuario = findViewById(R.id.menuAgregarUsuario)
        menuProductos = findViewById(R.id.menuProductos)
        menuPedidos = findViewById(R.id.menuPedidos)
        menuUsuarios = findViewById(R.id.menuUsuarios)
        menuCortesdeCaja = findViewById(R.id.menuCortesdeCaja)
        menuInfoApp = findViewById(R.id.menuInfoApp)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)

        configurarFormatoPrecioMXN()

        // Botón de retroceso
        imageViewBack.setOnClickListener { finish() }

        // --- Inicio del fragmento de código del menú lateral ---
        val closeDrawer = {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        imageViewMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        imageViewMenuLogo.setOnClickListener { closeDrawer() }

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

        // Botón para cargar imagen
        buttonCargarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // Botón para guardar cambios
        buttonGuardarCambios.setOnClickListener {
            actualizarProducto()
        }

        cargarDatosProducto()
    }

    private fun cargarDatosProducto() {
        db.collection("productos").document(idProducto).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    editTextNombre.setText(doc.getString("nombre") ?: "")
                    editTextCantidad.setText(doc.getLong("cantidad")?.toString() ?: "")

                    val precio = doc.getDouble("precioUnitario")
                    if (precio != null) {
                        editTextPrecioUnitario.setText("$%.2f".format(precio))
                    }

                    imagenBase64 = doc.getString("imagenBase64")
                    try {
                        val bytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageViewProducto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imageViewProducto.setImageResource(R.drawable.logo_floralia)
                    }
                } else {
                    Toast.makeText(this, "El producto no fue encontrado.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar la información del producto: ${e.message}.", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun actualizarProducto() {
        // Limpia errores anteriores
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
            Toast.makeText(this, "Por favor, selecciona una imagen para el producto.", Toast.LENGTH_LONG).show()
            isValid = false
        }

        if (!isValid) {
            return
        }

        val datos = mapOf(
            "nombre" to nombre,
            "cantidad" to cantidad!!,
            "precioUnitario" to precio!!,
            "imagenBase64" to imagenBase64!!
        )

        progressDialog.setMessage("Guardando cambios del producto...")
        progressDialog.show()

        db.collection("productos").document(idProducto).update(datos)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Producto actualizado con éxito.", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al guardar los cambios: ${e.message}.", Toast.LENGTH_LONG).show()
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

                if (partes.size > 2) texto = partes[0] + "." + partes[1]
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

    @Deprecated("Usa ActivityResult API en el futuro")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            imageViewProducto.setImageURI(uri)
            val inputStream: InputStream? = contentResolver.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imagenBase64 = convertirABase64(bitmap)
        }
    }

    private fun convertirABase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}

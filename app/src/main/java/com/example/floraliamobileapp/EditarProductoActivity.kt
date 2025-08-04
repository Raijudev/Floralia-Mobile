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
    private lateinit var menuAgregarProducto: TextView
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
        imageViewMenu = findViewById(R.id.imageViewMenu)
        drawerLayout = findViewById(R.id.drawerLayout)

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
            val intent = Intent(this, AgregarUsuarioActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            // No uses finish() aquí si quieres que la actividad actual (desde donde se abre el menú)
            // permanezca en la pila si no es la misma que la que se va a iniciar.
            // Solo usa finish() si la actividad actual NO debe permanecer si es diferente de la destino.
            // Para un menú lateral, usualmente NO querrás hacer finish() aquí.
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
                        editTextPrecioUnitario.setText("$${"%.2f".format(precio)}")
                    }

                    imagenBase64 = doc.getString("imagenBase64")
                    try {
                        val bytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageViewProducto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imageViewProducto.setImageResource(R.drawable.logo_floralia)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar producto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarProducto() {
        val nombre = editTextNombre.text.toString().trim()
        val cantidad = editTextCantidad.text.toString().toIntOrNull()
        val precio = obtenerPrecioComoDouble(editTextPrecioUnitario.text.toString())

        if (nombre.isEmpty() || cantidad == null || precio == null || imagenBase64 == null) {
            Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = mapOf(
            "nombre" to nombre,
            "cantidad" to cantidad,
            "precioUnitario" to precio,
            "imagenBase64" to imagenBase64!!
        )

        progressDialog.setMessage("Editando producto...")
        progressDialog.show()

        db.collection("productos").document(idProducto).update(datos)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_LONG).show()
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

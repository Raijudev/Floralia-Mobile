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
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.firestore.FirebaseFirestore
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
        imageViewMenuLogo = findViewById(R.id.imageViewMenuLogo)

        menuAgregarUsuario = findViewById(R.id.menuAgregarUsuario)
        menuProductos = findViewById(R.id.menuProductos)
        menuAgregarProducto = findViewById(R.id.menuAgregarProducto)
        menuPedidos = findViewById(R.id.menuPedidos)
        menuUsuarios = findViewById(R.id.menuUsuarios)
        menuCortesdeCaja = findViewById(R.id.menuCortesdeCaja)
        menuInfoApp = findViewById(R.id.menuInfoApp)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)

        configurarFormatoPrecioMXN()

        // Botón de retroceso
        imageViewBack.setOnClickListener { finish() }

        // Abrir menú lateral al hacer clic en el logo (imageViewMenu)
        imageViewMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Cerrar menú al hacer clic en logo dentro del drawer
        imageViewMenuLogo.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        // Configurar clicks en opciones del menú
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
            startActivity(Intent(this, InfoAppActivity::class.java))
            finish()
        }

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

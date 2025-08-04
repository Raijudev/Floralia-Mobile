package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String
    private var idTarjetaAnterior: String? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_usuario)

        db = FirebaseFirestore.getInstance()
        uid = intent.getStringExtra("uid") ?: return finish()

        // Inicializar ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Editando Usuario...")
        progressDialog.setCancelable(false)

        // UI
        val nombre = findViewById<EditText>(R.id.editTextNombre)
        val apellido = findViewById<EditText>(R.id.editTextApellido)
        val telefono = findViewById<EditText>(R.id.editTextTelefono)
        val domicilio = findViewById<EditText>(R.id.editTextDomicilio)
        val curp = findViewById<EditText>(R.id.editTextCurp)
        val spinnerRol = findViewById<Spinner>(R.id.spinnerRol)
        val idTarjeta = findViewById<EditText>(R.id.editTextIdTarjeta)
        val puntos = findViewById<EditText>(R.id.editTextPuntos)
        val fechaActivacion = findViewById<EditText>(R.id.editTextFechaActivacion)
        val fechaVencimiento = findViewById<EditText>(R.id.editTextFechaVencimiento)
        val btnActualizar = findViewById<Button>(R.id.buttonGuardarCambios)

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

        val roles = arrayOf("Administrador", "Empleado", "Cliente")
        spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        // Cargar datos usuario
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nombre.setText(doc.getString("nombre"))
                    apellido.setText(doc.getString("apellido"))
                    telefono.setText(doc.getString("telefono"))
                    domicilio.setText(doc.getString("domicilio"))
                    curp.setText(doc.getString("curp"))
                    idTarjetaAnterior = doc.getString("idTarjetaNFC")
                    idTarjeta.setText(idTarjetaAnterior)

                    if (!idTarjetaAnterior.isNullOrEmpty()) {
                        puntos.setText(doc.getLong("puntos")?.toString())
                        doc.getTimestamp("fechaActivacion")?.let {
                            fechaActivacion.setText(dateFormat.format(it.toDate()))
                        } ?: fechaActivacion.setText("")
                        doc.getTimestamp("fechaVencimiento")?.let {
                            fechaVencimiento.setText(dateFormat.format(it.toDate()))
                        } ?: fechaVencimiento.setText("")
                    } else {
                        puntos.setText("")
                        fechaActivacion.setText("")
                        fechaVencimiento.setText("")
                    }

                    spinnerRol.setSelection(roles.indexOf(doc.getString("rol")))
                }
            }

        val uidDelUsuarioActual = intent.getStringExtra("uid") ?: ""

        idTarjeta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val tarjetaId = s.toString().trim()
                if (tarjetaId.isNotEmpty()) {
                    db.collection("tarjetas_nfc")
                        .whereEqualTo("idTarjetaNFC", tarjetaId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val tarjetaDoc = querySnapshot.documents[0]
                                val estado = tarjetaDoc.getString("estado") ?: "Desconocido"
                                val asignadoAUid = tarjetaDoc.getString("asignadoAUid") ?: ""

                                val esMismaPersona = asignadoAUid == uidDelUsuarioActual

                                if ((estado == "Asignada" || estado == "Cancelada") && !esMismaPersona) {
                                    Toast.makeText(
                                        this@EditarUsuarioActivity,
                                        if (estado == "Asignada")
                                            "La tarjeta ya está asignada a otro usuario"
                                        else
                                            "Esta tarjeta ha sido cancelada y no puede usarse",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    idTarjeta.post { idTarjeta.setText("") }
                                    puntos.setText("")
                                    fechaActivacion.setText("")
                                    fechaVencimiento.setText("")
                                } else {
                                    val puntosValue = tarjetaDoc.getLong("puntos") ?: 0
                                    val activacionTS = tarjetaDoc.getTimestamp("fechaActivacion")
                                    val vencimientoTS = tarjetaDoc.getTimestamp("fechaVencimiento")

                                    puntos.setText(puntosValue.toString())
                                    fechaActivacion.setText(
                                        activacionTS?.toDate()?.let { dateFormat.format(it) } ?: ""
                                    )
                                    fechaVencimiento.setText(
                                        vencimientoTS?.toDate()?.let { dateFormat.format(it) } ?: ""
                                    )
                                }
                            } else {
                                puntos.setText("")
                                fechaActivacion.setText("")
                                fechaVencimiento.setText("")
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@EditarUsuarioActivity,
                                "Error al buscar tarjeta: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    puntos.setText("")
                    fechaActivacion.setText("")
                    fechaVencimiento.setText("")
                }
            }
        })

        btnActualizar.text = "Actualizar"
        btnActualizar.setOnClickListener {
            val nombreStr = nombre.text.toString().trim()
            val apellidoStr = apellido.text.toString().trim()
            val rolStr = spinnerRol.selectedItem.toString()
            val nuevaIdTarjeta = idTarjeta.text.toString().trim()

            if (nombreStr.isEmpty() || apellidoStr.isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val puntosInt = puntos.text.toString().toIntOrNull()
            val fechaActivacionTimestamp = fechaActivacion.text.toString().takeIf { it.isNotEmpty() }?.let {
                Timestamp(dateFormat.parse(it)!!)
            }
            val fechaVencimientoTimestamp = fechaVencimiento.text.toString().takeIf { it.isNotEmpty() }?.let {
                Timestamp(dateFormat.parse(it)!!)
            }

            val userData = mutableMapOf<String, Any>(
                "nombre" to nombreStr,
                "apellido" to apellidoStr,
                "telefono" to telefono.text.toString().trim(),
                "domicilio" to domicilio.text.toString().trim(),
                "rol" to rolStr,
                "idTarjetaNFC" to nuevaIdTarjeta
            )
            curp.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
                userData["curp"] = it
            }
            puntosInt?.let { userData["puntos"] = it }
            fechaActivacionTimestamp?.let { userData["fechaActivacion"] = it }
            fechaVencimientoTimestamp?.let { userData["fechaVencimiento"] = it }

            progressDialog.show()
            btnActualizar.isEnabled = false

            db.collection("usuarios").document(uid).update(userData)
                .addOnSuccessListener {
                    // 1. Liberar tarjeta anterior si cambió o fue eliminada
                    if (!idTarjetaAnterior.isNullOrEmpty() && idTarjetaAnterior != nuevaIdTarjeta) {
                        db.collection("tarjetas_nfc")
                            .whereEqualTo("idTarjetaNFC", idTarjetaAnterior)
                            .get()
                            .addOnSuccessListener { query ->
                                if (!query.isEmpty) {
                                    val tarjetaRef = query.documents[0].reference
                                    tarjetaRef.update(
                                        mapOf(
                                            "estado" to "Disponible",
                                            "asignadoAUid" to com.google.firebase.firestore.FieldValue.delete()
                                        )
                                    )
                                }
                            }
                    }

                    // 2. Asignar nueva tarjeta si es distinta a la anterior
                    if (nuevaIdTarjeta.isNotEmpty() && nuevaIdTarjeta != idTarjetaAnterior) {
                        db.collection("tarjetas_nfc")
                            .whereEqualTo("idTarjetaNFC", nuevaIdTarjeta)
                            .get()
                            .addOnSuccessListener { query ->
                                if (!query.isEmpty) {
                                    val tarjetaRef = query.documents[0].reference
                                    tarjetaRef.update(
                                        mapOf(
                                            "estado" to "Asignada",
                                            "asignadoAUid" to uid
                                        )
                                    )
                                }
                            }
                    }

                    progressDialog.dismiss()
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    btnActualizar.isEnabled = true
                    Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<ImageView>(R.id.imageViewBack).setOnClickListener {
            finish()
        }
    }
}

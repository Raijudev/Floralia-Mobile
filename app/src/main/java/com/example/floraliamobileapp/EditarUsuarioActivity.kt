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

    // Handler y Runnable para implementar el "debounce"
    private val validationHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var validationRunnable: Runnable? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_usuario)

        db = FirebaseFirestore.getInstance()
        uid = intent.getStringExtra("uid") ?: run {
            Toast.makeText(this, "No se pudo cargar la información del usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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
                    println("Error al obtener el rol del usuario: $exception")
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

        val roles = arrayOf("Administrador", "Empleado")
        spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

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
                } else {
                    Toast.makeText(this, "El usuario no fue encontrado en la base de datos.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar la información del usuario: ${e.message}.", Toast.LENGTH_LONG).show()
                finish()
            }

        val uidDelUsuarioActual = intent.getStringExtra("uid") ?: ""

        idTarjeta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validationRunnable?.let { validationHandler.removeCallbacks(it) }

                val tarjetaId = s.toString().trim()
                if (tarjetaId.isEmpty()) {
                    idTarjeta.error = null // Limpia el error cuando el campo está vacío
                    puntos.setText("")
                    fechaActivacion.setText("")
                    fechaVencimiento.setText("")
                    return
                }

                validationRunnable = Runnable {
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
                                    idTarjeta.error = if (estado == "Asignada")
                                        "La tarjeta ya está asignada a otro usuario."
                                    else
                                        "Esta tarjeta ha sido cancelada y no puede ser utilizada."

                                    idTarjeta.postDelayed({
                                        idTarjeta.setText("")
                                        idTarjeta.error = null
                                    }, 500)

                                    puntos.setText("")
                                    fechaActivacion.setText("")
                                    fechaVencimiento.setText("")
                                } else {
                                    idTarjeta.error = null // Limpia el error si la tarjeta es válida
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
                                idTarjeta.error = "La tarjeta ingresada no existe." // Muestra el error
                                puntos.setText("")
                                fechaActivacion.setText("")
                                fechaVencimiento.setText("")
                            }
                        }
                        .addOnFailureListener { e ->
                            idTarjeta.error = "Error al verificar la tarjeta: ${e.message}."
                        }
                }
                validationHandler.postDelayed(validationRunnable!!, 500)
            }
        })

        btnActualizar.text = "Actualizar"
        btnActualizar.setOnClickListener {
            // Limpia los errores previos antes de validar
            nombre.error = null
            apellido.error = null
            telefono.error = null
            domicilio.error = null
            puntos.error = null
            fechaActivacion.error = null
            fechaVencimiento.error = null

            val nombreStr = nombre.text.toString().trim()
            val apellidoStr = apellido.text.toString().trim()
            val telefonoStr = telefono.text.toString().trim()
            val domicilioStr = domicilio.text.toString().trim()
            val rolStr = spinnerRol.selectedItem.toString()
            val nuevaIdTarjeta = idTarjeta.text.toString().trim()

            var isValid = true

            // Validaciones de campos
            if (nombreStr.isEmpty()) {
                nombre.error = "Campo obligatorio"
                isValid = false
            }
            if (apellidoStr.isEmpty()) {
                apellido.error = "Campo obligatorio"
                isValid = false
            }
            if (telefonoStr.isEmpty()) {
                telefono.error = "Campo obligatorio"
                isValid = false
            } else if (telefonoStr.length < 10) {
                telefono.error = "Debe tener 10 dígitos"
                isValid = false
            }
            if (domicilioStr.isEmpty()) {
                domicilio.error = "Campo obligatorio"
                isValid = false
            }

            val puntosInt = puntos.text.toString().toIntOrNull()
            if (puntos.text.isNotEmpty() && puntosInt == null) {
                puntos.error = "Debe ser un número entero"
                isValid = false
            }

            val fechaActivacionTimestamp = fechaActivacion.text.toString().takeIf { it.isNotEmpty() }?.let {
                try {
                    dateFormat.parse(it)?.let { date -> Timestamp(date) }
                } catch (e: Exception) {
                    fechaActivacion.error = "Formato no válido (dd/MM/yyyy)"
                    isValid = false
                    null
                }
            }
            val fechaVencimientoTimestamp = fechaVencimiento.text.toString().takeIf { it.isNotEmpty() }?.let {
                try {
                    dateFormat.parse(it)?.let { date -> Timestamp(date) }
                } catch (e: Exception) {
                    fechaVencimiento.error = "Formato no válido (dd/MM/yyyy)"
                    isValid = false
                    null
                }
            }

            if (!isValid) {
                return@setOnClickListener
            }

            val userData = mutableMapOf<String, Any>(
                "nombre" to nombreStr,
                "apellido" to apellidoStr,
                "telefono" to telefonoStr,
                "domicilio" to domicilioStr,
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
                    Toast.makeText(this, "La información del usuario se actualizó correctamente.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    btnActualizar.isEnabled = true
                    Toast.makeText(this, "Error al actualizar el usuario: ${e.message}.", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<ImageView>(R.id.imageViewBack).setOnClickListener {
            finish()
        }
    }
}

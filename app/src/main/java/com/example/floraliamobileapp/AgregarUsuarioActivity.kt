package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.drawerlayout.widget.DrawerLayout
import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AgregarUsuarioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var imageViewBack: ImageView
    private lateinit var progressDialog: ProgressDialog

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Handler y Runnable para implementar el "debounce"
    private val validationHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var validationRunnable: Runnable? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_usuario)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        imageViewBack = findViewById(R.id.imageViewBack)
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
            startActivity(Intent(this, InfoAppActivity::class.java))
            finish()
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Agregando usuario...")
        progressDialog.setCancelable(false)

        val nombre = findViewById<EditText>(R.id.editTextNombre)
        val apellido = findViewById<EditText>(R.id.editTextApellido)
        val telefono = findViewById<EditText>(R.id.editTextTelefono)
        val domicilio = findViewById<EditText>(R.id.editTextDomicilio)
        val correo = findViewById<EditText>(R.id.editTextCorreo)
        val contrasena = findViewById<EditText>(R.id.editTextContraseña)
        val curp = findViewById<EditText>(R.id.editTextCurp)
        val spinnerRol = findViewById<Spinner>(R.id.spinnerRol)
        val idTarjeta = findViewById<EditText>(R.id.editTextIdTarjeta)
        val puntos = findViewById<EditText>(R.id.editTextPuntos)
        val fechaActivacion = findViewById<EditText>(R.id.editTextFechaActivacion)
        val fechaVencimiento = findViewById<EditText>(R.id.editTextFechaVencimiento)
        val btnAgregar = findViewById<Button>(R.id.buttonAgregarUsuario)

        val roles = arrayOf("Administrador", "Empleado")
        spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        idTarjeta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validationRunnable?.let { validationHandler.removeCallbacks(it) }

                val tarjetaId = s.toString().trim()
                if (tarjetaId.isEmpty()) {
                    idTarjeta.error = null
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

                                if (estado == "Asignada" || estado == "Cancelada") {
                                    val mensajeError = if (estado == "Asignada") {
                                        "Esta tarjeta ya ha sido asignada a otro usuario."
                                    } else {
                                        "Esta tarjeta está cancelada y no se puede usar."
                                    }
                                    idTarjeta.error = mensajeError

                                    idTarjeta.postDelayed({
                                        idTarjeta.setText("")
                                        idTarjeta.error = null
                                    }, 500)

                                    puntos.setText("")
                                    fechaActivacion.setText("")
                                    fechaVencimiento.setText("")
                                } else {
                                    idTarjeta.error = null
                                    val puntosValue = tarjetaDoc.getLong("puntos") ?: 0
                                    val activacionTS = tarjetaDoc.getTimestamp("fechaActivacion")
                                    val vencimientoTS = tarjetaDoc.getTimestamp("fechaVencimiento")

                                    puntos.setText(puntosValue.toString())
                                    activacionTS?.let {
                                        fechaActivacion.setText(dateFormat.format(it.toDate()))
                                    } ?: fechaActivacion.setText("")
                                    vencimientoTS?.let {
                                        fechaVencimiento.setText(dateFormat.format(it.toDate()))
                                    } ?: fechaVencimiento.setText("")
                                }
                            } else {
                                idTarjeta.error = "La tarjeta ingresada no existe."
                                puntos.setText("")
                                fechaActivacion.setText("")
                                fechaVencimiento.setText("")
                            }
                        }
                        .addOnFailureListener {
                            idTarjeta.error = "Error al buscar la tarjeta: ${it.message}."
                        }
                }
                validationHandler.postDelayed(validationRunnable!!, 500)
            }
        })

        btnAgregar.setOnClickListener {
            // Limpia errores previos al intentar agregar
            nombre.error = null
            apellido.error = null
            telefono.error = null
            domicilio.error = null
            correo.error = null
            contrasena.error = null
            puntos.error = null
            fechaActivacion.error = null
            fechaVencimiento.error = null

            val name = nombre.text.toString().trim()
            val lastName = apellido.text.toString().trim()
            val tel = telefono.text.toString().trim()
            val dom = domicilio.text.toString().trim()
            val email = correo.text.toString().trim()
            val pass = contrasena.text.toString().trim()
            val curpText = curp.text.toString().trim()
            val rolSeleccionado = spinnerRol.selectedItem.toString()
            val idTarjetaNFC = idTarjeta.text.toString().trim()
            val puntosStr = puntos.text.toString().trim()
            val fechaActivacionStr = fechaActivacion.text.toString().trim()
            val fechaVencimientoStr = fechaVencimiento.text.toString().trim()

            var isValid = true

            if (name.isEmpty()) {
                nombre.error = "Campo obligatorio"
                isValid = false
            }
            if (lastName.isEmpty()) {
                apellido.error = "Campo obligatorio"
                isValid = false
            }
            if (tel.isEmpty()) {
                telefono.error = "Campo obligatorio"
                isValid = false
            }
            if (tel.length < 10) {
                telefono.error = "Debe tener 10 dígitos"
                isValid = false
            }
            if (dom.isEmpty()) {
                domicilio.error = "Campo obligatorio"
                isValid = false
            }
            if (email.isEmpty()) {
                correo.error = "Campo obligatorio"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                correo.error = "Formato de correo no válido"
                isValid = false
            }
            if (pass.isEmpty()) {
                contrasena.error = "Campo obligatorio"
                isValid = false
            } else if (pass.length < 6) {
                contrasena.error = "Mínimo 6 caracteres"
                isValid = false
            }

            val fechaActivacionTS = fechaActivacionStr.takeIf { it.isNotEmpty() }?.let {
                try {
                    Timestamp(dateFormat.parse(it)!!)
                } catch (e: Exception) {
                    fechaActivacion.error = "Formato no válido (dd/MM/yyyy)"
                    isValid = false
                    null
                }
            }
            val fechaVencimientoTS = fechaVencimientoStr.takeIf { it.isNotEmpty() }?.let {
                try {
                    Timestamp(dateFormat.parse(it)!!)
                } catch (e: Exception) {
                    fechaVencimiento.error = "Formato no válido (dd/MM/yyyy)"
                    isValid = false
                    null
                }
            }

            val puntosInt = puntosStr.takeIf { it.isNotEmpty() }?.toIntOrNull()
            if (puntosStr.isNotEmpty() && puntosInt == null) {
                puntos.error = "Debe ser un número entero"
                isValid = false
            }

            if (!isValid) {
                // Si la validación falla, no se continúa
                return@setOnClickListener
            }

            // Continuar con el proceso de creación de usuario si la validación es exitosa
            progressDialog.show()
            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (!signInMethods.isNullOrEmpty()) {
                        progressDialog.dismiss()
                        correo.error = "Este correo ya está registrado"
                    } else {
                        if (idTarjetaNFC.isNotEmpty()) {
                            db.collection("tarjetas_nfc")
                                .whereEqualTo("idTarjetaNFC", idTarjetaNFC)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val doc = querySnapshot.documents[0]
                                        if (doc.getString("estado") == "Asignada") {
                                            progressDialog.dismiss()
                                            idTarjeta.error = "Tarjeta asignada a otro usuario"
                                        } else {
                                            crearUsuarioFirebase(name, lastName, tel, dom, email, pass, rolSeleccionado, curpText, idTarjetaNFC, puntosInt, fechaActivacionTS, fechaVencimientoTS)
                                        }
                                    } else {
                                        progressDialog.dismiss()
                                        idTarjeta.error = "La tarjeta no existe"
                                    }
                                }
                                .addOnFailureListener {
                                    progressDialog.dismiss()
                                    idTarjeta.error = "Error al validar la tarjeta"
                                }
                        } else {
                            crearUsuarioFirebase(name, lastName, tel, dom, email, pass, rolSeleccionado, curpText, null, puntosInt, fechaActivacionTS, fechaVencimientoTS)
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al verificar el correo: ${task.exception?.message}.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun crearUsuarioFirebase(
        nombre: String, apellido: String, telefono: String, domicilio: String,
        correo: String, contrasena: String, rol: String, curp: String?,
        idTarjetaNFC: String?, puntos: Int?, fechaActivacion: Timestamp?, fechaVencimiento: Timestamp?
    ) {
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val userData = mutableMapOf<String, Any>(
                    "nombre" to nombre, "apellido" to apellido,
                    "telefono" to telefono, "domicilio" to domicilio, "correo" to correo, "rol" to rol
                )
                curp?.let { if (it.isNotEmpty()) userData["curp"] = it }
                idTarjetaNFC?.let { if (it.isNotEmpty()) userData["idTarjetaNFC"] = it }
                puntos?.let { userData["puntos"] = it }
                fechaActivacion?.let { userData["fechaActivacion"] = it }
                fechaVencimiento?.let { userData["fechaVencimiento"] = it }

                db.collection("usuarios").document(uid).set(userData)
                    .addOnSuccessListener {
                        if (!idTarjetaNFC.isNullOrEmpty()) {
                            db.collection("tarjetas_nfc")
                                .whereEqualTo("idTarjetaNFC", idTarjetaNFC)
                                .get()
                                .addOnSuccessListener { snap ->
                                    if (!snap.isEmpty) {
                                        val tarjetaRef = snap.documents[0].reference
                                        tarjetaRef.update(
                                            mapOf(
                                                "estado" to "Asignada",
                                                "asignadoAUid" to uid
                                            )
                                        )
                                    }
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Usuario agregado correctamente.", Toast.LENGTH_LONG).show()
                                    limpiarCampos()
                                }
                                .addOnFailureListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "El usuario fue creado, pero hubo un error al asignar la tarjeta: ${it.message}.", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Usuario agregado correctamente.", Toast.LENGTH_LONG).show()
                            limpiarCampos()
                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error al guardar la información del usuario: ${it.message}.", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Error al crear la cuenta de usuario: ${it.message}.", Toast.LENGTH_LONG).show()
            }
    }

    private fun limpiarCampos() {
        findViewById<EditText>(R.id.editTextNombre).text.clear()
        findViewById<EditText>(R.id.editTextApellido).text.clear()
        findViewById<EditText>(R.id.editTextTelefono).text.clear()
        findViewById<EditText>(R.id.editTextDomicilio).text.clear()
        findViewById<EditText>(R.id.editTextCorreo).text.clear()
        findViewById<EditText>(R.id.editTextContraseña).text.clear()
        findViewById<EditText>(R.id.editTextCurp).text.clear()
        findViewById<EditText>(R.id.editTextIdTarjeta).text.clear()
        findViewById<EditText>(R.id.editTextPuntos).text.clear()
        findViewById<EditText>(R.id.editTextFechaActivacion).text.clear()
        findViewById<EditText>(R.id.editTextFechaVencimiento).text.clear()
        findViewById<Spinner>(R.id.spinnerRol).setSelection(0)
    }
}

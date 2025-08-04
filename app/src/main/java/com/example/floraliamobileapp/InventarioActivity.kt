package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class InventarioActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private val listaProductos = mutableListOf<Producto>()
    private val listaProductosFiltrada = mutableListOf<Producto>()

    private lateinit var imageViewBack: ImageView
    private lateinit var imageViewAbrirAgregarProducto: ImageView
    private lateinit var editTextBuscarProducto: EditText
    private lateinit var textViewSinResultados: TextView

    // Variable para almacenar el rol del usuario
    private var currentUserRole: String? = null // <- NUEVO: Variable para el rol

    private val agregarProductoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cargarProductos()
        }
    }

    private val editarProductoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cargarProductos()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        recyclerView = findViewById(R.id.recyclerViewInventario)
        recyclerView.layoutManager = LinearLayoutManager(this)

        editTextBuscarProducto = findViewById(R.id.editTextBuscarProducto)
        textViewSinResultados = findViewById(R.id.textViewSinResultados)

        editTextBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarProductos(s.toString())
            }
        })

        // --- IMPORTANTE: La inicialización del adaptador se moverá después de obtener el rol ---
        // adapter = ProductoAdapter(listaProductosFiltrada) { producto -> ... }

        // --- Inicio del fragmento de código del menú lateral (con una pequeña modificación) ---
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

        // --- Lógica de validación de rol para el menú y otras funcionalidades (MODIFICADO) ---
        imageViewAbrirAgregarProducto = findViewById(R.id.imageViewAbrirAgregarProducto) // Asegurarse de que esté inicializado aquí

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val rol = document.getString("rol")
                        currentUserRole = rol // <- NUEVO: Asignar el rol a la variable de la actividad

                        if (rol == "Administrador") {
                            menuUsuarios.visibility = View.VISIBLE
                            imageViewAbrirAgregarProducto.visibility = View.VISIBLE // <- NUEVO: Mostrar botón para admin
                            // Si es administrador, permite el click en las tarjetas
                            adapter = ProductoAdapter(listaProductosFiltrada, rol) { producto ->
                                val intent = Intent(this, EditarProductoActivity::class.java).apply {
                                    putExtra("idProducto", producto.uid)
                                }
                                editarProductoLauncher.launch(intent)
                            }
                        } else if (rol == "Empleado") {
                            menuUsuarios.visibility = View.GONE
                            imageViewAbrirAgregarProducto.visibility = View.GONE // <- NUEVO: Ocultar botón para empleado
                            // Si es empleado, no permite el click en las tarjetas (se pasa null para el click listener)
                            adapter = ProductoAdapter(listaProductosFiltrada, rol) { /* No hacer nada */ }
                        }
                        recyclerView.adapter = adapter // <- Mover aquí la asignación del adapter
                        cargarProductos() // <- Mover aquí para asegurar que el adaptador está configurado
                    } else {
                        // Documento del usuario no existe, ocultar por seguridad
                        menuUsuarios.visibility = View.GONE
                        imageViewAbrirAgregarProducto.visibility = View.GONE
                        // En caso de error o sin rol, el adaptador no permitirá clicks por seguridad
                        adapter = ProductoAdapter(listaProductosFiltrada, null) { /* No hacer nada */ }
                        recyclerView.adapter = adapter
                        cargarProductos()
                    }
                }
                .addOnFailureListener { exception ->
                    // Error al obtener el rol, ocultar por seguridad
                    println("Error al obtener el rol del usuario: $exception")
                    menuUsuarios.visibility = View.GONE
                    imageViewAbrirAgregarProducto.visibility = View.GONE
                    // En caso de error, el adaptador no permitirá clicks por seguridad
                    adapter = ProductoAdapter(listaProductosFiltrada, null) { /* No hacer nada */ }
                    recyclerView.adapter = adapter
                    cargarProductos()
                }
        } else {
            // No hay usuario logeado, ocultar por seguridad
            menuUsuarios.visibility = View.GONE
            imageViewAbrirAgregarProducto.visibility = View.GONE
            // Sin usuario logeado, el adaptador no permitirá clicks por seguridad
            adapter = ProductoAdapter(listaProductosFiltrada, null) { /* No hacer nada */ }
            recyclerView.adapter = adapter
            cargarProductos()
        }
        // --- Fin de la lógica de validación de rol ---

        // --- Resaltar la opción del menú actual ---
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
        // --- Fin de la lógica de resaltado ---

        menuAgregarUsuario.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, AgregarUsuarioActivity::class.java))
            finish()
        }

        menuProductos.setOnClickListener {
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
        // --- Fin del fragmento de código del menú lateral ---

        // El listener de este botón ahora se configura dependiendo del rol dentro del addOnSuccessListener
        // imageViewAbrirAgregarProducto = findViewById(R.id.imageViewAbrirAgregarProducto)
        imageViewAbrirAgregarProducto.setOnClickListener {
            val intent = Intent(this, AgregarProductoActivity::class.java)
            agregarProductoLauncher.launch(intent)
        }

        imageViewBack = findViewById(R.id.imageViewBack)
        imageViewBack.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cargarProductos() {
        FirebaseFirestore.getInstance().collection("productos")
            .get()
            .addOnSuccessListener { snapshot ->
                listaProductos.clear()
                for (documento in snapshot) {
                    val producto = documento.toObject(Producto::class.java).copy(uid = documento.id)
                    listaProductos.add(producto)
                }
                listaProductosFiltrada.clear()
                listaProductosFiltrada.addAll(listaProductos)
                adapter.notifyDataSetChanged()
                mostrarMensajeSiNoHayResultados()
            }
            .addOnFailureListener {
                // Manejar error si es necesario
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filtrarProductos(texto: String) {
        val filtro = texto.lowercase().trim()
        listaProductosFiltrada.clear()

        if (filtro.isEmpty()) {
            listaProductosFiltrada.addAll(listaProductos)
        } else {
            listaProductosFiltrada.addAll(listaProductos.filter {
                it.uid.lowercase().contains(filtro) ||
                        it.nombre.lowercase().contains(filtro) ||
                        it.cantidad.toString().contains(filtro) ||
                        it.precioUnitario.toString().contains(filtro)
            })
        }

        adapter.notifyDataSetChanged()
        mostrarMensajeSiNoHayResultados()
    }

    private fun mostrarMensajeSiNoHayResultados() {
        if (listaProductosFiltrada.isEmpty()) {
            textViewSinResultados.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textViewSinResultados.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}

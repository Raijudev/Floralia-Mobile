package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class GestionUsuariosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextBuscarUsuario: EditText
    private lateinit var textViewSinResultados: TextView
    private lateinit var usuarioAdapter: UsuarioAdapter

    private val listaUsuarios = mutableListOf<Usuario>()
    private var listaFiltradaUsuarios = mutableListOf<Usuario>()

    private val db = FirebaseFirestore.getInstance()
    private lateinit var editarUsuarioLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_usuarios)

        recyclerView = findViewById(R.id.recyclerViewUsuarios)
        editTextBuscarUsuario = findViewById(R.id.editTextBuscarUsuario)
        textViewSinResultados = findViewById(R.id.textViewSinResultados)

        recyclerView.layoutManager = LinearLayoutManager(this)

        editarUsuarioLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                cargarUsuariosDesdeFirestore()
            }
        }

        usuarioAdapter = UsuarioAdapter(listaFiltradaUsuarios) { usuario ->
            val intent = Intent(this, EditarUsuarioActivity::class.java)
            intent.putExtra("uid", usuario.uid)
            editarUsuarioLauncher.launch(intent)
        }

        recyclerView.adapter = usuarioAdapter

        findViewById<ImageView>(R.id.imageViewBack).setOnClickListener {
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
            // Ya estás en esta pantalla, solo cierra el menú
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

        editTextBuscarUsuario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarUsuarios(s.toString())
            }
        })

        cargarUsuariosDesdeFirestore()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cargarUsuariosDesdeFirestore() {
        db.collection("usuarios").get()
            .addOnSuccessListener { documentos ->
                listaUsuarios.clear()
                for (doc in documentos) {
                    val usuario = doc.toObject(Usuario::class.java)
                    usuario.uid = doc.id
                    listaUsuarios.add(usuario)
                }

                listaFiltradaUsuarios.clear()
                listaFiltradaUsuarios.addAll(listaUsuarios)
                usuarioAdapter.notifyDataSetChanged()
                textViewSinResultados.visibility = View.GONE
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filtrarUsuarios(texto: String) {
        val filtro = texto.lowercase().trim()
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        listaFiltradaUsuarios.clear()

        if (filtro.isEmpty()) {
            listaFiltradaUsuarios.addAll(listaUsuarios)
        } else {
            listaFiltradaUsuarios.addAll(
                listaUsuarios.filter {
                    val nombreCompleto = "${it.nombre} ${it.apellido}".lowercase()

                    nombreCompleto.contains(filtro) ||
                            it.uid.lowercase().contains(filtro) ||
                            it.nombre.lowercase().contains(filtro) ||
                            it.apellido.lowercase().contains(filtro) ||
                            it.telefono.lowercase().contains(filtro) ||
                            it.domicilio.lowercase().contains(filtro) ||
                            it.correo.lowercase().contains(filtro) ||
                            it.curp?.lowercase()?.contains(filtro) ?: false ||
                            it.rol.lowercase().contains(filtro) ||
                            it.idTarjetaNFC?.lowercase()?.contains(filtro) ?: false ||
                            it.puntos?.toString()?.contains(filtro) ?: false ||
                            it.fechaActivacion?.let { ts -> formato.format(ts.toDate()).contains(filtro) } ?: false ||
                            it.fechaVencimiento?.let { ts -> formato.format(ts.toDate()).contains(filtro) } ?: false
                }
            )
        }

        usuarioAdapter.notifyDataSetChanged()

        textViewSinResultados.visibility = if (listaFiltradaUsuarios.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

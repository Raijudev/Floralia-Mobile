package com.example.floraliamobileapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CortesDeCajaActivity : AppCompatActivity() {

    private lateinit var recyclerCortes: RecyclerView
    private lateinit var editTextBuscar: EditText
    private lateinit var textViewSinResultados: TextView
    private val listaCortes = mutableListOf<CorteCaja>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adaptador: CorteCajaAdapter

    // Constante para el código de solicitud de permiso
    private val STORAGE_PERMISSION_CODE = 101

    // Fuentes personalizadas adaptadas para el nuevo estilo
    private val COLOR_PRIMARY_BLUE = BaseColor(236, 0, 140) // Azul vibrante (similar a #1A73E8)
    private val COLOR_BLACK = BaseColor.BLACK // Negro puro (RGB 0, 0, 0)

    // Colores de gris separados para bordes y texto de contenido
    private val COLOR_BORDER_GREY = BaseColor(220, 220, 220) // Gris más claro para los bordes
    private val COLOR_CONTENT_TEXT_GREY = BaseColor(90, 90, 90) // Gris más oscuro para el texto del contenido

    private val COLOR_LIGHT_GREY_BG = BaseColor(245, 245, 245) // Fondo de filas alternas (similar a #F5F5F5)
    private val COLOR_WHITE_TEXT = BaseColor.WHITE

    private val FONT_TITLE = Font(Font.FontFamily.HELVETICA, 22f, Font.BOLD, COLOR_BLACK)
    private val FONT_SUBTITLE = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, COLOR_BLACK)
    private val FONT_HEADER = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, COLOR_WHITE_TEXT)

    // FONT_CELL_LABEL (para las etiquetas de la primera columna en info/resumen) ahora en gris oscuro
    private val FONT_CELL_LABEL = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, COLOR_CONTENT_TEXT_GREY)
    // FONT_CELL_VALUE (para los valores de las celdas) ahora en gris oscuro
    private val FONT_CELL_VALUE = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, COLOR_CONTENT_TEXT_GREY)

    private val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

    // Variable para almacenar el UID del corte para el que se solicitó el permiso
    private var pendingCorteUidForPdf: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cortes_de_caja)

        recyclerCortes = findViewById(R.id.recyclerCortes)
        editTextBuscar = findViewById(R.id.editTextBuscarCorte)
        textViewSinResultados = findViewById(R.id.textViewSinResultados)
        val btnBack: ImageView = findViewById(R.id.imageViewBack)

        recyclerCortes.layoutManager = LinearLayoutManager(this)

        db.collection("cortes_de_caja")
            .orderBy("fechaFinal", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listaCortes.clear()
                for (document in result) {
                    val corte = document.toObject(CorteCaja::class.java).copy(uid = document.id)
                    listaCortes.add(corte)
                }
                adaptador = CorteCajaAdapter(listaCortes.toMutableList()) { selectedCorte ->
                    pendingCorteUidForPdf = selectedCorte.uid
                    checkAndRequestPermission()

                }
                recyclerCortes.adapter = adaptador
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar cortes: ${exception.message}", Toast.LENGTH_LONG).show()
            }


        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString().lowercase(Locale.getDefault())
                filtrarCortes(texto)
            }
        })

        btnBack.setOnClickListener {
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
            startActivity(Intent(this, AgregarUsuarioActivity::class.java))
            finish()
        }

        menuProductos.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, InventarioActivity::class.java))
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
            // Ya estás en esta pantalla, solo cierra el menú
        }

        menuInfoApp.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, InfoAppActivity::class.java))
            finish()
        }
// --- Fin del fragmento de código del menú lateral ---
    }

    private fun filtrarCortes(texto: String) {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        val listaFiltrada = listaCortes.filter { corte ->
            val textoCompleto = buildString {
                append(corte.uid).append(" ")
                append(corte.generadoPor).append(" ")
                append(corte.totalPedidos).append(" ")
                append(corte.totalVentas).append(" ")
                append(corte.efectivo).append(" ")
                append(corte.transferencia).append(" ")
                append(corte.tarjeta).append(" ")
                append(corte.puntos).append(" ")
                append(corte.puntosGanadosTotal).append(" ")
                append(corte.puntosUsadosTotal).append(" ")
                corte.fechaInicial?.let { append(formatoFecha.format(it.toDate())).append(" ") }
                corte.fechaFinal?.let { append(formatoFecha.format(it.toDate())).append(" ") }
                corte.fechaHoraCreacion?.let { append(formatoFecha.format(it.toDate())).append(" ") }
            }.lowercase()

            textoCompleto.contains(texto)
        }

        adaptador.actualizarLista(listaFiltrada.toMutableList())
        textViewSinResultados.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pendingCorteUidForPdf?.let { generateCorteCajaPdf(it) }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            } else {
                pendingCorteUidForPdf?.let { generateCorteCajaPdf(it) }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pendingCorteUidForPdf?.let { generateCorteCajaPdf(it) }
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado. No se puede generar el PDF.", Toast.LENGTH_LONG).show()
                pendingCorteUidForPdf = null
            }
        }
    }

    private fun generateCorteCajaPdf(corteUid: String) {
        Toast.makeText(this, "Generando PDF...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val corteDoc = db.collection("cortes_de_caja").document(corteUid).get().await()
                val corte = corteDoc.toObject(CorteCaja::class.java)?.copy(uid = corteDoc.id)

                if (corte == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CortesDeCajaActivity, "Error: Corte de caja no encontrado.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val pedidosSnapshot = db.collection("cortes_de_caja").document(corteUid)
                    .collection("pedidos")
                    .get()
                    .await()
                val pedidos = pedidosSnapshot.documents.mapNotNull { it.toObject(PedidoCorte::class.java)?.copy(uid = it.id) }

                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }

                val fileName = "Corte_de_Caja_${corte.uid}.pdf"
                val filePath = File(downloadDir, fileName)
                val document = Document(PageSize.A4, 40f, 40f, 40f, 40f) // Márgenes

                PdfWriter.getInstance(document, FileOutputStream(filePath))
                document.open()

                addHeaderAndLogo(document) // Añade logo y gestiona el espacio para el título principal

                val mainTitle = Paragraph("Detalles del Corte de Caja", FONT_TITLE)
                mainTitle.alignment = Element.ALIGN_LEFT // Alineación a la izquierda
                mainTitle.spacingBefore = 20f // Espacio para bajar el título
                mainTitle.spacingAfter = 30f
                document.add(mainTitle)

                // Información Principal del Corte
                document.add(Paragraph("Información Principal del Corte", FONT_SUBTITLE).apply { spacingAfter = 15f })
                val tableInfoPrincipal = PdfPTable(2).apply {
                    widthPercentage = 100f
                    setSpacingBefore(10f)
                    setSpacingAfter(25f)
                    setWidths(floatArrayOf(1f, 2f))
                }

                addInfoRow(tableInfoPrincipal, "UID Corte", corte.uid)
                addInfoRow(tableInfoPrincipal, "Fecha y Hora Inicial", corte.fechaInicial?.toDate()?.let { sdf.format(it) } ?: "N/A")
                addInfoRow(tableInfoPrincipal, "Fecha y Hora Final", corte.fechaFinal?.toDate()?.let { sdf.format(it) } ?: "N/A")
                addInfoRow(tableInfoPrincipal, "Fecha y Hora de Creación", corte.fechaHoraCreacion?.toDate()?.let { sdf.format(it) } ?: "N/A")
                addInfoRow(tableInfoPrincipal, "Generado Por", corte.generadoPor)
                document.add(tableInfoPrincipal)

                // Resumen de Cotización
                document.add(Paragraph("Resumen de Cotización", FONT_SUBTITLE).apply { spacingAfter = 15f })
                val tableResumen = PdfPTable(2).apply {
                    widthPercentage = 100f
                    setSpacingBefore(10f)
                    setSpacingAfter(25f)
                    setWidths(floatArrayOf(1f, 2f))
                }

                addInfoRow(tableResumen, "Efectivo", "$${String.format("%.2f", corte.efectivo)}")
                addInfoRow(tableResumen, "Tarjeta", "$${String.format("%.2f", corte.tarjeta)}")
                addInfoRow(tableResumen, "Transferencia", "$${String.format("%.2f", corte.transferencia)}")
                addInfoRow(tableResumen, "Puntos (Equivalente)", "${String.format("%.2f", corte.puntos)}")
                addInfoRow(tableResumen, "Puntos Usados Total", "${String.format("%.2f", corte.puntosUsadosTotal)}")
                addInfoRow(tableResumen, "Puntos Ganados Total", "${String.format("%.2f", corte.puntosGanadosTotal)}")
                addInfoRow(tableResumen, "Total de Pedidos", "${corte.totalPedidos}")
                addInfoRow(tableResumen, "Total de Ventas", "$${String.format("%.2f", corte.totalVentas)}")
                document.add(tableResumen)

                // Segunda página: Pedidos del Corte
                document.newPage()

                val pedidosTitle = Paragraph("Pedidos del Corte", FONT_SUBTITLE)
                pedidosTitle.alignment = Element.ALIGN_LEFT // Alineación a la izquierda
                pedidosTitle.spacingAfter = 20f
                document.add(pedidosTitle)

                if (pedidos.isNotEmpty()) {
                    val tablePedidos = PdfPTable(6).apply {
                        widthPercentage = 100f
                        setSpacingBefore(10f)
                        setWidths(floatArrayOf(1.5f, 1.5f, 2f, 1.5f, 1f, 1f))
                        setHeaderRows(1) // Repite el encabezado en nuevas páginas
                    }
                    addTableHeader(tablePedidos, listOf("UID Pedido", "Fecha Creación", "Cliente", "Método Pago", "Estado", "Total Final"))

                    pedidos.forEachIndexed { index, pedido ->
                        addCell(tablePedidos, pedido.uid, index)
                        addCell(tablePedidos, pedido.fechaHoraCreacion?.toDate()?.let { sdf.format(it) } ?: "N/A", index)
                        addCell(tablePedidos, pedido.cliente, index)
                        addCell(tablePedidos, pedido.metodoPago, index)
                        addCell(tablePedidos, pedido.estadoPedido, index)
                        addCell(tablePedidos, "$${String.format("%.2f", pedido.totalFinal)}", index)
                    }
                    document.add(tablePedidos)
                } else {
                    val noPedidos = Paragraph("No hay pedidos registrados para este corte.", FONT_CELL_VALUE)
                    noPedidos.alignment = Element.ALIGN_CENTER
                    document.add(noPedidos)
                }

                document.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CortesDeCajaActivity, "PDF generado en: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
                    openPdf(filePath)
                    pendingCorteUidForPdf = null
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CortesDeCajaActivity, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    // Funciones auxiliares para el PDF

    private fun addTableHeader(table: PdfPTable, headers: List<String>) {
        headers.forEach { header ->
            val cell = PdfPCell(Phrase(header, FONT_HEADER)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                setPaddingTop(10f)
                setPaddingBottom(10f)
                setPaddingLeft(8f)
                setPaddingRight(8f)
                backgroundColor = COLOR_PRIMARY_BLUE
                border = Rectangle.BOX
                borderColor = COLOR_BORDER_GREY // Usa el gris más claro para el borde
                borderWidth = 0.5f
            }
            table.addCell(cell)
        }
    }

    // Usado para la tabla de información principal y resumen
    private fun addInfoRow(table: PdfPTable, label: String, value: String) {
        val rowIndex = table.rows.size
        addCell(table, label, rowIndex, isBold = true)
        addCell(table, value, rowIndex, isBold = false)
    }

    // Versión para celdas de información principal/resumen con bold y fondo alterno
    private fun addCell(table: PdfPTable, text: String, rowIndex: Int, isBold: Boolean) {
        val font = if (isBold) FONT_CELL_LABEL else FONT_CELL_VALUE
        val cell = PdfPCell(Phrase(text, font)).apply {
            horizontalAlignment = Element.ALIGN_LEFT
            verticalAlignment = Element.ALIGN_MIDDLE
            setPaddingTop(8f)
            setPaddingBottom(8f)
            setPaddingLeft(8f)
            setPaddingRight(8f)
            backgroundColor = if (rowIndex % 2 == 0) BaseColor.WHITE else COLOR_LIGHT_GREY_BG
            border = Rectangle.BOX
            borderColor = COLOR_BORDER_GREY // Usa el gris más claro para el borde
            borderWidth = 0.5f
        }
        table.addCell(cell)
    }

    // Versión para celdas de tabla de pedidos con fondo alterno
    private fun addCell(table: PdfPTable, text: String, rowIndex: Int) {
        val cell = PdfPCell(Phrase(text, FONT_CELL_VALUE)).apply {
            horizontalAlignment = Element.ALIGN_LEFT
            verticalAlignment = Element.ALIGN_MIDDLE
            setPaddingTop(8f)
            setPaddingBottom(8f)
            setPaddingLeft(8f)
            setPaddingRight(8f)
            backgroundColor = if (rowIndex % 2 == 0) BaseColor.WHITE else COLOR_LIGHT_GREY_BG
            border = Rectangle.BOX
            borderColor = COLOR_BORDER_GREY // Usa el gris más claro para el borde
            borderWidth = 0.5f
        }
        table.addCell(cell)
    }

    private fun addHeaderAndLogo(document: Document) {
        try {
            val d: Drawable? = ContextCompat.getDrawable(this, R.drawable.logo_floralia1)
            if (d != null) {
                val bitmap: Bitmap = if (d is BitmapDrawable) {
                    d.bitmap
                } else {
                    val bitmap = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    d.setBounds(0, 0, canvas.width, canvas.height)
                    d.draw(canvas)
                    bitmap
                }

                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val image = Image.getInstance(stream.toByteArray())

                val scaleFactor = 0.10f // Reducido el factor de escala para hacerlo más pequeño
                image.scalePercent(scaleFactor * 100)

                // Posicionamiento más preciso en la esquina superior derecha
                val margin = 40f // Margen superior y derecho consistente
                val logoX = PageSize.A4.width - margin - image.scaledWidth
                val logoY = PageSize.A4.height - margin - image.scaledHeight

                image.setAbsolutePosition(logoX, logoY)
                document.add(image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al añadir logo al PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPdf(filePath: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            filePath
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Eliminada la bandera FLAG_ACTIVITY_NO_HISTORY para evitar cierres inesperados
            // addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No hay aplicación para abrir PDFs.", Toast.LENGTH_SHORT).show()
        }
    }
}

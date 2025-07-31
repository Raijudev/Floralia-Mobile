package com.example.floraliamobileapp

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.floraliamobileapp.adapter.PedidoAdapter
import com.example.floraliamobileapp.model.Pedido
import com.example.floraliamobileapp.model.ProductoPedido
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.IOException

// Imports adicionales para el AlertDialog
import android.app.AlertDialog
import android.provider.Settings

class HistorialPedidosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PedidoAdapter
    private lateinit var editTextBuscar: EditText
    private lateinit var textViewSinResultados: TextView

    private val listaPedidos = mutableListOf<Pedido>()
    private val listaPedidosFiltrada = mutableListOf<Pedido>()

    private val db = FirebaseFirestore.getInstance()
    private var pendingPedido: Pedido? = null // Para almacenar el pedido mientras esperamos el resultado del permiso

    private val PERMISSION_REQUEST_CODE = 101 // Código de solicitud para permisos

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_pedidos)

        recyclerView = findViewById(R.id.recyclerViewPedidos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        editTextBuscar = findViewById(R.id.editTextBuscarPedido)
        textViewSinResultados = findViewById(R.id.textViewSinResultados)

        adapter = PedidoAdapter(listaPedidosFiltrada) { pedidoId ->
            val pedido = listaPedidosFiltrada.find { it.uid == pedidoId }
            if (pedido != null) {
                pendingPedido = pedido // Almacena el pedido pendiente
                checkAndRequestPermissions(pedido) // Llama a la función auxiliar para verificar/solicitar permisos
            }
        }

        recyclerView.adapter = adapter

        val btnBack: ImageView = findViewById(R.id.imageViewBack)
        btnBack.setOnClickListener { finish() }

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
        val menuAgregarProducto = findViewById<TextView>(R.id.menuAgregarProducto)
        val menuPedidos = findViewById<TextView>(R.id.menuPedidos)
        val menuUsuarios = findViewById<TextView>(R.id.menuUsuarios)
        val menuCortesdeCaja = findViewById<TextView>(R.id.menuCortesdeCaja)
        val menuInfoApp = findViewById<TextView>(R.id.menuInfoApp)

        imageViewLogoMenu.setOnClickListener { closeDrawer() }

        menuAgregarUsuario.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, AgregarUsuarioActivity::class.java))
            finish()
        }

        menuProductos.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, InventarioActivity::class.java))
            finish()
        }

        menuAgregarProducto.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, AgregarProductoActivity::class.java))
            finish()
        }

        menuPedidos.setOnClickListener { closeDrawer() }

        menuUsuarios.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
            finish()
        }

        menuCortesdeCaja.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, CortesDeCajaActivity::class.java))
            finish()
        }

        menuInfoApp.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, InfoAppActivity::class.java))
            finish()
        }

        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarPedidos(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarPedidos()
    }

    // MODIFICADA: Simplificada para enfocarse solo en READ_EXTERNAL_STORAGE para API 28 e inferiores
    private fun checkAndRequestPermissions(pedido: Pedido) {
        val permissionsToRequest = mutableListOf<String>()

        // Solo solicitamos WRITE_EXTERNAL_STORAGE si es Android 9 (API 28) o inferior
        // Esto es porque para Q (29) y superiores, se usa MediaStore que no necesita este permiso.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // P es API 28
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // READ_EXTERNAL_STORAGE para API 32 o inferior para leer archivos no multimedia.
        // En API 33+, para leer archivos que no son imágenes/video/audio, el acceso directo
        // al almacenamiento público puede no ser manejado por este permiso de forma visible.
        // Sin embargo, si tu propia app lo guarda con MediaStore, la app debería tener
        // acceso implícito para leerlo. Lo mantenemos por compatibilidad.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) { // TIRAMISU es API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }


        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            // Permisos ya concedidos o no necesarios para esta versión de Android,
            // procede a generar el PDF.
            generarPDFPedido(pedido)
            pendingPedido = null // Limpia el pedido pendiente
        }
    }

    // Sobreescribe este método para manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (allPermissionsGranted) {
                // Permisos concedidos, genera el PDF si hay un pedido pendiente
                pendingPedido?.let {
                    generarPDFPedido(it)
                }
            } else {
                // Permisos denegados. Informa al usuario.
                Toast.makeText(this, "Permisos de almacenamiento denegados. No se puede generar el PDF.", Toast.LENGTH_LONG).show()

                // Verifica si el usuario marcó "No volver a preguntar" o denegó permanentemente
                // Se considera denegado permanentemente si shouldShowRequestPermissionRationale devuelve false después de una denegación.
                var showRationale = false
                for (perm in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                        showRationale = true // Al menos un permiso fue denegado permanentemente
                        break
                    }
                }

                if (showRationale) {
                    // Si el usuario denegó permanentemente al menos uno de los permisos necesarios,
                    // lo enviamos a la configuración de la app.
                    AlertDialog.Builder(this)
                        .setTitle("Permisos necesarios")
                        .setMessage("Para generar PDFs y guardarlos, necesitamos permisos de almacenamiento. Por favor, actívalos manualmente en la configuración de la aplicación.")
                        .setPositiveButton("Ir a Configuración") { dialog, which ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
            pendingPedido = null // Siempre limpia pendingPedido después de manejar el resultado
        }
    }

    private fun cargarPedidos() {
        db.collection("pedidos")
            .orderBy("fechaHoraCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentos ->
                listaPedidos.clear()
                for (doc in documentos) {
                    val pedido = doc.toObject(Pedido::class.java).copy(uid = doc.id)
                    listaPedidos.add(pedido)
                }
                listaPedidosFiltrada.clear()
                listaPedidosFiltrada.addAll(listaPedidos)
                adapter.notifyDataSetChanged()
                verificarResultados()
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    private fun formatoFecha(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(it)
        } ?: "N/A"
    }

    private fun formatoMoneda(monto: Double): String {
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formatoMoneda.format(monto)
    }

    private fun filtrarPedidos(textoBusqueda: String) {
        val texto = textoBusqueda.lowercase(Locale.getDefault()).trim()
        listaPedidosFiltrada.clear()
        if (texto.isEmpty()) {
            listaPedidosFiltrada.addAll(listaPedidos)
        } else {
            for (pedido in listaPedidos) {
                val coincide = pedido.uid.lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.cliente.lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.domicilio.lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.entregadoPor.lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.estadoPedido.lowercase(Locale.getDefault()).contains(texto) ||
                        formatoFecha(pedido.fechaHoraCreacion).lowercase(Locale.getDefault()).contains(texto) ||
                        formatoFecha(pedido.fechaHoraEntrega).lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.idTarjetaNFC.lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.metodoPago.lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.registradoPor.lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.tipoPedido.lowercase(Locale.getDefault()).contains(texto) ||
                        formatoMoneda(pedido.impuestos).lowercase(Locale.getDefault()).contains(texto) ||
                        formatoMoneda(pedido.subTotal).lowercase(Locale.getDefault()).contains(texto) ||
                        formatoMoneda(pedido.totalFinal).lowercase(Locale.getDefault()).contains(texto) ||
                        pedido.puntosAntesCompra.toString().contains(texto) ||
                        pedido.puntosDescontados.toString().contains(texto) ||
                        pedido.puntosGanados.toString().contains(texto) ||
                        pedido.puntosTotales.toString().contains(texto)

                if (coincide) {
                    listaPedidosFiltrada.add(pedido)
                }
            }
        }
        adapter.notifyDataSetChanged()
        verificarResultados()
    }

    private fun verificarResultados() {
        if (listaPedidosFiltrada.isEmpty()) {
            textViewSinResultados.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textViewSinResultados.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun abrirPDF(file: File) {
        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No hay aplicación para abrir PDFs instalada en el dispositivo.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado al intentar abrir el PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun generarPDFPedido(pedido: Pedido) {
        db.collection("pedidos").document(pedido.uid).collection("productos").get()
            .addOnSuccessListener { snapshot ->
                val productos = snapshot.mapNotNull { doc ->
                    doc.toObject(ProductoPedido::class.java)?.copy(uid = doc.id)
                }

                if (productos.isEmpty()) {
                    Toast.makeText(this, "No hay productos en el pedido para generar el PDF.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val pdf = PdfDocument()
                var pageNumber = 1
                val pageConfig = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create() // A4 size
                var page = pdf.startPage(pageConfig)
                var canvas = page.canvas
                val paint = Paint() // Paint genérico para drawBitmap sin filtros específicos

                // --- Configuración de Fuentes y Estilos (Modernos y tipo Google) ---
                val fontName = "sans-serif" // Mapea a Roboto en Android, similar a Google Fonts
                val lightFont = Typeface.create(fontName, Typeface.NORMAL)
                val mediumFont = Typeface.create(fontName, Typeface.BOLD)

                val titlePaint = Paint().apply {
                    typeface = mediumFont
                    textSize = 28f // Más grande para el título principal del documento
                    color = Color.parseColor("#212121") // Casi negro
                    isAntiAlias = true
                }

                val subTitlePaint = Paint().apply {
                    typeface = mediumFont
                    textSize = 18f // Para subtítulos de sección
                    color = Color.parseColor("#424242")
                    isAntiAlias = true
                }

                val bodyTextPaint = Paint().apply {
                    typeface = lightFont
                    textSize = 12f
                    color = Color.parseColor("#616161")
                    isAntiAlias = true
                }

                val headerTextPaint = Paint().apply {
                    typeface = mediumFont
                    textSize = 12f
                    color = Color.WHITE
                    isAntiAlias = true
                }

                // Colores para el diseño moderno
                val primaryColor = Color.parseColor("#1A73E8") // Azul vibrante
                val lightGray = Color.parseColor("#F5F5F5") // Fondo de filas alternas o encabezados suaves
                val mediumGray = Color.parseColor("#E0E0E0") // Bordes sutiles

                val linePaint = Paint().apply {
                    strokeWidth = 0.5f // Bordes aún más finos
                    color = mediumGray
                    style = Paint.Style.STROKE
                }

                val headerFillPaint = Paint().apply {
                    style = Paint.Style.FILL
                    color = primaryColor
                }

                val rowFillPaint = Paint().apply {
                    style = Paint.Style.FILL
                    color = lightGray
                }

                // --- Logo Floralia: Solo dibujar en la primera página ---
                val logoBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo_floralia)
                val logoHeight = 70 // Ajustado, no tan pequeño
                val aspectRatio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
                val logoWidth = (logoHeight * aspectRatio).toInt()
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoWidth, logoHeight, true)

                // Posición: Arriba a la derecha, con un buen margen
                val logoRightMargin = 40f
                val logoTopMargin = 40f
                val logoX = pageConfig.pageWidth - logoWidth - logoRightMargin
                val logoY = logoTopMargin
                // Solo dibujar el logo aquí, una vez al inicio del documento
                canvas.drawBitmap(scaledLogo, logoX, logoY, null)

                var y = 140f // Posición inicial Y para el contenido principal (más abajo para el título)
                val leftMargin = 40f
                val rightMargin = pageConfig.pageWidth - 40f
                val contentWidth = rightMargin - leftMargin
                val bottomMargin = 40f // Margen inferior constante

                // --- Título principal del documento ---
                canvas.drawText("Detalles del Pedido", leftMargin, y - 50, titlePaint) // Título más grande arriba

                // --- Helper para nueva página ---
                fun nextPage() {
                    pdf.finishPage(page)
                    pageNumber++
                    page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                    canvas = page.canvas
                    y = 100f // Reinicia 'y' para la nueva página
                }

                // --- Helper para dibujar tablas clave-valor (Información del Pedido, Resumen de Cotización) ---
                fun dibujarTablaClaveValor(datos: List<Pair<String, String>>, startY: Float): Float {
                    var currentY = startY
                    val rowHeight = 25f
                    val col1Width = contentWidth * 0.4f
                    val col2Width = contentWidth * 0.6f

                    // Dibujar solo el borde exterior superior de la tabla
                    canvas.drawLine(leftMargin, currentY, rightMargin, currentY, linePaint)

                    datos.forEachIndexed { index, pair ->
                        val top = currentY + index * rowHeight
                        val bottom = top + rowHeight

                        // Fondo de fila alterna
                        if (index % 2 == 0) {
                            canvas.drawRect(leftMargin, top, rightMargin, bottom, rowFillPaint)
                        }

                        // Líneas divisorias internas (horizontales y vertical)
                        canvas.drawLine(leftMargin, bottom, rightMargin, bottom, linePaint) // Línea inferior de la fila
                        canvas.drawLine(leftMargin + col1Width, top, leftMargin + col1Width, bottom, linePaint) // Línea vertical

                        // Calcular la posición Y para centrar el texto verticalmente
                        val textY = top + rowHeight / 2 + (bodyTextPaint.textSize / 2.5f) // Ajuste fino para centrado

                        // Texto de clave y valor con padding
                        canvas.drawText(pair.first, leftMargin + 10f, textY, bodyTextPaint)
                        canvas.drawText(pair.second, leftMargin + col1Width + 10f, textY, bodyTextPaint)
                    }
                    // Dibujar bordes verticales exteriores de la tabla
                    canvas.drawLine(leftMargin, startY, leftMargin, currentY + datos.size * rowHeight, linePaint)
                    canvas.drawLine(rightMargin, startY, rightMargin, currentY + datos.size * rowHeight, linePaint)


                    return currentY + datos.size * rowHeight + 30f // Espacio después de la tabla
                }

                // === SECCIÓN 1: Información del Pedido ===
                canvas.drawText("Información del Pedido", leftMargin, y, subTitlePaint)
                y += 25f // Espaciado consistente
                y = dibujarTablaClaveValor(
                    listOf(
                        "UID Pedido:" to pedido.uid,
                        "Tarjeta NFC:" to pedido.idTarjetaNFC,
                        "Fecha de Creación:" to formatoFecha(pedido.fechaHoraCreacion),
                        "Fecha de Entrega:" to formatoFecha(pedido.fechaHoraEntrega),
                        "Cliente:" to pedido.cliente,
                        "Método de Pago:" to pedido.metodoPago,
                        "Tipo de Pedido:" to pedido.tipoPedido,
                        "Domicilio:" to pedido.domicilio,
                        "Estado del Pedido:" to pedido.estadoPedido,
                        "Registrado Por:" to pedido.registradoPor,
                        "Entregado Por:" to pedido.entregadoPor
                    ), y
                )

                // === SECCIÓN 2: Resumen de Cotización ===
                canvas.drawText("Resumen de Cotización", leftMargin, y, subTitlePaint)
                y += 25f // Espaciado consistente
                y = dibujarTablaClaveValor(
                    listOf(
                        "Subtotal:" to formatoMoneda(pedido.subTotal),
                        "Impuestos:" to formatoMoneda(pedido.impuestos),
                        "Puntos Antes:" to pedido.puntosAntesCompra.toString(),
                        "Puntos Usados:" to pedido.puntosDescontados.toString(),
                        "Puntos Ganados:" to pedido.puntosGanados.toString(),
                        "Puntos Totales:" to pedido.puntosTotales.toString(),
                        "Total Final:" to formatoMoneda(pedido.totalFinal)
                    ), y
                )

                // --- FORZAR SALTO DE PÁGINA AQUÍ para que los productos siempre empiecen en la segunda hoja ---
                nextPage()
                // y se reinicia a 100f en nextPage()

                // === SECCIÓN 3: Productos del Pedido ===
                canvas.drawText("Productos del Pedido", leftMargin, y, subTitlePaint)
                y += 45f // Ajustado a 25f para consistencia con los otros títulos

                val productRowHeight = 100f // Mantener esta altura para texto e imagen
                val colWidthsProducts = listOf(contentWidth * 0.30f, contentWidth * 0.15f, contentWidth * 0.20f, contentWidth * 0.20f, contentWidth * 0.15f)
                val headersProducts = listOf("Nombre", "Cantidad", "Precio Unitario", "Subtotal", "Imagen")

                // Dibujar cabecera de la tabla de productos
                var currentX = leftMargin
                val headerTop = y - 20f
                val headerBottom = y + 5f
                val headerTextY = headerTop + (headerBottom - headerTop) / 2 + (headerTextPaint.textSize / 2.5f) // Centrar texto verticalmente

                // Dibujar el borde superior general de la tabla de productos
                canvas.drawLine(leftMargin, headerTop, rightMargin, headerTop, linePaint)

                headersProducts.forEachIndexed { idx, header ->
                    canvas.drawRect(currentX, headerTop, currentX + colWidthsProducts[idx], headerBottom, headerFillPaint) // Fondo azul
                    // Solo borde vertical derecho para cada columna de cabecera (el general lo haremos al final)
                    canvas.drawLine(currentX + colWidthsProducts[idx], headerTop, currentX + colWidthsProducts[idx], headerBottom, linePaint)
                    canvas.drawText(header, currentX + 5f, headerTextY, headerTextPaint) // Texto blanco con padding
                    currentX += colWidthsProducts[idx]
                }
                // Dibujar la línea inferior de la cabecera
                canvas.drawLine(leftMargin, headerBottom, rightMargin, headerBottom, linePaint)
                y = headerBottom + 10f // Espacio después de la cabecera

                productos.forEachIndexed { index, prod ->
                    // Salto de página si no hay espacio para la fila completa (después de la primera página)
                    if (y + productRowHeight > pageConfig.pageHeight - bottomMargin) {
                        nextPage()
                        // Redibujar cabecera en la nueva página
                        currentX = leftMargin
                        val newHeaderTop = y - 20f
                        val newHeaderBottom = y + 5f
                        val newHeaderTextY = newHeaderTop + (newHeaderBottom - newHeaderTop) / 2 + (headerTextPaint.textSize / 2.5f)

                        // Dibujar el borde superior general de la tabla de productos en la nueva página
                        canvas.drawLine(leftMargin, newHeaderTop, rightMargin, newHeaderTop, linePaint)

                        headersProducts.forEachIndexed { idx, header ->
                            canvas.drawRect(currentX, newHeaderTop, currentX + colWidthsProducts[idx], newHeaderBottom, headerFillPaint)
                            canvas.drawLine(currentX + colWidthsProducts[idx], newHeaderTop, currentX + colWidthsProducts[idx], newHeaderBottom, linePaint)
                            canvas.drawText(header, currentX + 5f, newHeaderTextY, headerTextPaint)
                            currentX += colWidthsProducts[idx]
                        }
                        canvas.drawLine(leftMargin, newHeaderBottom, rightMargin, newHeaderBottom, linePaint) // Línea inferior de la cabecera
                        y = newHeaderBottom + 10f
                    }

                    val rowTop = y // El "y" actual es el top de la fila
                    val rowBottom = y + productRowHeight

                    // Fondo de fila alterna
                    if (index % 2 == 0) {
                        canvas.drawRect(leftMargin, rowTop, rightMargin, rowBottom, rowFillPaint)
                    }

                    // Dibujar línea horizontal inferior para la fila
                    canvas.drawLine(leftMargin, rowBottom, rightMargin, rowBottom, linePaint)

                    // Texto columnas y líneas verticales
                    currentX = leftMargin // Reset X para contenido de la fila
                    val textYCenter = rowTop + productRowHeight / 2 + (bodyTextPaint.textSize / 2.5f) // Centrar texto verticalmente en la fila

                    canvas.drawText(prod.nombre, currentX + 5f, textYCenter, bodyTextPaint)
                    currentX += colWidthsProducts[0]
                    canvas.drawLine(currentX, rowTop, currentX, rowBottom, linePaint) // Línea vertical
                    canvas.drawText(prod.cantidad.toString(), currentX + 5f, textYCenter, bodyTextPaint)
                    currentX += colWidthsProducts[1]
                    canvas.drawLine(currentX, rowTop, currentX, rowBottom, linePaint) // Línea vertical
                    canvas.drawText(formatoMoneda(prod.precioUnitario), currentX + 5f, textYCenter, bodyTextPaint)
                    currentX += colWidthsProducts[2]
                    canvas.drawLine(currentX, rowTop, currentX, rowBottom, linePaint) // Línea vertical
                    canvas.drawText(formatoMoneda(prod.subTotal), currentX + 5f, textYCenter, bodyTextPaint)
                    currentX += colWidthsProducts[3]
                    canvas.drawLine(currentX, rowTop, currentX, rowBottom, linePaint) // Línea vertical

                    // Imagen: centrada y escalada proporcionalmente dentro de su celda
                    if (prod.imagenBase64.isNotEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(prod.imagenBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                            val cellPadding = 10f // Espacio alrededor de la imagen dentro de la celda
                            val maxImageWidth = colWidthsProducts[4] - (cellPadding * 2)
                            val maxImageHeight = productRowHeight - (cellPadding * 2)

                            val imageAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val cellAspectRatio = maxImageWidth / maxImageHeight

                            val finalWidth: Int
                            val finalHeight: Int

                            if (imageAspectRatio > cellAspectRatio) { // Imagen es más ancha que la celda
                                finalWidth = maxImageWidth.toInt()
                                finalHeight = (maxImageWidth / imageAspectRatio).toInt()
                            } else { // Imagen es más alta o de la misma proporción que la celda
                                finalHeight = maxImageHeight.toInt()
                                finalWidth = (maxImageHeight * imageAspectRatio).toInt()
                            }

                            val imgScaled = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)

                            // Centrar imagen en la celda
                            val imgX = currentX + (colWidthsProducts[4] - finalWidth) / 2
                            val imgY = rowTop + (productRowHeight - finalHeight) / 2
                            canvas.drawBitmap(imgScaled, imgX, imgY, paint)

                        } catch (e: Exception) {
                            // Mejor dibujar el texto un poco más centrado en la celda si la imagen falla
                            canvas.drawText("[Imagen inválida]", currentX + 5f, textYCenter, bodyTextPaint)
                            e.printStackTrace() // Log the error for debugging
                        }
                    }
                    // Dibujar el borde derecho de la tabla (solo para la última columna)
                    canvas.drawLine(rightMargin, rowTop, rightMargin, rowBottom, linePaint)

                    y += productRowHeight // Incrementa Y para la siguiente fila de producto
                }

                // Dibujar los bordes verticales generales para toda la tabla de productos (desde el inicio de la cabecera hasta el final de la última fila)
                val productTableStart = y - (productos.size * productRowHeight) - (headerBottom - headerTop) - 10f // Calcula donde empezó la tabla
                canvas.drawLine(leftMargin, productTableStart, leftMargin, y, linePaint) // Borde izquierdo
                canvas.drawLine(rightMargin, productTableStart, rightMargin, y, linePaint) // Borde derecho

                pdf.finishPage(page)

                // --- Guardar y abrir PDF ---
                val fileName = "Pedido_${pedido.uid}.pdf"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    try {
                        uri?.let {
                            resolver.openOutputStream(it).use { outputStream ->
                                pdf.writeTo(outputStream!!)
                            }
                            abrirPDFDesdeUri(it)
                        } ?: throw IOException("No se pudo crear el archivo. URI es nula.")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error al guardar PDF (Android Q+): ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsPath, fileName)

                    try {
                        FileOutputStream(file).use { fos ->
                            pdf.writeTo(fos)
                        }
                        abrirPDF(file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error al guardar PDF (Android < Q): ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                pdf.close()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener productos del pedido: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace() // Log the error for debugging
            }
    }

    private fun abrirPDFDesdeUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No hay aplicación para abrir PDFs instalada.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado al intentar abrir el PDF (URI): ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}

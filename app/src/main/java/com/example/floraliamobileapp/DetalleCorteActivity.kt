package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DetalleCorteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var documentId: String // UID del documento de corte de caja

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_corte)

        findViewById<ImageView>(R.id.btnBackDetalle).setOnClickListener {
            finish()
        }

        documentId = intent.getStringExtra("documentId") ?: return
        db = FirebaseFirestore.getInstance()

        // Carga los detalles del corte
        cargarDatosDetalle(documentId)
    }

    private fun cargarDatosDetalle(id: String) {
        db.collection("cortes_de_caja")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                val corte = doc.toObject(DetalleCorteCaja::class.java)
                if (corte != null) {
                    // Asignamos manualmente el uid para que se muestre
                    val corteConUid = corte.copy(uid = doc.id)
                    mostrarEnPantalla(corteConUid)
                    cargarPedidos(id) // carga subcolección de pedidos usando el mismo UID
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                Toast.makeText(this, "Error al cargar el corte", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarEnPantalla(corte: DetalleCorteCaja) {
        val formato = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        findViewById<TextView>(R.id.txtUidCorte).text = "UID Corte #${corte.uid}"
        findViewById<TextView>(R.id.txtGeneradoPorDetalle).text = "Generado por: ${corte.generadoPor}"
        findViewById<TextView>(R.id.txtFechaInicial).text = "Inicio: ${formato.format(corte.fechaInicial?.toDate())}"
        findViewById<TextView>(R.id.txtFechaFinal).text = "Final: ${formato.format(corte.fechaFinal?.toDate())}"
        findViewById<TextView>(R.id.txtFechaCreacion).text = "Creado el: ${formato.format(corte.fechaHoraCreacion?.toDate())}"

        findViewById<TextView>(R.id.txtTotalPedidosDetalle).text = "Total pedidos: ${corte.totalPedidos}"
        findViewById<TextView>(R.id.txtTotalVentasDetalle).text = "Total ventas: $${corte.totalVentas} MXN"
        findViewById<TextView>(R.id.txtEfectivo).text = "Efectivo: $${corte.efectivo} MXN"
        findViewById<TextView>(R.id.txtTransferencia).text = "Transferencia: $${corte.transferencia} MXN"
        findViewById<TextView>(R.id.txtTarjeta).text = "Tarjeta: $${corte.tarjeta} MXN"
        findViewById<TextView>(R.id.txtPuntos).text = "Puntos: ${corte.puntos}"
        findViewById<TextView>(R.id.txtGanados).text = "Puntos ganados: ${corte.puntosGanadosTotal}"
        findViewById<TextView>(R.id.txtUsados).text = "Puntos usados: ${corte.puntosUsadosTotal}"
    }

    private fun cargarPedidos(documentId: String) {
        val tabla = findViewById<TableLayout>(R.id.tablePedidos)
        tabla.removeAllViews()

        // Crear encabezado
        val header = TableRow(this).apply {
            setPadding(4, 4, 4, 4)
            setBackgroundColor(Color.parseColor("#00796B"))
        }

        val headers = listOf("UID", "Cliente", "Estado", "Fecha", "Método","Total")
        headers.forEach { texto ->
            val tv = TextView(this).apply {
                text = texto
                setPadding(16, 12, 16, 12)
                textSize = 15f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                minWidth = 220
            }
            header.addView(tv)
        }
        tabla.addView(header)

        // Obtener pedidos de la subcolección usando el UID del corte
        db.collection("cortes_de_caja")
            .document(documentId)
            .collection("pedidos")
            .get()
            .addOnSuccessListener { result ->
                val formato = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

                if (result.isEmpty) {
                    Toast.makeText(this, "Este corte no contiene pedidos", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for ((index, doc) in result.withIndex()) {
                    val pedido = doc.toObject(PedidoCorte::class.java)

                    val row = TableRow(this).apply {
                        setPadding(4, 4, 4, 4)
                        setBackgroundColor(Color.parseColor(if (index % 2 == 0) "#E0F2F1" else "#FFFFFF"))
                    }

                    val fechaTexto = pedido.fechaHoraCreacion?.toDate()?.let { formato.format(it) } ?: "N/D"

                    val celdas = listOf(
                        doc.id, // Usamos el UID del documento como identificador del pedido
                        pedido.cliente,
                        pedido.estadoPedido,
                        fechaTexto,
                        pedido.metodoPago,
                        "$${String.format("%.2f", pedido.totalFinal)}"
                    )

                    celdas.forEach { texto ->
                        val tv = TextView(this).apply {
                            text = texto
                            setPadding(16, 12, 16, 12)
                            textSize = 14f
                            setTextColor(Color.BLACK)
                            gravity = Gravity.CENTER
                            minWidth = 220
                        }
                        row.addView(tv)
                    }

                    tabla.addView(row)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                Toast.makeText(this, "Error al cargar los pedidos", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.floraliamobileapp.adapter.ProductoPedidoAdapter
import com.example.floraliamobileapp.model.PedidoDetalle
import com.example.floraliamobileapp.model.ProductoPedido
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DetallesPedidoActivity : AppCompatActivity() {

    private lateinit var tvUidPedido: TextView
    private lateinit var tvCliente: TextView
    private lateinit var tvDomicilio: TextView
    private lateinit var tvEntregadoPor: TextView
    private lateinit var tvEstadoPedido: TextView
    private lateinit var tvFechaCreacion: TextView
    private lateinit var tvFechaEntrega: TextView
    private lateinit var tvIdTarjetaNFC: TextView
    private lateinit var tvImpuestos: TextView
    private lateinit var tvMetodoPago: TextView
    private lateinit var tvPuntosAntesCompra: TextView
    private lateinit var tvPuntosDescontados: TextView
    private lateinit var tvPuntosGanados: TextView
    private lateinit var tvPuntosTotales: TextView
    private lateinit var tvRegistradoPor: TextView
    private lateinit var tvSubTotal: TextView
    private lateinit var tvTipoPedido: TextView
    private lateinit var tvTotalFinal: TextView
    private lateinit var imageViewBack: ImageView

    private lateinit var recyclerProductos: RecyclerView
    private val listaProductos = mutableListOf<ProductoPedido>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_pedido)

        tvUidPedido = findViewById(R.id.tvUidPedido)
        tvCliente = findViewById(R.id.tvCliente)
        tvDomicilio = findViewById(R.id.tvDomicilio)
        tvEntregadoPor = findViewById(R.id.tvEntregadoPor)
        tvEstadoPedido = findViewById(R.id.tvEstadoPedido)
        tvFechaCreacion = findViewById(R.id.tvFechaCreacion)
        tvFechaEntrega = findViewById(R.id.tvFechaEntrega)
        tvIdTarjetaNFC = findViewById(R.id.tvIdTarjetaNFC)
        tvImpuestos = findViewById(R.id.tvImpuestos)
        tvMetodoPago = findViewById(R.id.tvMetodoPago)
        tvPuntosAntesCompra = findViewById(R.id.tvPuntosAntesCompra)
        tvPuntosDescontados = findViewById(R.id.tvPuntosDescontados)
        tvPuntosGanados = findViewById(R.id.tvPuntosGanados)
        tvPuntosTotales = findViewById(R.id.tvPuntosTotales)
        tvRegistradoPor = findViewById(R.id.tvRegistradoPor)
        tvSubTotal = findViewById(R.id.tvSubTotal)
        tvTipoPedido = findViewById(R.id.tvTipoPedido)
        tvTotalFinal = findViewById(R.id.tvTotalFinal)
        imageViewBack = findViewById(R.id.imageViewBack)

        recyclerProductos = findViewById(R.id.recyclerProductos)
        recyclerProductos.layoutManager = LinearLayoutManager(this)

        imageViewBack.setOnClickListener {
            finish()
        }

        val idPedido = intent.getStringExtra("ID_PEDIDO")
        if (idPedido != null) {
            cargarDetallesPedido(idPedido)
        } else {
            Toast.makeText(this, "No se recibió el ID del pedido", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cargarDetallesPedido(idPedido: String) {
        val docRef = db.collection("pedidos").document(idPedido)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val pedido = document.toObject(PedidoDetalle::class.java)
                    if (pedido != null) {
                        val pedidoConUid = pedido.copy(uid = idPedido)
                        mostrarDatos(pedidoConUid)
                        cargarProductosDePedido(idPedido)
                    } else {
                        Toast.makeText(this, "Error al cargar datos del pedido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun cargarProductosDePedido(idPedido: String) {
        db.collection("pedidos").document(idPedido).collection("productos")
            .get()
            .addOnSuccessListener { documentos ->
                listaProductos.clear()
                for (doc in documentos) {
                    val producto = doc.toObject(ProductoPedido::class.java)
                    listaProductos.add(producto)
                }
                recyclerProductos.adapter = ProductoPedidoAdapter(listaProductos)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarDatos(pedido: PedidoDetalle) {
        tvUidPedido.text = "UID Pedido: ${pedido.uid}"
        tvCliente.text = "Cliente: ${pedido.cliente}"
        tvDomicilio.text = "Domicilio: ${pedido.domicilio}"
        tvEntregadoPor.text = "Entregado por: ${pedido.entregadoPor}"
        tvEstadoPedido.text = "Estado: ${pedido.estadoPedido}"
        tvFechaCreacion.text = "Fecha Creación: ${formatTimestamp(pedido.fechaHoraCreacion)}"
        tvFechaEntrega.text = "Fecha Entrega: ${formatTimestamp(pedido.fechaHoraEntrega)}"
        tvIdTarjetaNFC.text = "ID Tarjeta NFC: ${pedido.idTarjetaNFC}"
        tvImpuestos.text = "Impuestos: ${formatCurrency(pedido.impuestos)}"
        tvMetodoPago.text = "Método de Pago: ${pedido.metodoPago}"
        tvPuntosAntesCompra.text = "Puntos antes de compra: ${pedido.puntosAntesCompra}"
        tvPuntosDescontados.text = "Puntos descontados: ${pedido.puntosDescontados}"
        tvPuntosGanados.text = "Puntos ganados: ${pedido.puntosGanados}"
        tvPuntosTotales.text = "Puntos totales: ${pedido.puntosTotales}"
        tvRegistradoPor.text = "Registrado por: ${pedido.registradoPor}"
        tvSubTotal.text = "Subtotal: ${formatCurrency(pedido.subTotal)}"
        tvTipoPedido.text = "Tipo de pedido: ${pedido.tipoPedido}"
        tvTotalFinal.text = "Total final: ${formatCurrency(pedido.totalFinal)}"
    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        return if (timestamp != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale("es", "MX"))
            sdf.format(timestamp.toDate())
        } else {
            "N/A"
        }
    }

    private fun formatCurrency(monto: Double): String {
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return "${formatoMoneda.format(monto)} MXN"
    }
}

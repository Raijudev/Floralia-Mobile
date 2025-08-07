package com.example.floraliamobileapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.floraliamobileapp.R
import com.example.floraliamobileapp.model.Pedido
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PedidoAdapter(
    private val pedidos: List<Pedido>,
    private val onDownloadClick: ((pedidoId: String) -> Unit)? = null
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.bind(pedido)

        // Código para descargar PDF
        holder.itemView.findViewById<ImageView>(R.id.imageViewDescargarPdf).setOnClickListener {
            onDownloadClick?.invoke(pedido.uid)
        }
    }

    override fun getItemCount(): Int = pedidos.size

    class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val uidPedido: TextView = itemView.findViewById(R.id.textUidPedido)
        private val fecha: TextView = itemView.findViewById(R.id.textFecha)
        private val cliente: TextView = itemView.findViewById(R.id.textCliente)
        private val total: TextView = itemView.findViewById(R.id.textTotal)
        private val metodo: TextView = itemView.findViewById(R.id.textMetodo)
        private val estado: TextView = itemView.findViewById(R.id.textEstado)

        fun bind(pedido: Pedido) {
            uidPedido.text = "Núm. de Pedido: ${pedido.uid}"

            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            pedido.fechaHoraCreacion?.let {
                fecha.text = sdf.format(it.toDate())
            }

            cliente.text = "Cliente: ${pedido.cliente}"

            val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            total.text = "${formatoMoneda.format(pedido.totalFinal)} MXN"

            metodo.text = "Método de Pago: ${pedido.metodoPago}"
            estado.text = "Estado del Pedido: ${pedido.estadoPedido}"
        }
    }
}

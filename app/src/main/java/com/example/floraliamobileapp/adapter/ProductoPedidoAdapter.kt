package com.example.floraliamobileapp.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.floraliamobileapp.R
import com.example.floraliamobileapp.model.ProductoPedido
import java.text.NumberFormat
import java.util.*

class ProductoPedidoAdapter(private val productos: List<ProductoPedido>) :
    RecyclerView.Adapter<ProductoPedidoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_pedido, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreProducto)
        private val tvCantidad = itemView.findViewById<TextView>(R.id.tvCantidad)
        private val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecio)
        private val tvSubTotal = itemView.findViewById<TextView>(R.id.tvSubTotal)
        private val imgProducto = itemView.findViewById<ImageView>(R.id.imgProducto)

        @SuppressLint("SetTextI18n")
        fun bind(producto: ProductoPedido) {
            tvNombre.text = producto.nombre
            tvCantidad.text = "Cantidad: ${producto.cantidad}"
            tvPrecio.text = "Precio: ${formatCurrency(producto.precioUnitario)}"
            tvSubTotal.text = "Subtotal: ${formatCurrency(producto.subTotal)}"

            // Decodificar imagen base64
            try {
                val imageBytes = Base64.decode(producto.imagenBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imgProducto.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgProducto.setImageResource(R.drawable.logo_floralia)
            }
        }

        private fun formatCurrency(monto: Double): String {
            val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            return "${formato.format(monto)} MXN"
        }
    }
}

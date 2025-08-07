package com.example.floraliamobileapp

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ProductoAdapter(
    private val productos: List<Producto>,
    private val userRole: String?,
    private val onEditarClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position], userRole, onEditarClick)
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagen: ImageView = itemView.findViewById(R.id.imageViewProducto)
        private val nombre: TextView = itemView.findViewById(R.id.textViewNombre)
        private val cantidad: TextView = itemView.findViewById(R.id.textViewCantidad)
        private val precio: TextView = itemView.findViewById(R.id.textViewPrecio)

        fun bind(producto: Producto, userRole: String?, onEditarClick: (Producto) -> Unit) {

            nombre.text = producto.nombre
            cantidad.text = "Cantidad: ${producto.cantidad}"

            // Formatear el precio como MXN
            val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            precio.text = formato.format(producto.precioUnitario)
            precio.text = "${precio.text} MXN"

            // Decodificar la imagen desde Base64
            try {
                val decodedBytes = Base64.decode(producto.imagenBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imagen.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imagen.setImageResource(R.drawable.logo_floralia) // Fallback si falla
            }

            // 👉 Click para editar - CONDICIONAL SEGÚN EL ROL
            if (userRole == "Administrador") {
                itemView.setOnClickListener {
                    onEditarClick(producto)
                }
                itemView.isClickable = true
                itemView.isFocusable = true
            } else {
                itemView.setOnClickListener(null)
                itemView.isClickable = false
                itemView.isFocusable = false
            }
        }
    }
}
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
    private val userRole: String?, // <- NUEVO: Se pasa el rol del usuario aquí
    private val onEditarClick: (Producto) -> Unit // Este listener ahora solo se ejecutará si se permite
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        // Pasar el rol y el listener a la función bind del ViewHolder
        holder.bind(productos[position], userRole, onEditarClick) // <- MODIFICADO
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagen: ImageView = itemView.findViewById(R.id.imageViewProducto)
        private val nombre: TextView = itemView.findViewById(R.id.textViewNombre)
        private val cantidad: TextView = itemView.findViewById(R.id.textViewCantidad)
        private val precio: TextView = itemView.findViewById(R.id.textViewPrecio)

        fun bind(producto: Producto, userRole: String?, onEditarClick: (Producto) -> Unit) { // <- MODIFICADO
            nombre.text = producto.nombre
            cantidad.text = "Cantidad: ${producto.cantidad}"

            // Formatear el precio como MXN
            val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            precio.text = formato.format(producto.precioUnitario)

            // Decodificar la imagen desde Base64
            try {
                val decodedBytes = Base64.decode(producto.imagenBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imagen.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imagen.setImageResource(R.drawable.logo_floralia) // Fallback si falla
            }

            // 👉 Click para editar - CONDICIONAL SEGÚN EL ROL
            if (userRole == "Administrador") { // <- NUEVO: Solo si es administrador
                itemView.setOnClickListener {
                    onEditarClick(producto)
                }
                itemView.isClickable = true // Asegurarse de que sea clicable
                itemView.isFocusable = true
            } else {
                itemView.setOnClickListener(null) // <- NUEVO: Eliminar el listener si no es admin
                itemView.isClickable = false // <- NUEVO: Hacer que no sea clicable
                itemView.isFocusable = false
            }
        }
    }
}

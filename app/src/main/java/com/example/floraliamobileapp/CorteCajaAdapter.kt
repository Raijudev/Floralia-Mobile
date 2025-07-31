package com.example.floraliamobileapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CorteCajaAdapter(
    private val listaCortes: MutableList<CorteCaja>,
    private val onDownloadPdfClick: (CorteCaja) -> Unit // Lambda para el clic en el botón de descarga
) : RecyclerView.Adapter<CorteCajaAdapter.CorteViewHolder>() {

    class CorteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtuid: TextView = itemView.findViewById(R.id.txtUidCorte)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFechas)
        val txtGenerado: TextView = itemView.findViewById(R.id.txtGeneradoPor)
        val txtPedidos: TextView = itemView.findViewById(R.id.txtTotalPedidos)
        val txtVentas: TextView = itemView.findViewById(R.id.txtTotalVentas)
        val imgDownloadPdf: ImageView = itemView.findViewById(R.id.imgDownloadPdf) // Nuevo ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CorteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_corte_caja, parent, false)
        return CorteViewHolder(vista)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CorteViewHolder, position: Int) {
        val corte = listaCortes[position]
        val formato = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        holder.txtuid.text = "UID: ${corte.uid}"
        holder.txtFecha.text = "De ${formato.format(corte.fechaInicial?.toDate())} a ${formato.format(corte.fechaFinal?.toDate())}"
        holder.txtGenerado.text = "Generado por: ${corte.generadoPor}"
        holder.txtPedidos.text = "Pedidos: ${corte.totalPedidos}"
        holder.txtVentas.text = "Ventas: $${String.format("%.2f", corte.totalVentas)} MXN"

        // Nuevo listener para el icono de descarga
        holder.imgDownloadPdf.setOnClickListener {
            onDownloadPdfClick(corte) // Llama al lambda cuando se hace clic en el icono
        }
    }

    override fun getItemCount() = listaCortes.size

    // Función para actualizar lista con los resultados filtrados
    fun actualizarLista(nuevaLista: MutableList<CorteCaja>) {
        listaCortes.clear()
        listaCortes.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}

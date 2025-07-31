package com.example.floraliamobileapp.model

import com.google.firebase.Timestamp

// Modelo de datos de un Pedido
data class Pedido(
    val uid: String = "",
    val cliente: String = "",
    val domicilio: String = "",
    val entregadoPor: String = "",
    val estadoPedido: String = "",
    val fechaHoraCreacion: Timestamp? = null,
    val fechaHoraEntrega: Timestamp? = null,
    val idTarjetaNFC: String = "",
    val impuestos: Double = 0.0,
    val metodoPago: String = "",
    val puntosAntesCompra: Int = 0,
    val puntosDescontados: Int = 0,
    val puntosGanados: Int = 0,
    val puntosTotales: Int = 0,
    val registradoPor: String = "",
    val subTotal: Double = 0.0,
    val tipoPedido: String = "",
    val totalFinal: Double = 0.0
)

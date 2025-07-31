package com.example.floraliamobileapp

import com.google.firebase.Timestamp

data class PedidoCorte(
    val uid: String = "",
    val cliente: String = "",
    val estadoPedido: String = "",
    val fechaHoraCreacion: Timestamp? = null,
    val metodoPago: String = "",
    val totalFinal: Double = 0.0
)

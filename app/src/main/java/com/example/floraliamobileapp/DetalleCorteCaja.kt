package com.example.floraliamobileapp

import com.google.firebase.Timestamp

data class DetalleCorteCaja(
    val uid: String = "",
    val fechaInicial: Timestamp? = null,
    val fechaFinal: Timestamp? = null,
    val fechaHoraCreacion: Timestamp? = null,
    val generadoPor: String = "",
    val totalPedidos: Int = 0,
    val totalVentas: Double = 0.0,
    val efectivo: Double = 0.0,
    val transferencia: Double = 0.0,
    val tarjeta: Double = 0.0,
    val puntos: Double = 0.0,
    val puntosGanadosTotal: Double = 0.0,
    val puntosUsadosTotal: Double = 0.0
)

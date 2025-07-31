package com.example.floraliamobileapp

import com.google.firebase.Timestamp

data class TarjetaNFC(
    val idTarjetaNFC: String = "",
    val estado: String = "",
    val puntos: Int = 0,
    val fechaActivacion: Timestamp? = null,
    val fechaVencimiento: Timestamp? = null
)

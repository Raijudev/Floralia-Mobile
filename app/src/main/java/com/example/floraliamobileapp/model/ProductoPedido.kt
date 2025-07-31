package com.example.floraliamobileapp.model

data class ProductoPedido(
    val uid: String = "",
    val nombre: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subTotal: Double = 0.0,
    val imagenBase64: String = ""
)

package com.example.floraliamobileapp

import com.google.firebase.Timestamp

data class Usuario(
    var uid: String = "", // ID de autenticación de Firebase
    var nombre: String = "",
    var apellido: String = "",
    var telefono: String = "",
    var domicilio: String = "",
    var correo: String = "",
    var curp: String? = null, // Opcional

    var rol: String = "", // "Administrador", "Empleado", "Cliente"

    // Tarjeta NFC asociada
    var idTarjetaNFC: String? = null,
    var puntos: Int? = null, // Puntos acumulados por el usuario
    var fechaActivacion: Timestamp? = null, // Fecha en que se activó la tarjeta
    var fechaVencimiento: Timestamp? = null // Fecha de expiración de la tarjeta
)

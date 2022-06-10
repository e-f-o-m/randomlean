package com.efom.randomlearn.MODELS

class Tarjeta {
    var id_tarjeta = -1
    var id_lista = -1
    var id_horario = -1
    var pregunta: String? = null
    var respuesta: String? = null
    var color: String? = null
    var dificultad = 5.0
    var detalles: String? = null
    var tipo = -1
    var fecha_inicio: String? = null
    var fecha_fin: String? = null
    var estado = -1

    constructor(
        id_tarjeta: Int,
        id_lista: Int,
        id_horario: Int,
        pregunta: String?,
        respuesta: String?,
        color: String?,
        dificultad: Double,
        detalles: String?,
        tipo: Int,
        fecha_inicio: String?,
        fecha_fin: String?,
        estado: Int
    ) {
        this.id_lista = id_lista
        this.id_tarjeta = id_tarjeta
        this.id_horario = id_horario
        this.pregunta = pregunta
        this.respuesta = respuesta
        this.color = color
        this.dificultad = dificultad
        this.detalles = detalles
        this.tipo = tipo
        this.fecha_inicio = fecha_inicio
        this.fecha_fin = fecha_fin
        this.estado = estado
    }

    constructor() {}
}
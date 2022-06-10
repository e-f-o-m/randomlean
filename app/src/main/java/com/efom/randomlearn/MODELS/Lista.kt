package com.efom.randomlearn.MODELS
class Lista {
    var id_lista: Int? = null;
    var nombre: String? = null;
    var ruta: String? = null;
    var color: String? = null;
    var ico: String? = null;
    var detalles: String? = null;
    var rango_preguntas: Int? = null;
    var tiempo_estudio: String? = null;
    var estado: Int? = null;

    constructor(
        id_lista: Int?,
        nombre: String?,
        ruta: String?,
        color: String?,
        ico: String?,
        detalles: String?,
        rango_preguntas: Int?,
        tiempo_estudio: String?,
        estado: Int?
    ) {
        this.id_lista = id_lista
        this.nombre = nombre
        this.ruta = ruta
        this.color = color
        this.ico = ico
        this.detalles = detalles
        this.rango_preguntas = rango_preguntas
        this.tiempo_estudio = tiempo_estudio
        this.estado = estado
    }
}
package com.efom.randomlearn.SQLITE

import com.efom.randomlearn.SQLITE.Constants_SQLite

open class Constants_SQLite {
    companion object {
        const val DB_NAME = "randomlearn_db"
        const val TABLA_LISTAS = "tabla_lista"
        const val TABLA_TARJETAS = "tabla_tarjetas"
        const val TABLA_HORARIO = "tabla_horario"
        const val ID_LISTA = "id_lista"
        const val NOMBRE = "nombre_lista"
        const val ICO = "ico"
        const val DETALLES = "detalles"
        const val RANGO_PREGUNTAS = "rango_preguntas"
        const val TIEMPO_ESTUDIO = "tiempo_estudio"
        const val ESTADO = "estado"
        const val ID_TARJETA = "id_tarjeta"
        const val PREGUNTA = "pregunta"
        const val RESPUESTA = "respuesta"
        const val DIFICULTAD = "dificultad"
        const val COLOR = "color"
        const val ID_HORARIO = "id_horario"

        //public final static String DETALLES = "";
        const val TIPO = "tipo"
        const val FECHA_INICIO = "fecha_inicio"
        const val FECHA_FIN = "fecha_fin"

        //public final static String ESTADO = "";
        const val FAVORITOS = "favoritos"
        const val ID_LEARM = "id_learm"
        const val N_DAY = "n_days"
        const val N_REPEAT = "n_repeat"
        const val ESTATE = "estate"

        //ID_HORARIO+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
        const val FECHA_HORA = "fecha_hora"
        const val TIPO_NOTIFICACION = "tipo_notificacion"
        const val RUTA = "ruta"
        const val CREATE_TABLE_LISTAS = ("CREATE TABLE " + TABLA_LISTAS + " ("
                + ID_LISTA + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOMBRE + " TEXT, "
                + RUTA + " TEXT, "
                + COLOR + " TEXT, "
                + ICO + " TEXT, "
                + DETALLES + " TEXT, "
                + RANGO_PREGUNTAS + " INTEGER, "
                + TIEMPO_ESTUDIO + " TEXT, "
                + ESTADO + " INTEGER)")
        val AR_LISTA = arrayOf(
            ID_LISTA,
            NOMBRE,
            RUTA,
            COLOR,
            ICO,
            DETALLES,
            RANGO_PREGUNTAS,
            TIEMPO_ESTUDIO,
            ESTADO
        )
        const val CREATE_TABLE_TARJETAS = ("CREATE TABLE " + TABLA_TARJETAS + " ("
                + ID_TARJETA + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ID_LISTA + " INTEGER, "
                + ID_HORARIO + " INTEGER, "
                + PREGUNTA + " TEXT, "
                + RESPUESTA + " TEXT, "
                + COLOR + " TEXT, "
                + DIFICULTAD + " DOUBLE, "
                + DETALLES + " TEXT, "
                + TIPO + " INTEGER, "
                + FECHA_INICIO + " TEXT, "
                + FECHA_FIN + " TEXT, "
                + FAVORITOS + " INTEGER, "
                + ESTADO + " INTEGER)")
        val AR_TARJETAS = arrayOf(
            ID_TARJETA,
            ID_LISTA,
            ID_HORARIO,
            PREGUNTA,
            RESPUESTA,
            COLOR,
            DIFICULTAD,
            DETALLES,
            TIPO,
            FECHA_INICIO,
            FECHA_FIN,
            ESTADO
        )
        const val CREATE_TABLE_HORARIO = ("CREATE TABLE " + TABLA_HORARIO + " ("
                + ID_HORARIO + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FECHA_HORA + " TEXT, "
                + TIPO_NOTIFICACION + " INTEGER )")
        val AR_HORARIO = arrayOf(
            ID_HORARIO,
            FECHA_HORA,
            TIPO_NOTIFICACION
        )
    }
}
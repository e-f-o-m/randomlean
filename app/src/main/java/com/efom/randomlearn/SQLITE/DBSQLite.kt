package com.efom.randomlearn.SQLITE

import com.efom.randomlearn.SQLITE.OpenHelper.Companion.newInstance
import android.database.sqlite.SQLiteDatabase
import com.efom.randomlearn.MODELS.Lista
import android.content.ContentValues
import android.content.Context
import com.efom.randomlearn.MODELS.Tarjeta
import android.database.DatabaseUtils
import com.efom.randomlearn.Utiles.CONST
import java.util.ArrayList

class DBSQLite(context: Context?) : Constants_SQLite() {
    private var openHelper: OpenHelper = newInstance(context)
    var db: SQLiteDatabase? = null
    private val CO: CONST = CONST

    fun openDB(abrirLectura: Boolean) {
        db = if (abrirLectura) {
            openHelper.readableDatabase
        } else {
            openHelper.writableDatabase
        }
    }

    /*
    * create: one
    * read: one, list, personalize
    * update: one
    * delete: one
    * counts: one, personalize
    * */

    /**- - - - - - - LISTAs - - - - - - - - */

    fun addLista(lista: Lista): Int {
        openDB(false)
        val contenedor = ContentValues()
        contenedor.put(NOMBRE, lista.nombre)
        contenedor.put(RUTA, lista.ruta)
        contenedor.put(COLOR, lista.color)
        contenedor.put(ICO, lista.ico)
        contenedor.put(DETALLES, lista.detalles)
        contenedor.put(RANGO_PREGUNTAS, lista.rango_preguntas)
        contenedor.put(TIEMPO_ESTUDIO, lista.tiempo_estudio)
        contenedor.put(ESTADO, lista.estado)
        val id = db!!.insert(TABLA_LISTAS, null, contenedor).toInt()
        db!!.close()
        return id
    }

    fun getListasCards(): ArrayList<Lista> {
        openDB(true)
        val list = ArrayList<Lista>()
        val sql =
            "SELECT * FROM $TABLA_LISTAS"
        val cursor = db!!.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val lista = Lista(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getString(7),
                    cursor.getInt(8)
                )
                list.add(lista)
            } while (cursor.moveToNext())
        }
        db!!.close()
        return list
    }

    fun getLista(id_lista: Int): Lista? {
        openDB(true)
        var lista: Lista? = null
        val sql = "SELECT * FROM " + TABLA_LISTAS + " WHERE " + ID_LISTA + " = " + id_lista
        val cursor = db!!.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            lista = Lista(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6),
                cursor.getString(7),
                cursor.getInt(8)
            )
        }
        db!!.close()
        return lista
    }

    fun getListaName(id_lista: Int): String {
        openDB(true)
        var nombre = ""
        val sql =
            "SELECT $NOMBRE FROM $TABLA_LISTAS WHERE $ID_LISTA = $id_lista"
        val cursor = db!!.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                nombre = cursor.getString(0)
            } while (cursor.moveToNext())
        }
        db!!.close()
        return nombre
    }

    //web
    fun putNombreListaID(nombreLista: String?, id_lista: Int): Int {
        openDB(false)
        val contenedor = ContentValues()
        contenedor.put(ID_LISTA, id_lista)
        contenedor.put(NOMBRE, nombreLista)
        val id = db!!.insert(TABLA_LISTAS, null, contenedor).toInt()
        db!!.close()
        return id
    }

    fun deleteLista(id_lista: Int) {
        openDB(false)
        var queryDeleteList: String? = null
        var queryDeleteTarjeta: String? = null
        queryDeleteList = "DELETE FROM " + TABLA_LISTAS + " WHERE " + ID_LISTA + " = " + id_lista
        queryDeleteTarjeta =
            "DELETE FROM " + TABLA_TARJETAS + " WHERE " + ID_LISTA + " = " + id_lista
        db!!.execSQL(queryDeleteTarjeta)
        db!!.execSQL(queryDeleteList)
        db!!.close()
    }

    fun updateNameLista(lista: Lista): Int {
        openDB(false)
        val contenedor = ContentValues()
        contenedor.put(NOMBRE, lista.nombre)
        val where = ID_LISTA + " = " + lista.id_lista
        val id = db!!.update(TABLA_LISTAS, contenedor, where, null)
        db!!.close()
        return id
    }


    /**- - - - - - - TARJETAS - - - - - - - - */

    fun addTarjeta(_tarjeta: Tarjeta): Int {
        openDB(false)
        val contenedor = ContentValues()
        contenedor.put(ID_LISTA, _tarjeta.id_lista)
        contenedor.put(PREGUNTA, _tarjeta.pregunta)
        contenedor.put(RESPUESTA, _tarjeta.respuesta)
        contenedor.put(DIFICULTAD, _tarjeta.dificultad)
        contenedor.put(COLOR, _tarjeta.color)
        contenedor.put(DETALLES, _tarjeta.detalles)
        contenedor.put(TIPO, _tarjeta.tipo)
        contenedor.put(FECHA_INICIO, _tarjeta.fecha_inicio)
        contenedor.put(FECHA_FIN, _tarjeta.fecha_fin)
        contenedor.put(ESTADO, _tarjeta.estado)
        val i = db!!.insert(TABLA_TARJETAS, null, contenedor).toInt()
        db!!.close()
        return i
    }

    fun addTarjetas(tarjetas: ArrayList<Tarjeta>) {
        openDB(false)
        for (i in tarjetas.indices) {
            val contenedor = ContentValues()
            //id tarjeta -1
            contenedor.put(ID_LISTA, tarjetas[i].id_lista)
            contenedor.put(PREGUNTA, tarjetas[i].pregunta)
            contenedor.put(RESPUESTA, tarjetas[i].respuesta)
            contenedor.put(DIFICULTAD, tarjetas[i].dificultad)
            contenedor.put(COLOR, tarjetas[i].color)
            contenedor.put(DETALLES, tarjetas[i].detalles)
            contenedor.put(TIPO, tarjetas[i].tipo)
            contenedor.put(FECHA_INICIO, tarjetas[i].fecha_inicio)
            contenedor.put(FECHA_FIN, tarjetas[i].fecha_fin)
            contenedor.put(ESTADO, tarjetas[i].estado)
            db!!.insert(TABLA_TARJETAS, null, contenedor)
        }
        db!!.close()
    }

    fun getTarjetas(id_lista: Int): ArrayList<Tarjeta> {
        openDB(true)
        val list = ArrayList<Tarjeta>()
        val sql = "SELECT * FROM $TABLA_TARJETAS WHERE $ID_LISTA = $id_lista"
        val cursor = db!!.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val _tarjeta = Tarjeta(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getDouble(6),
                    cursor.getString(7),
                    cursor.getInt(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11)
                )
                list.add(_tarjeta)
            } while (cursor.moveToNext())
        }
        db!!.close()
        return list
    }

    fun getTarjetasRandom(id_lista: Int, order: Int): ArrayList<Tarjeta> {
        openDB(true)
        val list = ArrayList<Tarjeta>()
        var sql = "SELECT * FROM $TABLA_TARJETAS WHERE $ID_LISTA = $id_lista"
        when (order) {
            0 -> sql = "$sql ORDER BY RANDOM();"
            1 -> sql = "$sql ORDER BY $PREGUNTA ASC;"
            2 -> sql = "$sql ORDER BY $PREGUNTA DESC;"
            3 -> sql = "$sql ORDER BY $FECHA_INICIO ASC;"
            4 -> sql = "$sql ORDER BY $FECHA_INICIO DESC;"
            7 -> sql = "$sql AND $TIPO = " + CO.FAVORITO
            8 -> sql = "$sql AND $TIPO = " + CO.TEXTO
        }
        val cursor = db!!.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val _tarjeta = Tarjeta(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getDouble(6),
                    cursor.getString(7),
                    cursor.getInt(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11)
                )
                list.add(_tarjeta)
            } while (cursor.moveToNext())
        }
        db!!.close()
        return list
    }

    fun getTarjeta(id_lista: Int, id_tarjeta: Int): Tarjeta? {
        openDB(true)
        var _tarjeta: Tarjeta? = null
        val sql =
            "SELECT * FROM $TABLA_TARJETAS WHERE $ID_TARJETA = $id_tarjeta AND $ID_LISTA = $id_lista"
        val cursor = db!!.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                _tarjeta = Tarjeta(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getDouble(6),
                    cursor.getString(7),
                    cursor.getInt(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11)
                )
            } while (cursor.moveToNext())
        }
        db!!.close()
        return _tarjeta
    }

    fun updateTarjeta(_tarjeta: Tarjeta): Int {
        openDB(false)
        val contenedor = ContentValues()
        contenedor.put(PREGUNTA, _tarjeta.pregunta)
        contenedor.put(RESPUESTA, _tarjeta.respuesta)
        contenedor.put(DIFICULTAD, _tarjeta.dificultad)
        contenedor.put(COLOR, _tarjeta.color)
        contenedor.put(DETALLES, _tarjeta.detalles)
        contenedor.put(TIPO, _tarjeta.tipo)
        val where = ID_TARJETA + " = " + _tarjeta.id_tarjeta
        val id = db!!.update(TABLA_TARJETAS, contenedor, where, null)
        db!!.close()
        return id
    }

    fun updateResetStars(starts: Double, idLista: Int): Int {
        openDB(false)
        val contend = ContentValues()
        contend.put(DIFICULTAD, starts)
        val where = "$ID_LISTA = $idLista"
        val id = db!!.update(TABLA_TARJETAS, contend, where, null)
        db!!.close()
        return id
    }

    fun deleteTarjeta(id_tarjeta: Int) {
        openDB(false)
        var queryDeleteTarjeta: String? = null
        queryDeleteTarjeta =
            "DELETE FROM " + TABLA_TARJETAS + " WHERE " + ID_TARJETA + " = " + id_tarjeta
        db!!.execSQL(queryDeleteTarjeta)
        db!!.close()
    }

    //firebase
    fun updateTarjetaFB(_tarjeta: Tarjeta): Int {
        openDB(false)
        val contenedor = ContentValues()
        contenedor.put(PREGUNTA, _tarjeta.pregunta)
        contenedor.put(RESPUESTA, _tarjeta.respuesta)
        contenedor.put(DIFICULTAD, _tarjeta.dificultad)
        contenedor.put(COLOR, _tarjeta.color)
        contenedor.put(DETALLES, _tarjeta.detalles)
        val where = ID_TARJETA + " = " + _tarjeta.id_tarjeta
        var id = db!!.update(TABLA_TARJETAS, contenedor, where, null)
        if (id == 0) {
            contenedor.put(ID_TARJETA, _tarjeta.id_tarjeta)
            contenedor.put(ID_LISTA, _tarjeta.id_lista)
            id = db!!.insert(TABLA_TARJETAS, null, contenedor).toInt()
        }
        db!!.close()
        return id
    }

    // fixme: query ineficiente
    fun nextIdTarjeta(id_lista: Int, id_tarjeta: Int, ultimo: Int): Int {
        var id_tarjeta = id_tarjeta
        openDB(true)
        var pos = -1
        var salirBo = false
        while (!salirBo && id_tarjeta < ultimo) {
            val sql =
                "SELECT * FROM $TABLA_TARJETAS WHERE $ID_TARJETA = $id_tarjeta AND $ID_LISTA = $id_lista"
            val cursor = db!!.rawQuery(sql, null)
            if (cursor.moveToFirst()) {
                do {
                    pos = cursor.getInt(0)
                    salirBo = true
                } while (cursor.moveToNext())
            }
            id_tarjeta++
        }
        db!!.close()
        return pos
    }

    fun contadorResultados(id_lista: Int): IntArray {
        openDB(true)
        val sql =
            "SELECT COUNT(*) FROM $TABLA_TARJETAS WHERE $ID_LISTA = $id_lista AND $DIFICULTAD = 0.0"
        val sql2 =
            "SELECT COUNT(*) FROM $TABLA_TARJETAS WHERE $ID_LISTA = $id_lista"
        val i = intArrayOf(
            DatabaseUtils.longForQuery(db, sql, null).toInt(),
            DatabaseUtils.longForQuery(db, sql2, null).toInt()
        )
        db!!.close()
        return i
    }

}
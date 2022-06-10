package com.efom.randomlearn.Utiles


import android.util.Log
import com.efom.randomlearn.MODELS.Tarjeta
import java.io.BufferedReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DataController {
    private val CO:CONST = CONST
    private lateinit var reader: BufferedReader
    private lateinit var separador: String
    private var idLista: Int = -1
    private val listTarjetas = ArrayList<Tarjeta>()
    private var arIndex = IntArray(4){-1}
    private val regex = "([a-zA-Z0-9_-])\\w+".toRegex()
    private lateinit var arRottule: List<String>
    private lateinit var rotulo: String

    fun getTransformDataMassive(reader: BufferedReader, separador: String, idLista: Int) : ArrayList<Tarjeta> {
        this.reader = reader
        this.separador = separador
        this.idLista = idLista

        this.rotulo = this.reader.readLine().toString().lowercase(Locale.ROOT)
        this.arRottule = this.rotulo.split(separador)

        if (this.arRottule.size <= 1) {
            transformRowsMode()
        } else {
            transformColsMode()
        }
        return this.listTarjetas
    }

    private fun transformColsMode() {
        for (i in 0 until arRottule.size) {
            val rotulo = regex.find(arRottule[i].trim())!!.value
            when (rotulo){
                "preguta" -> arIndex[0] = i
                "pregunta" -> arIndex[0] = i
                "respuesta"-> arIndex[1] = i
                "respueta"-> arIndex[1] = i
                "dificultad"-> arIndex[2] = i
                "nivel"-> arIndex[2] = i
                "estrellas"-> arIndex[2] = i
                "observacion"-> arIndex[3] = i
                "observación"-> arIndex[3] = i
                "detalles"-> arIndex[3] = i
            }
        }

        reader.forEachLine {
            try {
                if (it.isNotEmpty()) {
                    val tokens = it.split(separador).toTypedArray()
                    listTarjetas.add(Tarjeta(
                        -1,
                        idLista,
                        -1,
                        if (arIndex[0] != -1) tokens[arIndex[0]] else "",
                        if (arIndex[1] != -1) tokens[arIndex[1]] else "",
                        generateRandomColor(),
                        if (arIndex[2] != -1) tokens[arIndex[2]].toDouble() else 5.0,
                        if (arIndex[3] != -1) tokens[arIndex[3]] else "",
                        CO.TEXTO,
                        "",
                        "",
                        CO.ACTIVO
                    )
                    )
                }
            } catch (e: Exception) {
                Log.e("data controller error", "getListDataMassive: " + e.message.toString())
            }
        }
    }

    private fun transformRowsMode() {
        var isPreviousEmply = false //respuesta
        var pregunta = rotulo //render ya extrajo rotulos
        var respuesta =""
        reader.forEachLine {
            try {
                Log.d("88 DataController.kt", "transformRowsMode: "+(it.isNotEmpty() && !isPreviousEmply))
                if(it.isNotEmpty() && !isPreviousEmply) { //respuesta -> anterior si existe
                    respuesta += it + "\\n"
                    Log.d("90 DataController.kt", "inicio")
                } else if (it.isNotEmpty() && isPreviousEmply) { // siguiente pregunta -> anterior vacío
                    pregunta = it
                    isPreviousEmply = false
                }else if (respuesta.isNotEmpty() or pregunta.isNotEmpty()){
                    isPreviousEmply = true
                    listTarjetas.add(Tarjeta(-1, idLista, -1, pregunta, respuesta,
                        generateRandomColor(), 5.0, "", CO.TEXTO, "", "", CO.ACTIVO
                    ))
                    respuesta = ""
                    pregunta = ""
                }
            } catch (e: Exception) {
                Log.e("data controller error", "getListDataMassive: " + e.message.toString())
            }
        }
        if(!isPreviousEmply and respuesta.isNotEmpty()){
            listTarjetas.add(Tarjeta(-1, idLista, -1, pregunta, respuesta,
                generateRandomColor(), 5.0, "", CO.TEXTO, "", "", CO.ACTIVO
            ))
        }
    }

    private fun generateRandomColor(): String {
        val letters = arrayOf('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F')
        var color = "#"
        for (i in 0..5) {
            color += letters[(Math.random() * 15).roundToInt()]
        }
        return color
    }


}
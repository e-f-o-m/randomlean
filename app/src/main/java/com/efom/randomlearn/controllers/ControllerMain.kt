package com.efom.randomlearn.controllers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.efom.randomlearn.R
import com.efom.randomlearn.database.MyDB
import com.efom.randomlearn.models.Metadata
import com.efom.randomlearn.utils.CONSTS
import java.io.*
import java.nio.charset.Charset
import java.util.*

class ControllerMain : IControllerMain {

    override fun setFileToSqlite(data: Intent, context: Context): Metadata {
        val db = MyDB.getDB(context)!!
        val metadata = Metadata()
        val prefs = context.getSharedPreferences("mPreferences", AppCompatActivity.MODE_PRIVATE)
        val separator = prefs.getString("separador", ";")!!.toString()
        val dataController = DataController()
        val pathHome = context.getExternalFilesDir(null)!!.path.split("/Android/")[0]
        val pathPersonalRandom = context.getString(R.string.carpeta)

        metadata.state = CONSTS.ENABLED

        try {
            val arPathFull: MutableList<String>
            val arPathXiaomi = data.data!!.path.toString().split("primary:").toMutableList()
            val pathOriginFull: String = if (arPathXiaomi.size > 1) {
                pathHome + "/" + arPathXiaomi[1]
            } else {
                data.data!!.path.toString()
            }
            arPathFull = pathOriginFull.split("/").toMutableList()

            metadata.name = arPathFull.removeLast()

            val pathOrigin = arPathFull.joinToString("/")

            val nameFolder = arPathFull[arPathFull.size - 1]

            val ext = metadata.name!!.split(".").toMutableList().last()

            val pathDestiny = "$pathHome/$pathPersonalRandom/$nameFolder"
            metadata.path = pathDestiny

            if (ext.isNotEmpty()) {
                val idNewLista = db.metadataDAO().insert(metadata).toInt()
                metadata.idMetadata = idNewLista

                val file = File(pathOriginFull)
                val inputStream: InputStream = FileInputStream(file)
                val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                val tempCards = dataController
                    .getTransformDataMassive(reader, separator, idNewLista)
                inputStream.close()

                db.cardsDAO().insertAll(tempCards)

                createDirectory(metadata.path!!)

                copyFile(pathOriginFull, "$pathDestiny/${metadata.name}")
                for (tempListTarjeta in tempCards)  {
                    try {
                        var isIMG = when {
                            tempListTarjeta.answer!!.lowercase(Locale.ROOT)
                                .endsWith(".png") -> true
                            tempListTarjeta.answer!!.lowercase(Locale.ROOT)
                                .endsWith(".jpeg") -> true
                            tempListTarjeta.answer!!.lowercase(Locale.ROOT)
                                .endsWith(".jpg") -> true
                            tempListTarjeta.answer!!.lowercase(Locale.ROOT)
                                .endsWith(".gif") -> true
                            tempListTarjeta.answer!!.lowercase(Locale.ROOT)
                                .endsWith(".svg") -> true
                            else -> false
                        }
                        if (isIMG) {
                            copyFile(
                                "$pathOrigin/${tempListTarjeta.answer}",
                                "$pathDestiny/${tempListTarjeta.answer}"
                            )
                        }

                        isIMG = when {
                            tempListTarjeta.question!!.lowercase(Locale.ROOT)
                                .endsWith(".png") -> true
                            tempListTarjeta.question!!.lowercase(Locale.ROOT)
                                .endsWith(".jpeg") -> true
                            tempListTarjeta.question!!.lowercase(Locale.ROOT)
                                .endsWith(".jpg") -> true
                            tempListTarjeta.question!!.lowercase(Locale.ROOT)
                                .endsWith(".gif") -> true
                            tempListTarjeta.question!!.lowercase(Locale.ROOT)
                                .endsWith(".svg") -> true
                            else -> false
                        }
                        if (isIMG) {
                            copyFile(
                                "$pathOrigin/${tempListTarjeta.question}",
                                "$pathDestiny/${tempListTarjeta.question}"
                            )
                        }
                    } catch (ex: Exception) {
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("MainActivity", "L353-onActivityResult = = = " + ex.message)
        }

        return metadata
    }

    private fun createDirectory(nameDirectory: String?): File {
        val directory = File(nameDirectory!!)
        if (!directory.exists()){
            if(!directory.mkdirs()) {
                Log.i(" 📌 234", "MainActivity🥚createDirectory🍄creado: ")
            }else{
                Log.e("236 MainActivity.kt", "createDirectory: error, ruta: $directory")
            }
        } else {
            Log.d("238 MainActivity.kt", "createDirectory: exitoso, ruta: $directory")
        }
        return directory
    }

    private fun copyFile(filePath: String, destiny: String) {
        File(filePath).copyTo(File(destiny))
    }


    override fun setNewMetadata(nameMetadata: String, context: Context): Metadata {
        val db = MyDB.getDB(context)!!
        val metadata = Metadata()
        val pathHome = context.getExternalFilesDir(null)!!.path.split("/Android/")[0]
        val pathPersonalRandom = context.getString(R.string.carpeta)

        // Crear carpeta, en caso de que exista la renombra
        val ruta = "$pathHome/$pathPersonalRandom/$nameMetadata"
        val file =
            File("$pathHome/$pathPersonalRandom/$nameMetadata")

        //Validar, sino no guardar en la base de datos
        metadata.name = nameMetadata
        metadata.path = ruta
        metadata.state = CONSTS.ENABLED

        if (!file.exists()) {
            file.mkdirs()
        }
        val idMetadata = db.metadataDAO().insert(metadata)
        metadata.idMetadata = idMetadata.toInt()
        return metadata
    }

}

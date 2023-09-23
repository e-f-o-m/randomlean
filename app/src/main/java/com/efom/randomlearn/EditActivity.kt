package com.efom.randomlearn

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.utils.CONSTS
import com.efom.randomlearn.controllers.DataController
import com.efom.randomlearn.utils.DT
import com.efom.randomlearn.database.MyDB
import com.efom.randomlearn.databinding.ActivityEditBinding
import com.efom.randomlearn.models.Metadata
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    lateinit var metadata: Metadata
    lateinit var db: MyDB
    private var idCard = 0
    private var id_lista = 0
    private var ultimo = -1
    private var card: Card? = null
    var color = ""
    //donde se guarda la foto
    var rutaFotoPregunta: String? = null
    var rutaFotoRespuesta: String? = null
    var currentPhotoName: String? = null
    private var booFotoPregunta = true
    var prefs: SharedPreferences? = null
    var separador = ";"
    lateinit var path: String
    lateinit var nameFile: String
    lateinit var b: ActivityEditBinding
    var type = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditBinding.inflate(layoutInflater)
        setContentView(b.root)
        db = MyDB.getDB(applicationContext)!!
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        separador = prefs!!.getString("separador", ";").toString()

        actions()
        card = Card()
        if (intent.extras != null) {
            idCard = intent.getIntExtra("id_tarjeta", -1)
            id_lista = intent.getIntExtra("id_lista", -1)
            ultimo = intent.getIntExtra("ultimo", -1)
            type = intent.getIntExtra(getString(R.string.type), -1)
            Log.i(" 📌 68", "EditActivity🥚onCreate🍄type: " + type)
            if(idCard != -1){
                setDataViews()
            }
            metadata = db.metadataDAO().getById(id_lista)
        }
        if (ultimo == -1 && type != CONSTS.EDIT_CARD) {
            b.cbNuevoEd.isChecked = true
            card!!.difficulty = 5.0
            card!!.details  =  "-"
            b.raBrDificultadEd.rating = card!!.difficulty!!.toFloat()
        }
        checkExternalStoragePermission()
        getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let { deleteImgOld(it) }
        path = metadata.path.toString()
        nameFile = metadata.name.toString()
    }

    private fun setDataViews() {
        card = db.cardsDAO().getById(idCard)
        b.etPreguntaEd.setText(card?.question, TextView.BufferType.EDITABLE)
        b.etRespuestaEd.setText(card?.answer, TextView.BufferType.EDITABLE)
        b.raBrDificultadEd.rating = (card?.difficulty?.toFloat() ?: 5.0).toFloat()
        b.etTypeAE.isChecked = card!!.type == CONSTS.SPACE_REPEAT_DB
    }

    private fun clearDataViews() {
        b.etPreguntaEd.setText("", TextView.BufferType.EDITABLE)
        b.etRespuestaEd.setText("", TextView.BufferType.EDITABLE)
        b.raBrDificultadEd.rating = 5.0f
        b.etTypeAE.isChecked = card!!.type == CONSTS.TEXTO_DB
    }

    private fun sendData() {
        buildCard()
        if (b.cbNuevoEd.isChecked) {
            card!!.idCard = null
             if (b.cbFormaMasivaEd.isChecked ) {val dataController = DataController()
                val inputString: Reader = StringReader(card!!.question)
                 val reader = BufferedReader(inputString)
                val listaTarjetas = dataController.getTransformDataMassive(reader, separador, id_lista)
                db.cardsDAO().insertAll (listaTarjetas)
            }else{
                 db.cardsDAO().insert(card!!)
            }
        } else {
            db.cardsDAO().update(card!!)
        }
            //insert Firestore
        /*
        val tarjeta = hashMapOf(
                "id_tarjeta"  to _tarjeta!!.id_tarjeta,
                "pregunta"  to _tarjeta!!.pregunta,
                "respuesta"  to _tarjeta!!.respuesta,
                "dificultad"  to _tarjeta!!.dificultad,
                "observacion"  to _tarjeta!!.detalles
        )
        db.collection(db_sqLite!!.selectNombreLista(id_lista))
                .document(pojo_tarjeta!!.id_tarjeta.toString())
                .set(tarjeta)*/
    }

    private fun buildCard() {
            card!!.idCard     = idCard
            card!!.idMetadata = id_lista
            card!!.question   = b.etPreguntaEd.text.toString().trim()
            card!!.answer     = b.etRespuestaEd.text.toString().trim()
            card!!.difficulty = b.raBrDificultadEd.rating.toDouble()
            card!!.favorite = false
            card!!.details    = ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun actions() {
            b.btnEliminarEd.setOnClickListener {
                /*try {
                    //FIXME File(path + "/" + card!!.answer).delete()
                    //File(path + "/" + metadata.name + "/" + card!!.question).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }*/
                db.cardsDAO().delete(card!!)

                val cardT = db.cardsDAO().getNext(card!!.idCard!!, card!!.idMetadata!!)
                if(cardT != null){
                    Log.i(" 📌 157", "EditActivity🥚actions🍄senddata")
                    idCard = cardT.idCard!!
                    setDataViews()
                }else{
                    Log.i(" 📌 157", "EditActivity🥚actions🍄bacpress: ")
                    onBackPressed()
                }
            }
            b.btnAplicarEd.setOnClickListener {
                if (rutaFotoPregunta != null) {
                    moverFoto(rutaFotoPregunta!!)
                }
                if (rutaFotoRespuesta != null) {
                    moverFoto(rutaFotoRespuesta!!)
                }
                sendData()
                onBackPressed()
            }
            b.btnSiguienteEd.setOnClickListener {
                if (rutaFotoPregunta != null) {
                    moverFoto(rutaFotoPregunta!!)
                }
                if (rutaFotoRespuesta != null) {
                    moverFoto(rutaFotoRespuesta!!)
                }
                sendData()

                if(card!!.idCard != null){
                    val cardT = db.cardsDAO().getNext(card!!.idCard!!, card!!.idMetadata!!)
                    if(cardT != null){
                        idCard = cardT.idCard!!
                    }

                    if(idCard != -1){
                        setDataViews()
                    }else{
                        onBackPressed()
                    }
                }else{
                    clearDataViews()
                }
            }
            b.btnCancelarEd.setOnClickListener { onBackPressed() }
            b.imgBtnCameraPreguntaEd.setOnClickListener {
                booFotoPregunta = true
                dispatchTakePictureIntent()
            }
            b.imgBtnCameraRespuestaEd.setOnClickListener {
                booFotoPregunta = false
                dispatchTakePictureIntent()
            }
            b.imgVCapturaEA.setOnClickListener {
            }
            b.imgBtnInfoEA.setOnClickListener {
                b.fLInfoMasivoAE.visibility = View.VISIBLE
            }
            b.fLInfoMasivoAE.setOnClickListener {
                b.fLInfoMasivoAE.visibility = View.GONE
            }
            b.cbFormaMasivaEd.setOnClickListener {
                if (b.cbFormaMasivaEd.isChecked){
                    b.cbNuevoEd.isChecked = true
                    b.imgBtnCameraPreguntaEd.isEnabled = false
                    b.imgBtnCameraRespuestaEd.isEnabled = false
                    b.imgVCapturaEA.isEnabled = false
                    b.etRespuestaEd.isEnabled = false
                    b.raBrDificultadEd.isEnabled = false
                    b.etTypeAE.isChecked = false
                    b.etTypeAE.isEnabled = false
                } else {
                    b.imgBtnCameraPreguntaEd.isEnabled = true
                    b.imgBtnCameraRespuestaEd.isEnabled = true
                    b.imgVCapturaEA.isEnabled = true
                    b.etRespuestaEd.isEnabled = true
                    b.raBrDificultadEd.isEnabled = true
                    b.etTypeAE.isEnabled = true
                }
            }
            b.etTypeAE.setOnClickListener {
                if(b.etTypeAE.isChecked){
                    card!!.type = CONSTS.SPACE_REPEAT_DB
                    card!!.startDate = DT.startDay()
                    card!!.endDate = DT.endDay()
                }else{
                    card!!.type = CONSTS.TEXTO_DB
                    card!!.startDate = 0
                    card!!.endDate = 0
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //FL_containerCapture_EA.setVisibility(View.VISIBLE);
            //File f = new File(currentPhotoPath);
            //Uri contentUri = Uri.fromFile(f);
            if (booFotoPregunta) {
                //b.imgBtnCameraPregunta_Ed.setImageURI(contentUri);
                b.etPreguntaEd.setText(currentPhotoName)
            } else {
                //b.imgBtnCameraRespuesta_Ed.setImageURI(contentUri);
                b.etRespuestaEd.setText(currentPhotoName)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        if (booFotoPregunta) {
            rutaFotoPregunta = image.absolutePath
            currentPhotoName = rutaFotoPregunta!!.split("/")[rutaFotoPregunta!!.split("/").size - 1]
        } else {
            rutaFotoRespuesta = image.absolutePath
            currentPhotoName = rutaFotoRespuesta!!.split("/")[rutaFotoRespuesta!!.split("/").size - 1]
        }
        return image
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this, "com.efom.randomlearn", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun checkExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 225)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 226)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun moverFoto(rutaFoto: String) {
        val origenPath = FileSystems.getDefault().getPath(rutaFoto)
        val lista = db.metadataDAO().getById(id_lista)
        val nombre = rutaFoto.split("/")[rutaFoto.split("/").size - 1]
        val destinoPath = FileSystems.getDefault().getPath(lista.path + "/" + nombre)
        try {
            Files.move(origenPath, destinoPath, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            System.err.println(e)
        }
    }

    private fun deleteImgOld(file: File) {
        if (file.isDirectory) {
            val children = file.list()
            if (children != null) {
                for (i in children.indices) {
                    File(file, children[i]).delete()
                }
            }
        }
    }

    override fun onBackPressed() {
        if(ultimo != -1){
            super.onBackPressed()
        }else{
            if(type == CONSTS.EDIT_CARD){
                super.onBackPressed()
                finish()
            }else{
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("id_lista", id_lista)
                startActivity(intent)
            }
        }
        finish()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}
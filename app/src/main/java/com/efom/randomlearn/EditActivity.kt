package com.efom.randomlearn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.efom.randomlearn.MODELS.Tarjeta
import com.efom.randomlearn.SQLITE.DBSQLite
import com.efom.randomlearn.Utiles.CONST
import com.efom.randomlearn.Utiles.DataController
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences
import android.util.Log

class EditActivity : AppCompatActivity(), View.OnClickListener{
    //Vistas
    private var raBrDificultadEd: RatingBar? = null
    private var etPreguntaEd: EditText? = null
    private var etRespuestaEd: EditText? = null
    private var etObservacionEd: EditText? = null
    private var imgBtnCameraPregunta_Ed: ImageButton? = null
    private var imgBtnCameraRespuesta_Ed: ImageButton? = null
    private var btnEliminarEd: Button? = null
    private var btnColorEd: Button? = null
    private var btnAplicarEd: Button? = null
    private var btnCancelarEd: Button? = null
    private var btnSiguienteEd: Button? = null
    private var fLInfoMasivo_AE: FrameLayout? = null
    private var imgBtnInfo_EA: ImageView? = null
    private var cbNuevoEd: CheckBox? = null
    private var cbFormaMasivaEd: CheckBox? = null
    private var DBSQLite: DBSQLite? = null
    private var context: Context? = null
    private var id_tarjeta = 0
    private var id_lista = 0
    private var ultimo = -1
    private var tarjeta: Tarjeta? = null
    var imgV_captura_EA: ImageView? = null
    var FL_containerCapture_EA: FrameLayout? = null
    var color = ""
    //donde se guarda la foto
    var rutaFotoPregunta: String? = null
    var rutaFotoRespuesta: String? = null
    var currentPhotoName: String? = null
    private var booFotoPregunta = true
    var prefs: SharedPreferences? = null
    var separador = ";"
    private lateinit var pathHome: String
    private lateinit var directorioRandomSD: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        separador = prefs!!.getString("separador", ";").toString()
        pathHome = prefs!!.getString("path_home", ";").toString()
        directorioRandomSD = getString(R.string.carpeta)

        asignarVistas()
        directorioRandomSD = getString(R.string.carpeta)
        tarjeta = Tarjeta()
        context = applicationContext
        DBSQLite = DBSQLite(context)
        if (intent.extras != null) {
            id_tarjeta = intent.getIntExtra("id_tarjeta", -1)
            id_lista = intent.getIntExtra("id_lista", -1)
            ultimo = intent.getIntExtra("ultimo", -1)
            if(id_tarjeta != -1){
                setDataViews()
            }
        }
        if (ultimo == -1) {
            cbNuevoEd!!.isChecked = true
            tarjeta!!.color = "#00BD72"
            tarjeta!!.dificultad = 5.0
            tarjeta!!.detalles = "-"
            raBrDificultadEd!!.rating = tarjeta!!.dificultad.toFloat()
            etObservacionEd!!.setText("-")
        }
        checkExternalStoragePermission()
        getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let { deleteImgOld(it) }
    }

    //PETICIÓN Y RELLENAR CAMPOS
    private fun setDataViews() {
        tarjeta = DBSQLite!!.getTarjeta(id_lista, id_tarjeta)
        etPreguntaEd!!.setText(tarjeta?.pregunta, TextView.BufferType.EDITABLE)
        etRespuestaEd!!.setText(tarjeta?.respuesta, TextView.BufferType.EDITABLE)
        etObservacionEd!!.setText(tarjeta?.detalles, TextView.BufferType.EDITABLE)
        cambiarColorBoton(tarjeta?.color)
        raBrDificultadEd!!.rating = (tarjeta?.dificultad?.toFloat() ?: 5.0) as Float
    }

    //TODO Enviar sqlite - firebase (aceptar o aplicar)
    private fun enviarDatos() {
        tarjeta = asignarPojoTarjeta()
        if (cbNuevoEd!!.isChecked) {
            if (cbFormaMasivaEd!!.isChecked){
                val dataController = DataController()
                val inputString: Reader = StringReader(tarjeta!!.pregunta)
                Log.d("119 EditActivity.kt", "enviarDatos: "+tarjeta!!.pregunta)
                val reader = BufferedReader(inputString)
                val listaTarjetas = dataController.getTransformDataMassive(reader, separador, id_lista)
                DBSQLite!!.addTarjetas(listaTarjetas)
            }else{
                DBSQLite!!.addTarjeta(tarjeta!!)
            }
        } else {
            DBSQLite!!.updateTarjeta(tarjeta!!)
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

    // DIALOGO SELECTOR DE COLOR HEXADECIMAL
    private fun openDialogColor(supportsAlpha: Boolean) {
        val dialog = AmbilWarnaDialog(this, ContextCompat.getColor(applicationContext, R.color.colorAccent), supportsAlpha, object : OnAmbilWarnaListener {
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                this@EditActivity.color = "#" + Integer.toHexString(color).substring(2)
                cambiarColorBoton(this@EditActivity.color)
            }

            override fun onCancel(dialog: AmbilWarnaDialog) {}
        })
        dialog.show()
    }

        //fixme: pojo tarjeta set db
    private fun asignarPojoTarjeta(): Tarjeta {
        return Tarjeta(
            id_tarjeta,
            id_lista,
            -1,
            etPreguntaEd!!.text.toString().trim { it <= ' ' },
            etRespuestaEd!!.text.toString().trim { it <= ' ' },
            "#ffffff",
            raBrDificultadEd!!.rating.toDouble(),
            etObservacionEd!!.text.toString().trim { it <= ' ' },
            CONST.TEXTO,
            "",
            "",
            CONST.ACTIVO
        )
    }

    private fun cambiarColorBoton(colorHex: String?) {
        if (colorHex != null) {
            btnColorEd!!.setBackgroundColor(Color.parseColor(colorHex))
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnEliminarEd -> {
                try {
                    File(Environment.getExternalStorageDirectory().toString() + "/" + directorioRandomSD + "/" + DBSQLite!!.getListaName(id_lista) + "/" +
                            tarjeta!!.pregunta).delete()
                    File(Environment.getExternalStorageDirectory().toString() + "/" + directorioRandomSD + "/" + DBSQLite!!.getListaName(id_lista) + "/" +
                            tarjeta!!.respuesta).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                DBSQLite!!.deleteTarjeta(tarjeta!!.id_tarjeta)
                id_tarjeta = DBSQLite!!.nextIdTarjeta(tarjeta!!.id_lista, tarjeta!!.id_tarjeta + 1, ultimo)
                setDataViews()
                Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                if(id_tarjeta != -1){
                    setDataViews()
                }else{
                    onBackPressed()
                }
            }
            R.id.btnAplicarEd -> {
                if (rutaFotoPregunta != null) {
                    moverFoto(rutaFotoPregunta!!)
                }
                if (rutaFotoRespuesta != null) {
                    moverFoto(rutaFotoRespuesta!!)
                }
                enviarDatos()
                onBackPressed()
            }
            R.id.btnSiguienteEd -> {
                if (rutaFotoPregunta != null) {
                    moverFoto(rutaFotoPregunta!!)
                }
                if (rutaFotoRespuesta != null) {
                    moverFoto(rutaFotoRespuesta!!)
                }
                enviarDatos()
                id_tarjeta = DBSQLite!!.nextIdTarjeta(tarjeta!!.id_lista, tarjeta!!.id_tarjeta + 1, ultimo)
                if(id_tarjeta != -1){
                    setDataViews()
                }else{
                    onBackPressed()
                }
            }
            R.id.btnCancelarEd -> onBackPressed()
            R.id.btnColorEd -> openDialogColor(false)
            R.id.imgBtnCameraPregunta_Ed -> {
                booFotoPregunta = true
                dispatchTakePictureIntent()
            }
            R.id.imgBtnCameraRespuesta_Ed -> {
                booFotoPregunta = false
                dispatchTakePictureIntent()
            }
            R.id.imgV_captura_EA -> {
            }
            R.id.imgBtnInfo_EA -> {
                fLInfoMasivo_AE!!.visibility = View.VISIBLE
            }
            R.id.fLInfoMasivo_AE -> {
                fLInfoMasivo_AE!!.visibility = View.GONE
            }
            R.id.cbFormaMasivaEd -> {
                if (cbFormaMasivaEd!!.isChecked){
                    cbNuevoEd!!.isChecked = true
                    btnColorEd!!.visibility = View.GONE
                    imgBtnCameraPregunta_Ed!!.visibility = View.GONE
                    imgBtnCameraRespuesta_Ed!!.visibility = View.GONE
                    imgV_captura_EA!!.visibility = View.GONE
                    etRespuestaEd!!.visibility = View.GONE
                    raBrDificultadEd!!.visibility = View.GONE
                    etObservacionEd!!.visibility = View.GONE
                } else {
                    btnColorEd!!.visibility = View.VISIBLE
                    imgBtnCameraPregunta_Ed!!.visibility = View.VISIBLE
                    imgBtnCameraRespuesta_Ed!!.visibility = View.VISIBLE
                    imgV_captura_EA!!.visibility = View.VISIBLE
                    etRespuestaEd!!.visibility = View.VISIBLE
                    raBrDificultadEd!!.visibility = View.VISIBLE
                    etObservacionEd!!.visibility = View.VISIBLE
                }
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
                //imgBtnCameraPregunta_Ed.setImageURI(contentUri);
                etPreguntaEd!!.setText(currentPhotoName)
            } else {
                //imgBtnCameraRespuesta_Ed.setImageURI(contentUri);
                etRespuestaEd!!.setText(currentPhotoName)
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
        val lista = DBSQLite!!.getLista(id_lista)
        val nombre = rutaFoto.split("/")[rutaFoto.split("/").size - 1]
        val destinoPath = FileSystems.getDefault().getPath(lista?.ruta + "/" + nombre)
        try {
            Files.move(origenPath, destinoPath, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            System.err.println(e)
        }
    }

    private fun deleteImgOld(file: File) {
        if (file.isDirectory) {
            val children = file.list()
            for (i in children.indices) {
                File(file, children[i]).delete()
            }
        }
    }

    private fun asignarVistas() {
        raBrDificultadEd = findViewById(R.id.raBrDificultadEd)
        etPreguntaEd = findViewById(R.id.etPreguntaEd)
        etRespuestaEd = findViewById(R.id.etRespuestaEd)
        etObservacionEd = findViewById(R.id.etObservacionEd)
        btnEliminarEd = findViewById(R.id.btnEliminarEd)
        cbNuevoEd = findViewById(R.id.cbNuevoEd)
        cbFormaMasivaEd = findViewById(R.id.cbFormaMasivaEd)
        btnColorEd = findViewById(R.id.btnColorEd)
        btnAplicarEd = findViewById(R.id.btnAplicarEd)
        btnCancelarEd = findViewById(R.id.btnCancelarEd)
        btnSiguienteEd = findViewById(R.id.btnSiguienteEd)
        imgBtnInfo_EA = findViewById(R.id.imgBtnInfo_EA)
        fLInfoMasivo_AE = findViewById(R.id.fLInfoMasivo_AE)
        imgBtnCameraPregunta_Ed = findViewById(R.id.imgBtnCameraPregunta_Ed)
        imgBtnCameraRespuesta_Ed = findViewById(R.id.imgBtnCameraRespuesta_Ed)
        imgV_captura_EA = findViewById(R.id.imgV_captura_EA)
        FL_containerCapture_EA = findViewById(R.id.FL_containerCapture_EA)

        btnEliminarEd!!.setOnClickListener(this)
        btnColorEd!!.setOnClickListener(this)
        btnAplicarEd!!.setOnClickListener(this)
        btnCancelarEd!!.setOnClickListener(this)
        btnSiguienteEd!!.setOnClickListener(this)
        fLInfoMasivo_AE!!.setOnClickListener(this)
        imgBtnInfo_EA!!.setOnClickListener(this)
        imgBtnCameraPregunta_Ed!!.setOnClickListener(this)
        imgBtnCameraRespuesta_Ed!!.setOnClickListener(this)
        imgV_captura_EA!!.setOnClickListener(this)
        cbFormaMasivaEd!!.setOnClickListener(this)
    }

    override fun onBackPressed() {
        if(ultimo != -1){
            super.onBackPressed()
        }else{
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("id_lista", id_lista)
            startActivity(intent)
        }
        finish()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}
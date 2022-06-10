package com.efom.randomlearn

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.Adapters.Main.AdapVH_Main
import com.efom.randomlearn.MODELS.Lista
import com.efom.randomlearn.SQLITE.DBSQLite
import com.efom.randomlearn.Utiles.CONST
import com.efom.randomlearn.Utiles.DataController
import java.io.*
import java.nio.charset.Charset
import java.util.*


class MainActivity : AppCompatActivity() {
    var adapVH_Main: AdapVH_Main? = null
    var recyclerView: RecyclerView? = null
    var listas: ArrayList<Lista>? = null
    var DBSQLite: DBSQLite? = null
    var context: Context? = null
    var prefs: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private val ES_CONGELAR_CARA = "congelarCara"
    private val ES_SIEMPRE_PREGUNTA = "esSiemprePregunta"
    private lateinit var directorioRandomSD: String
    private lateinit var pathHome: String

    var imgVSetting_main: ImageButton? = null
    var imgBtn_Inportar_MA: ImageButton? = null
    var imgBtn_NuevaLista_MA: ImageButton? = null
    var imgBtn_Cloud_MA: ImageButton? = null
    var etSearch_MA: EditText? = null
    var imgVSearch_main: ImageView? = null
    var title_MA: TextView? = null

    @RequiresApi(Build.VERSION_CODES.R)
    val listPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgVSetting_main = findViewById(R.id.imgVSetting_main)
        imgBtn_Inportar_MA = findViewById(R.id.imgBtn_Inportar_MA)
        imgBtn_NuevaLista_MA = findViewById(R.id.imgBtn_NuevaLista_MA)
        imgBtn_Cloud_MA = findViewById(R.id.imgBtn_Cloud_MA)
        etSearch_MA = findViewById(R.id.etSearch_MA)
        imgVSearch_main = findViewById(R.id.imgVSearch_main)
        title_MA = findViewById(R.id.title_MA)

        directorioRandomSD = getString(R.string.carpeta)
        Log.d("76 MainActivity.kt", "onCreate: 1")
        pathHome = this.getExternalFilesDir(null)!!.path.split("/Android/")[0]
        Log.d("78 MainActivity.kt", "onCreate: 2")
        context = applicationContext
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        editor = prefs!!.edit()

        //Definir? tema
        if (prefs?.getBoolean(getString(R.string.temaOscuro), true) == true) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        if (prefs?.getString("path_home", null) == null) {
            editor?.putString("path_home", pathHome)!!.apply()
        }

        if (prefs?.getString("separador", null) == null) {
            editor?.putString("separador", ";")
            editor?.putBoolean(ES_SIEMPRE_PREGUNTA, true)
            editor?.putBoolean(ES_CONGELAR_CARA, false)
            editor?.apply()
        }

        DBSQLite = DBSQLite(context)

        listas = DBSQLite!!.getListasCards()
        setupRecyclerView(listas!!)

        clicksViews()

        validatePermissions()
    }


    private fun clicksViews() {
        imgVSetting_main!!.setOnClickListener {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }

        /** INTENT FOLDER (cargar cvs) */
        imgBtn_Inportar_MA!!.setOnClickListener { openFolder() }

        /** DIALOGO NUEVA LISTA */
        imgBtn_NuevaLista_MA!!.setOnClickListener {
            dialogNewList()
        }

        /**
         * Cloud btn */
        imgBtn_Cloud_MA!!.setOnClickListener {
            /*val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                finish()*/
        }


        /** BUSCAR LISTA */
        etSearch_MA!!.visibility = View.GONE
        imgVSearch_main!!.setOnClickListener {
            if (etSearch_MA!!.visibility == View.GONE) {
                title_MA!!.visibility = View.GONE
                etSearch_MA!!.visibility = View.VISIBLE
                showSoftKeyboard(etSearch_MA!!)
            } else {
                title_MA!!.visibility = View.VISIBLE
                etSearch_MA!!.visibility = View.GONE
            }
        }
        etSearch_MA!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val filteredList = arrayListOf<Lista>()
                if (p0.toString() != "") {
                    for (item in listas!!) {
                        if (item.nombre?.lowercase(Locale.ROOT)
                                ?.contains(p0.toString().lowercase(Locale.ROOT)) == true
                        ) {
                            filteredList.add(item)
                        }
                    }
                    setupRecyclerView(filteredList)
                } else {
                    setupRecyclerView(listas)
                }
            }

        })
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupRecyclerView(listTarjetas: ArrayList<Lista>?) {
        adapVH_Main = AdapVH_Main(this, this)
        recyclerView = findViewById(R.id.recycler_main)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapVH_Main
        recyclerView?.setHasFixedSize(true)

        for (i in listTarjetas!!.indices) {
            adapVH_Main!!.addLista(
                listTarjetas[i],
                listTarjetas[i].id_lista.let { DBSQLite!!.contadorResultados(it!!) }
            )
        }
    }

    private fun dialogNewList() {
        val nombreDeLaLista = EditText(this@MainActivity)
        nombreDeLaLista.hint = "Nombre"
        nombreDeLaLista.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        AlertDialog.Builder(this@MainActivity).setMessage("NUEVA LISTA")
            .setView(nombreDeLaLista)
            .setPositiveButton("Aceptar") { dialogInterface, _ ->
                // Crear carpeta, en caso de que exista la renombra
                val ruta = "$pathHome/$directorioRandomSD/" + nombreDeLaLista.text.toString().trim()
                val file =
                    File("$pathHome/$directorioRandomSD/" + nombreDeLaLista.text.toString().trim())

                //Validar, sino no guardar en la base de datos
                val lista = Lista(
                    -1,
                    nombreDeLaLista.text.toString().trim(),
                    ruta,
                    "#ffffff",
                    "",
                    "",
                    -1,
                    "00:00",
                    CONST.ACTIVO
                )
                if (!file.exists()) {
                    file.mkdirs()
                }
                val id_lista = DBSQLite!!.addLista(lista)
                val intent = Intent(applicationContext, EditActivity::class.java)
                intent.putExtra("id_lista", id_lista)
                intent.putExtra("id_tarjeta", -1)
                startActivity(intent)
                dialogInterface.dismiss()
                finish()
            }.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    /** CREAR CARPETA  */
    private fun createDirectory(nameDirectory: String?): File {
        val directory = File(nameDirectory!!)
        if (!directory.mkdirs()) {
            Log.e("236 MainActivity.kt", "createDirectory: error, ruta: $pathHome")
        } else {
            Log.d("238 MainActivity.kt", "createDirectory: exitoso, ruta: $pathHome")
        }
        return directory
    }

    /** ABRIR CARPETA  */
    private fun openFolder() {
        try {
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            resultLauncher.launch(Intent.createChooser(intent, "Random"))
        } catch (e: ActivityNotFoundException) {
            Log.e("-m-MainActivity", "L111-openFolder = = = $e")
        }
    }

    /**  RECIBE ARCHIVO Y SUBE A SQLITE  */
    private var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            var idNewLista = 0
            val lista = Lista(-1, "-", "#ffffff", "", "", "", -1, "00:00", CONST.ACTIVO)
            val separador = prefs!!.getString("separador", ";")!!.toString()
            val dataController = DataController()
            try {
                val arPathFull = data!!.data!!.path!!.split("/").toTypedArray()
                val arPathXiomi = data.data!!.path.toString().split("primary:")
                lista.nombre = arPathFull[arPathFull.size - 1]
                val pathDestinity = pathHome + "/" + directorioRandomSD + "/" + lista.nombre
                lista.ruta = pathDestinity

                val ext = lista.nombre!!.takeLast(4)
                if (when (ext) {
                        ".txt" -> true
                        ".csv" -> true
                        else -> false
                    }
                ) {
                    val pathFull = if (arPathXiomi.size > 1) {
                        pathHome + "/" + arPathXiomi[1]
                    } else {
                        data.data!!.path.toString()
                    }
                    val arPathOrigin = pathFull.split("/").toMutableList()
                    arPathOrigin.removeLast()
                    val pathOrigin = arPathOrigin.joinToString("/")

                    idNewLista = DBSQLite!!.addLista(lista)
                    lista.id_lista = idNewLista

                    val file = File(pathFull)
                    val inputStream: InputStream = FileInputStream(file)
                    val reader =
                        BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                    val tempListTarjetas =
                        dataController.getTransformDataMassive(reader, separador, idNewLista)
                    inputStream.close()
                    DBSQLite!!.addTarjetas(tempListTarjetas)

                    createDirectory(lista.ruta!!)

                    copyFile(pathOrigin + "/" + lista.nombre, "$pathDestinity/${lista.nombre}")
                    for (tempListTarjeta in tempListTarjetas) {
                        try {
                            var isIMG = when {
                                tempListTarjeta.pregunta!!.lowercase(Locale.ROOT)
                                    .endsWith(".png") -> true
                                tempListTarjeta.pregunta!!.lowercase(Locale.ROOT)
                                    .endsWith(".jpeg") -> true
                                tempListTarjeta.pregunta!!.lowercase(Locale.ROOT)
                                    .endsWith(".jpg") -> true
                                tempListTarjeta.pregunta!!.lowercase(Locale.ROOT)
                                    .endsWith(".gif") -> true
                                tempListTarjeta.pregunta!!.lowercase(Locale.ROOT)
                                    .endsWith(".svg") -> true
                                else -> false
                            }
                            if (isIMG) {
                                copyFile(
                                    "$pathOrigin/${tempListTarjeta.pregunta}",
                                    "$pathDestinity/${tempListTarjeta.pregunta}"
                                )
                            }

                            isIMG = when {
                                tempListTarjeta.respuesta!!.lowercase(Locale.ROOT)
                                    .endsWith(".png") -> true
                                tempListTarjeta.respuesta!!.lowercase(Locale.ROOT)
                                    .endsWith(".jpeg") -> true
                                tempListTarjeta.respuesta!!.lowercase(Locale.ROOT)
                                    .endsWith(".jpg") -> true
                                tempListTarjeta.respuesta!!.lowercase(Locale.ROOT)
                                    .endsWith(".gif") -> true
                                tempListTarjeta.respuesta!!.lowercase(Locale.ROOT)
                                    .endsWith(".svg") -> true
                                else -> false
                            }
                            if (isIMG) {
                                copyFile(
                                    "$pathOrigin/${tempListTarjeta.respuesta}",
                                    "$pathDestinity/${tempListTarjeta.respuesta}"
                                )
                            }
                        } catch (ex: Exception) {
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e("MainActivity", "L155-onActivityResult = = = " + ex.message)
            }

            adapVH_Main!!.addLista(lista, DBSQLite!!.contadorResultados(idNewLista))

        }

    }

    private fun copyFile(filePath: String, destiny: String) {
        File(filePath).copyTo(File(destiny))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun validatePermissions() {
        for (permission in listPermissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, listPermissions, 123)
                break
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            for (i in grantResults.indices) {
                if (grantResults[i] == -1) {
                    Log.d(
                        "355 MainActivity.kt",
                        "onRequestPermissionsResult: sin permisos" + grantResults[i]
                    )
                    //val res = listPermissions[i].split(".")
                    //Toast.makeText(context, "No ha concedido permisos a "+res[res.size - 1], Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
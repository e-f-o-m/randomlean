package com.efom.randomlearn

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.efom.randomlearn.adapters.main.AdapVH_Main
import com.efom.randomlearn.controllers.ControllerMain
import com.efom.randomlearn.models.Metadata
import com.efom.randomlearn.utils.CONSTS
import com.efom.randomlearn.database.MyDB
import com.efom.randomlearn.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    var metadata: ArrayList<Metadata>? = null
    lateinit var db: MyDB
    var context: Context? = null
    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val ES_CONGELAR_CARA = "congelarCara"
    private val ES_SIEMPRE_PREGUNTA = "esSiemprePregunta"
    private lateinit var directorioRandomSD: String
    private lateinit var pathHome: String
    private lateinit var binding: ActivityMainBinding

    var adapVH_Main: AdapVH_Main? = null

    @RequiresApi(Build.VERSION_CODES.R)
    val listPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = MyDB.getDB(applicationContext)!!

        directorioRandomSD = getString(R.string.carpeta)
        pathHome = this.getExternalFilesDir(null)!!.path.split("/Android/")[0]
        context = applicationContext

        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        editor = prefs.edit()

        //Definir? tema
        if (prefs.getBoolean(CONSTS.IS_DARK, true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        if (prefs.getString("path_home", null) == null) {
            editor.putString("path_home", pathHome)!!.apply()
        }

        if (prefs.getString("separador", null) == null) {
            editor.putString("separador", ";")
            editor.putBoolean(ES_SIEMPRE_PREGUNTA, true)
            editor.putBoolean(ES_CONGELAR_CARA, false)
            editor.apply()
        }


        metadata = ArrayList(db.metadataDAO().getAll())
        setupRecyclerView(metadata!!)

        clicksViews()

        validatePermissions()
    }




    private fun clicksViews() {
        binding.imgVSettingMain.setOnClickListener {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.imgBtnInportarMA.setOnClickListener { openFolder() }

        binding.imgBtnNuevaListaMA.setOnClickListener {
            dialogNewList()
        }

        /** BUSCAR LISTA */
        binding.etSearchMA.visibility = View.GONE
        binding.imgVSearchMain.setOnClickListener {
            if (binding.etSearchMA.visibility == View.GONE) {
                binding.titleMA.visibility = View.GONE
                binding.etSearchMA.visibility = View.VISIBLE
                showSoftKeyboard(binding.etSearchMA)
            } else {
                binding.titleMA.visibility = View.VISIBLE
                binding.etSearchMA.visibility = View.GONE
            }
        }
        binding.etSearchMA.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val filteredList = arrayListOf<Metadata>()
                if (p0.toString() != "") {
                    for (item in metadata!!) {
                        if (item.name?.lowercase(Locale.ROOT)
                                ?.contains(p0.toString().lowercase(Locale.ROOT)) == true
                        ) {
                            filteredList.add(item)
                        }
                    }
                    setupRecyclerView(filteredList)
                } else {
                    setupRecyclerView(metadata)
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

    private fun setupRecyclerView(listTarjetas: ArrayList<Metadata>?) {
        adapVH_Main = AdapVH_Main(this, this)
        binding.recyclerMain.layoutManager = LinearLayoutManager(context)
        binding.recyclerMain.adapter = adapVH_Main
        binding.recyclerMain.setHasFixedSize(true)

        for (i in listTarjetas!!.indices) {
            adapVH_Main!!.addLista(
                listTarjetas[i],
                intArrayOf (
                    db.cardsDAO().countByMetaCompleted(listTarjetas[i].idMetadata!!),
                    db.cardsDAO().countByMeta(listTarjetas[i].idMetadata!!)

                )
            )
        }
    }

    private fun dialogNewList() {
        val metadataName = EditText(this@MainActivity)
        metadataName.hint = "Nombre"
        metadataName.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        AlertDialog.Builder(this@MainActivity).setMessage("NUEVA LISTA")
            .setView(metadataName)
            .setPositiveButton("Aceptar") { dialogInterface, _ ->
                // Crear carpeta, en caso de que exista la renombra
                val metadata = ControllerMain().setNewMetadata(metadataName.text.toString().trim(), applicationContext)
                val intent = Intent(applicationContext, EditActivity::class.java)
                intent.putExtra("id_lista", metadata.idMetadata)
                intent.putExtra("id_tarjeta", -1)
                startActivity(intent)
                dialogInterface.dismiss()
                finish()
            }.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
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
            val metadata = ControllerMain().setFileToSqlite(result.data!!, applicationContext)

            adapVH_Main!!.addLista(metadata,
                intArrayOf(
                    db.cardsDAO().countByMeta(metadata.idMetadata!!),
                    db.cardsDAO().countByMetaCompleted(metadata.idMetadata!!)
                )
                )

        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
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

    override fun onRequestPermissionsResult(reqCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(reqCode, permissions, grantResults)
        if (reqCode == 123) {
            for (i in grantResults.indices) {
                if (grantResults[i] == -1) {
                    Log.d(
                        "355 MainActivity.kt",
                        "onRequestPermissionsResult: sin permisos" + grantResults[i]
                    )
                }
            }
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
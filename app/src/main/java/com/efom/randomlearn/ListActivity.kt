package com.efom.randomlearn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.Adapters.Lista.AdapVH_Lista
import com.efom.randomlearn.MODELS.Lista
import com.efom.randomlearn.MODELS.Tarjeta
import com.efom.randomlearn.SQLITE.DBSQLite
import com.efom.randomlearn.Utiles.CONST
import com.efom.randomlearn.Utiles.ConstFIREBASE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ListActivity : AppCompatActivity(), View.OnClickListener {
    private var adapVH_Lista: AdapVH_Lista? = null
    var recyclerView: RecyclerView? = null
    private var DBSQLite: DBSQLite? = null
    private var context: Context? = null
    var prefs: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private var listTarjetas: ArrayList<Tarjeta>? = null
    private var id_lista = 0
    private var nombre_lista = ""
    private val constFR = ConstFIREBASE()
    private lateinit var auth: FirebaseAuth
    private var userName = ""
    private lateinit var pathHome: String
    private lateinit var directorioRandomSD: String
    //donde se guarda la foto
    var separador: String? = null
    var currentPhotoPath: String? = null
    var currentPhotoName: String? = null
    var imgActions_LA: ImageView? = null
    var imgBtnAdd_LA: ImageView? = null
    var imgVSearch_LA: ImageView? = null
    var title_LA: TextView? = null
    var title_LWA: TextView? = null
    var etSearch_LA: EditText? = null
    var etSearch_LWA: EditText? = null

    private lateinit var dbFrb: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        imgActions_LA  = findViewById(R.id.imgActions_LA)
        imgBtnAdd_LA  = findViewById(R.id.imgBtnAdd_LA)
        imgVSearch_LA  = findViewById(R.id.imgVSearch_LA)
        title_LA = findViewById(R.id.title_LA)
        title_LWA = findViewById(R.id.title_LWA)
        etSearch_LA = findViewById(R.id.etSearch_LA)
        etSearch_LWA = findViewById(R.id.etSearch_LWA)

        imgActions_LA!!.setOnClickListener(this)
        imgBtnAdd_LA!!.setOnClickListener(this)
        imgVSearch_LA!!.setOnClickListener(this)

        context = applicationContext
        DBSQLite = DBSQLite(context)
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        separador = prefs!!.getString("separador", ";").toString()
        pathHome = prefs!!.getString("path_home", ";").toString()
        directorioRandomSD = getString(R.string.carpeta)


        inputSearchList()

        if (intent.extras != null) {
            id_lista = intent.getIntExtra(getString(R.string.id_lista), -1)
            nombre_lista = intent.getStringExtra(getString(R.string.nombre_lista)).toString()
            listTarjetas = DBSQLite!!.getTarjetas(id_lista)
        }

        setupRefreshRcVw(listTarjetas)
        checkExternalStoragePermission()

        auth = FirebaseAuth.getInstance()
        userFirebase()


        //FIXME: btn Learn dialog
        /*val imgBtnNLearnDay_LA: ImageButton = findViewById(R.id.imgBtnNLearnDay_LA)
        imgBtnNLearnDay_LA.setOnClickListener { dayLearn() }*/

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(
            getString(R.string.id_lista),
            intent.getIntExtra(getString(R.string.id_lista), -1)
        )
        outState.putString(
            getString(R.string.nombre_lista),
            intent.getStringExtra(getString(R.string.nombre_lista))
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState)
        }
        id_lista = savedInstanceState!!.getInt(getString(R.string.id_lista), -1)
        nombre_lista = savedInstanceState!!.getString(getString(R.string.nombre_lista)).toString()
        listTarjetas = DBSQLite!!.getTarjetas(id_lista)
    }


    fun showSoftKeyboard(view: View?) {
        if (view!!.requestFocus()) {
            val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupRefreshRcVw(listTarjetas: ArrayList<Tarjeta>?) {
        adapVH_Lista = AdapVH_Lista(this)
        recyclerView = findViewById(R.id.recycler_lista)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapVH_Lista
        for (i in listTarjetas!!.indices) {
            adapVH_Lista!!.addLista(listTarjetas[i])
        }
    }

    override fun onStart() {
        super.onStart()
        listTarjetas = DBSQLite!!.getTarjetas(id_lista)
        setupRefreshRcVw(listTarjetas)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgBtnAdd_LA -> {
                val intent = Intent(applicationContext, EditActivity::class.java)
                intent.putExtra("id_lista", id_lista)
                intent.putExtra("nombre_lista", nombre_lista)
                intent.putExtra("id_tarjeta", -1)
                startActivity(intent)
            }
            R.id.imgActions_LA -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Acciones")
                    .setItems(
                        R.array.actions
                    ) { dialog, which ->
                        when (which) {
                            0 -> eliminar();
                            1 -> renombrar();
                            2 -> exportar();
                            3 -> reresetSt();
                            4 -> download();
                            5 -> upload();
                        }
                        dialog.dismiss()
                        //Toast.makeText(this, "test $which", Toast.LENGTH_SHORT).show()
                    }
                builder.create().show()
            }
            R.id.imgVSearch_LA -> {
                if (etSearch_LA!!.visibility == View.GONE) {
                    title_LA!!.visibility = View.GONE
                    etSearch_LA!!.visibility = View.VISIBLE
                    showSoftKeyboard(etSearch_LA)
                } else {
                    title_LA!!.visibility = View.VISIBLE
                    etSearch_LA!!.visibility = View.GONE
                }
            }
        }
    }

    /*fun dayLearn(){
        Log.d("190 ListActivity.kt", "dayLearn: 2")
        val v: View = this.layoutInflater.inflate(R.layout.dialog_nlearns, null)
        AlertDialog.Builder(this).setMessage("Cantidad preguntas al día").setView(v)
            .setPositiveButton("Aceptar") { dialogInterface, _ ->
                Log.d("195 ListActivity.kt", "dayLearn: 3")
                val checkBox = v.findViewById<CheckBox>(R.id.checkbox_DNL)
                val editText = v.findViewById<EditText>(R.id.etNLearn_DNL)
                var estate = 0;
                if(checkBox.isChecked){
                    estate = 1
                }
                val learnOrdered = listTarjetas!!.sortedByDescending { tarjeta -> tarjeta.dificultad }
                val nDay =  (editText.text.toString().toInt() - 1)
                Log.d("206 ListActivity.kt", "dayLearn: $nDay")
                val learnValidate = db_sqLite!!.selectLearnIdList(id_lista)
                var repeate = 0;

                if (learnValidate.size > 0){
                    repeate = learnValidate[0].nRepeat!!
                    //fixme:
                    //db_sqLite!!.deleteAllLearnIdList(id_lista)
                }

                for (i in 0..nDay){
                    Log.d("216 ListActivity.kt", "------: ${learnOrdered[i].id_lista}")
                    Log.d("218 ListActivity.kt", "------: ${learnOrdered[i].id_tarjeta}")
                    val learn: Learn = Learn()
                    learn.create(0, id_lista, learnOrdered[i].id_tarjeta, nDay, estate, repeate)
                    // fixme:
                    // db_sqLite!!.insertLearn(learn)
                }
                Log.d("217 ListActivity.kt", "dayLearn: 4")
                dialogInterface.dismiss()
            }.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
        Log.d("221 ListActivity.kt", "dayLearn: 5")
    }
    */
    fun eliminar() {
        val v: View = layoutInflater.inflate(R.layout.dialog_delete_list, null)
        AlertDialog.Builder(this).setMessage("Seguro que desea eliminar la lista?")
            .setView(v)
            .setPositiveButton("Aceptar") { dialogInterface, i ->
                val checkBox = v.findViewById<CheckBox>(R.id.checkbox_DDL)

                if (checkBox.isChecked) {
                    deleteFirebase()
                }
                DBSQLite!!.deleteLista(id_lista)
                editor = prefs!!.edit()
                editor!!.putInt("lista_widget", -1)
                Toast.makeText(context, "Lista eliminada", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@ListActivity, MainActivity::class.java))
                dialogInterface.dismiss()
                finish()
            }.setNegativeButton("Cancelar") { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }

    fun renombrar() {
        val nombreLista = EditText(this)
        nombreLista.hint = DBSQLite!!.getListaName(id_lista)
        nombreLista.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        AlertDialog.Builder(this).setMessage("Renombrar Lista")
            .setView(nombreLista)
            .setPositiveButton("Aceptar") { dialogInterface, i ->
                /** RENOMBRAR CARPETA  */
//                            /** RENOMBRAR CARPETA  */
                val file = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/RANDOM/" + DBSQLite!!.getListaName(id_lista), ""
                )
                val file2 = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/RANDOM/" + nombreLista.text.toString().trim { it <= ' ' },
                    ""
                )

                //Validar, sino no guardar en la base de datos
                if (file.renameTo(file2)) {
                    DBSQLite!!.updateNameLista(
                        Lista(
                            id_lista,
                            nombreLista.text.toString().trim { it <= ' ' },
                            "#ffffff",
                            "#ffffff",
                            "",
                            "",
                            -1,
                            "00:00",
                            CONST.ACTIVO
                        )


                    )
                    this.nombre_lista = nombreLista.text.toString().trim { it <= ' ' }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Error, la carpeta no existe",
                        Toast.LENGTH_LONG
                    ).show()
                }
                dialogInterface.dismiss()
            }.setNegativeButton("Cancelar") { dialogInterface, i -> dialogInterface.dismiss() }
            .show()

    }

    fun exportar() {
        AlertDialog.Builder(this).setMessage("Exportar Lista completa a Excel")
            .setPositiveButton("Aceptar") { dialogInterface, _ ->
                exportarCSV(id_lista)
                dialogInterface.dismiss()
            }.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    fun reresetSt() {
        AlertDialog.Builder(this).setMessage(getString(R.string.dialog_reiniciar_estrellas))
            .setPositiveButton("Aceptar") { dialogInterface, _ ->
                DBSQLite!!.updateResetStars(5.0, id_lista)
                Toast.makeText(applicationContext, "Dificultad reiniciada 5", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    //FIXME: nombre
    fun download() {
        if (!userName.isNullOrEmpty()) {
            dbFrb = Firebase.database.reference
            dbFrb.child(constFR.LISTAS).child(userName).child(id_lista.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var listPJTarjetasFB = ArrayList<Tarjeta>()
                        var pojo_tarjeta = Tarjeta()
                        dataSnapshot.child(constFR.TARJETAS).children.forEach {
                            try {
                                pojo_tarjeta =
                                    Tarjeta()
                                var nombre =
                                    dataSnapshot.child(constFR.NOMBRE_LISTA).value.toString()
                                pojo_tarjeta.pregunta = it.child(constFR.PREGUNTA).value.toString()
                                pojo_tarjeta.respuesta =
                                    it.child(constFR.RESPUESTA).value.toString()
                                pojo_tarjeta.color = it.child(constFR.COLOR).value.toString()
                                pojo_tarjeta.detalles =
                                    it.child(constFR.OBSERVACION).value.toString()
                                pojo_tarjeta.id_lista =
                                    it.child(constFR.ID_LISTA).value.toString().toInt()
                                pojo_tarjeta.dificultad =
                                    it.child(constFR.DIFICULTAD).value.toString().toDouble()
                                pojo_tarjeta.id_tarjeta =
                                    it.child(constFR.ID_TARJETA).value.toString().toInt()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            DBSQLite!!.updateTarjetaFB(pojo_tarjeta)
                            listPJTarjetasFB!!.add(pojo_tarjeta!!)
                        }
                        setupRefreshRcVw(listPJTarjetasFB!!)
                        Toast.makeText(context, "Datos Descargados", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("311 ListActivity.kt", "onCancelled: error ¿: " + error.message)
                    }
                })
        }
    }

    fun upload() {
        if (!userName.isNullOrEmpty()) {
            dbFrb = Firebase.database.reference
            dbFrb.child(constFR.LISTAS).child(userName).child(id_lista.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dbFrb.child(constFR.LISTAS)
                            .child("efom444")
                            .child(id_lista.toString())
                            .child(constFR.NOMBRE_LISTA).setValue(nombre_lista)

                        dbFrb.child(constFR.LISTAS)
                            .child("efom444")
                            .child(id_lista.toString())
                            .child(constFR.TARJETAS)
                            .setValue(listTarjetas).addOnSuccessListener {
                                Toast.makeText(context, "Datas Subidos", Toast.LENGTH_SHORT).show()
                            }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("341 XXX ListActivity", "onCancelled: error")
                    }
                })
        }
    }

    fun deleteFirebase(){
        if (!userName.isNullOrEmpty()) {
            dbFrb = Firebase.database.reference
            dbFrb.child(constFR.LISTAS).child(userName).child(id_lista.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            dataSnapshot.ref.removeValue()
                        }
                        Toast.makeText(context, "Datos Descargados", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("311 ListActivity.kt", "onCancelled: error ¿: " + error.message)
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*FL_containerCapture_LA!!.visibility = View.VISIBLE
            val f = File(currentPhotoPath)
            val contentUri = Uri.fromFile(f)
            imgV_captura_LA!!.setImageURI(contentUri)*/
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix    */
            ".jpg",  /* suffix    */
            storageDir /* directory */
        )
        currentPhotoPath = image.absolutePath
        currentPhotoName = currentPhotoPath!!.split("/".toRegex())
            .toTypedArray()[currentPhotoPath!!.split("/".toRegex()).toTypedArray().size - 1]
        return image
    }

    var TAG = "hola"
    private fun checkExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Permission not granted WRITE_EXTERNAL_STORAGE.")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    225
                )
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Permission not granted CAMERA.")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA),
                    226
                )
            }
        }
    }

    fun exportarCSV(idListSelect: Int) {
        val date = Date()
        val hourdateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH.mm")
        val historial = hourdateFormat.format(date)

        val listPojoTarjeta = DBSQLite!!.getTarjetas(idListSelect)
        val path = pathHome + "/"+ directorioRandomSD +"/" + DBSQLite!!.getListaName(idListSelect)

        val exportDir = File(path)

        if (!exportDir.exists()) {
            exportDir.mkdirs()
            Toast.makeText(
                context,
                "Directorio no existe, se creó uno nuevo",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            Toast.makeText(context, "Directorio si exiete", Toast.LENGTH_SHORT).show()
        }


        val file = File(
            path,
            DBSQLite!!.getListaName(idListSelect)
                    + "." + historial
                    + ".csv"
        )

        file.createNewFile()

        try {
            val csvWrite = CSVWriter(FileWriter(file.path))

            val arrStrTitulos = arrayOf("PREGUTA;RESPUESTA;DIFICULTAD;OBSERVACION;")
            csvWrite.writeNext(arrStrTitulos)
            for (index in listPojoTarjeta.indices) {
                val arrStr = arrayOf(
                    listPojoTarjeta[index].pregunta + ";" +
                            listPojoTarjeta[index].respuesta + ";" +
                            listPojoTarjeta[index].dificultad + ";" +
                            listPojoTarjeta[index].detalles
                )
                csvWrite.writeNext(arrStr)
            }

            csvWrite.close()

            Toast.makeText(
                context,
                "La base de datos fue exportada Satisfactoriamente",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            Log.d("336 ListActivity.kt", "exportarCSV: " + e)
            Toast.makeText(context, "Error al exportar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    fun userFirebase() {
        auth.currentUser?.let {
            if (it.email.toString().isNotEmpty()) {
                dbFrb = Firebase.database.reference
                Log.d("398 ListActivity.kt", "userFirebase: email " + it.email.toString())
                dbFrb.child(constFR.USERS).child(it.uid).child(constFR.USER_NAME)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            userName = dataSnapshot.value.toString()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("408 XXX ListActivity", "onCancelled: error")
                        }
                    })
            }
        }
    }


    private fun inputSearchList() {
        etSearch_LA!!.setOnClickListener {
            if (etSearch_LWA!!.visibility == View.GONE) {
                title_LWA!!.visibility = View.GONE
                etSearch_LWA!!.visibility = View.VISIBLE
                if (etSearch_LWA!!.requestFocus()) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(etSearch_LWA!!, InputMethodManager.SHOW_IMPLICIT)
                }
            } else {
                title_LWA!!.visibility = View.VISIBLE
                etSearch_LWA!!.visibility = View.GONE
            }
        }
        etSearch_LA!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                val filteredList: ArrayList<Tarjeta> = ArrayList<Tarjeta>()
                if (p0.toString() != "") {
                    for (item in listTarjetas!!) {
                        if (
                            item.pregunta!!.toLowerCase(Locale.ROOT)
                                .contains(p0.toString().toLowerCase(Locale.ROOT))
                            || item.respuesta?.toLowerCase(Locale.ROOT)
                            !!.contains(p0.toString().toLowerCase(Locale.ROOT))
                        ) {
                            filteredList?.add(item)
                        }
                    }
                    setupRefreshRcVw(filteredList)
                } else {
                    setupRefreshRcVw(listTarjetas)
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_TAKE_PHOTO = 1
    }
}
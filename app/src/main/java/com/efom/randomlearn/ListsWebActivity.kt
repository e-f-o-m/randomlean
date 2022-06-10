package com.efom.randomlearn

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.Adapters.Web.AdapVH_Web
import com.efom.randomlearn.MODELS.Tarjeta
import com.efom.randomlearn.SQLITE.DBSQLite
import com.efom.randomlearn.Utiles.CallBackAdapter
import com.efom.randomlearn.Utiles.ConstFIREBASE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class ListsWebActivity : AppCompatActivity(), CallBackAdapter {
    private lateinit var adapVH_Main: AdapVH_Web
    private lateinit var recyclerView: RecyclerView
    private lateinit var listTarjetas: ArrayList<Tarjeta>
    private lateinit var DBSQLite: DBSQLite
    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var dbFrb: DatabaseReference
    private lateinit var constFR: ConstFIREBASE
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar_LWA: ProgressBar
    var nombre = ""
    private var userName = ""

    private lateinit var imgVSearch_LWA: ImageView
    private lateinit var etSearch_LWA: EditText
    private lateinit var title_LWA: TextView
    private lateinit var recyclerView_LWA: RecyclerView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lists_web)

        imgVSearch_LWA = findViewById(R.id.imgVSearch_LWA)
        etSearch_LWA = findViewById(R.id.etSearch_LWA)
        title_LWA = findViewById(R.id.title_LWA)
        recyclerView_LWA = findViewById(R.id.recycler_LWA)

        context = applicationContext
        DBSQLite = DBSQLite(context)
        dbFrb = Firebase.database.reference
        constFR = ConstFIREBASE()
        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        editor = prefs?.edit()

        etSearch_LWA.visibility = View.GONE
        inputSearchList()


        auth = FirebaseAuth.getInstance()
        progressBar_LWA = findViewById(R.id.progressBar_LWA)
        userFirebase()
    } //fin onCreate

    /* Buscar nombre lista */
    private fun inputSearchList() {
        imgVSearch_LWA.setOnClickListener {
            if (etSearch_LWA.visibility == View.GONE) {
                title_LWA.visibility = View.GONE
                etSearch_LWA.visibility = View.VISIBLE
                if (etSearch_LWA.requestFocus()) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(etSearch_LWA, InputMethodManager.SHOW_IMPLICIT)
                }
            } else {
                title_LWA.visibility = View.VISIBLE
                etSearch_LWA.visibility = View.GONE
            }
        }
        etSearch_LWA.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val filteredList = arrayListOf<Tarjeta>()
                //fixme: nombre
                var nombre = ""
                if (p0.toString() != "") {
                    for (item in listTarjetas!!) {
                        if (nombre.toLowerCase(Locale.ROOT).contains(p0.toString().toLowerCase(Locale.ROOT))) {
                            filteredList?.add(item)
                        }
                    }
                    setupRefreshRcVw(filteredList)
                } else {
                    setupRefreshRcVw(listTarjetas)
                }
            }

        }) //fin buscar lista
    }


    private fun setupRefreshRcVw(listTarjetas: ArrayList<Tarjeta>?) {
        adapVH_Main = AdapVH_Web(this, this)
        recyclerView = findViewById(R.id.recycler_LWA)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapVH_Main
        recyclerView?.setHasFixedSize(true)

        for (i in listTarjetas!!.indices) {
            adapVH_Main!!.addLista(listTarjetas?.get(i) /*db_sqLite!!.contadorResultados(listPojoTarjetas?.get(i)!!.id_lista)*/)
        }
    }


    fun requestListFirebase() {
        if (userName.isNotEmpty()) {
            dbFrb = Firebase.database.reference
            dbFrb.child(constFR.LISTAS).child(userName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listTarjetas = ArrayList<Tarjeta>()
                    Log.d("120 ListsWebActivity.kt", "onDataChange: firebase")
                    dataSnapshot.children.forEach {
                        var pojo_tarjeta = Tarjeta()
                        try {
                            nombre = it.child(constFR.NOMBRE_LISTA).value.toString()
                            pojo_tarjeta.id_lista = it.key.toString().toInt()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show()
                        }
                        listTarjetas.add(pojo_tarjeta!!)
                    }
                    setupRefreshRcVw(listTarjetas!!)
                    progressBar_LWA.isIndeterminate = false
                    progressBar_LWA.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    fun userFirebase() {
        auth.currentUser?.let {
            if (it.email.toString().isNotEmpty()) {
                dbFrb = Firebase.database.reference
                dbFrb.child(constFR.USERS).child(it.uid).child(constFR.USER_NAME)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            userName = dataSnapshot.value.toString()
                            Toast.makeText(context, userName, Toast.LENGTH_SHORT).show()
                            requestListFirebase()
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("408 XXX ListActivity", "onCancelled: error")
                        }
                    })
            }
        }
    }


    fun requestListFrSQLite(position: Int) {
        if (!userName.isNullOrEmpty()) {

            dbFrb = Firebase.database.reference
            //fixme
            dbFrb.child(constFR.LISTAS).child(userName)
                .child(listTarjetas[position].id_lista.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var listPojoTarjetasFR = ArrayList<Tarjeta>()
                        var pojo_tarjeta =
                            Tarjeta()
                        nombre = dataSnapshot.child(constFR.NOMBRE_LISTA).value.toString()
                        Log.d("173 ListsWebActivity.kt", "onDataChange: firebase sqlite")
                        dataSnapshot.child(constFR.TARJETAS).children.forEach {
                            try {
                                pojo_tarjeta =
                                    Tarjeta()
                                pojo_tarjeta.pregunta = it.child(constFR.PREGUNTA).value.toString()
                                pojo_tarjeta.respuesta = it.child(constFR.RESPUESTA).value.toString()
                                pojo_tarjeta.color = it.child(constFR.COLOR).value.toString()
                                pojo_tarjeta.detalles = it.child(constFR.OBSERVACION).value.toString()
                                pojo_tarjeta.id_lista = it.child(constFR.ID_LISTA).value.toString().toInt()
                                pojo_tarjeta.dificultad = it.child(constFR.DIFICULTAD).value.toString().toDouble()
                                pojo_tarjeta.id_tarjeta = it.child(constFR.ID_TARJETA).value.toString().toInt()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show()
                            }
                            listPojoTarjetasFR!!.add(pojo_tarjeta!!)
                        }
                        if (!DBSQLite.getListaName(listTarjetas[position].id_lista)
                                .equals(nombre)
                        ) {
                            DBSQLite.putNombreListaID(nombre,
                                listTarjetas[position].id_lista
                            );
                            //fixme:
                            //db_sqLite.insertListaIDsTarjeta(listPojoTarjetasFR)
                            onBackPressed()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun intrfClick(pos: Int) {
        requestListFrSQLite(pos)
    }


}
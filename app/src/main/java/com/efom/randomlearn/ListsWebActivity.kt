package com.efom.randomlearn

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.adapters.web.AdapVH_Web
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.adapters.CallBackAdapter
import com.efom.randomlearn.utils.ConstFIREBASE
import com.efom.randomlearn.database.MyDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class ListsWebActivity : AppCompatActivity(), CallBackAdapter {
    private lateinit var adapVH_Main: AdapVH_Web
    private lateinit var recyclerView: RecyclerView
    private lateinit var listCards: ArrayList<Card>
    private lateinit var db: MyDB
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
        db = MyDB.getDB(context)!!
        dbFrb = Firebase.database.reference
        constFR = ConstFIREBASE
        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        editor = prefs.edit()!!

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
                val filteredList = arrayListOf<Card>()
                //fixme: nombre
                var nombre = ""
                if (p0.toString() != "") {
                    for (item in listCards!!) {
                        if (nombre.toLowerCase(Locale.ROOT).contains(p0.toString().toLowerCase(Locale.ROOT))) {
                            filteredList?.add(item)
                        }
                    }
                    setupRefreshRcVw(filteredList)
                } else {
                    setupRefreshRcVw(listCards)
                }
            }

        }) //fin buscar lista
    }


    private fun setupRefreshRcVw(listCards: ArrayList<Card>?) {
        adapVH_Main = AdapVH_Web(this, this)
        recyclerView = findViewById(R.id.recycler_LWA)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapVH_Main
        recyclerView?.setHasFixedSize(true)

        for (i in listCards!!.indices) {
            listCards.get(i)
                .let { adapVH_Main.addLista(it /*db_sqLite!!.contadorResultados(listPojoTarjetas?.get(i)!!.id_lista)*/) }
        }
    }


    fun requestListFirebase() {
        if (userName.isNotEmpty()) {
            dbFrb = Firebase.database.reference
            dbFrb.child(constFR.LISTAS).child(userName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listCards = ArrayList<Card>()
                    Log.d("120 ListsWebActivity.kt", "onDataChange: firebase")
                    dataSnapshot.children.forEach {
                        var pojo_card = Card()
                        try {
                            nombre = it.child(constFR.NOMBRE_LISTA).value.toString()
                            pojo_card.idMetadata = it.key.toString().toInt()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show()
                        }
                        listCards.add(pojo_card!!)
                    }
                    setupRefreshRcVw(listCards!!)
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
                .child(listCards[position].idMetadata.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var listPojoTarjetasFR = ArrayList<Card>()
                        var pojo_card =
                            Card()
                        nombre = dataSnapshot.child(constFR.NOMBRE_LISTA).value.toString()
                        Log.d("173 ListsWebActivity.kt", "onDataChange: firebase sqlite")
                        dataSnapshot.child(constFR.TARJETAS).children.forEach {
                            try {
                                pojo_card =
                                    Card()
                                pojo_card.answer = it.child(constFR.PREGUNTA).value.toString()
                                pojo_card.question = it.child(constFR.RESPUESTA).value.toString()
                                pojo_card.resourceAnswer = it.child(constFR.COLOR).value.toString()
                                pojo_card.details = it.child(constFR.OBSERVACION).value.toString()
                                pojo_card.idMetadata = it.child(constFR.ID_LISTA).value.toString().toInt()
                                pojo_card.difficulty = it.child(constFR.DIFICULTAD).value.toString().toDouble()
                                pojo_card.idCard = it.child(constFR.ID_TARJETA).value.toString().toInt()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show()
                            }
                            listPojoTarjetasFR!!.add(pojo_card!!)
                        }
                        if (!db.metadataDAO().getById(listCards[position].idMetadata!!).name
                                .equals(nombre)
                        ) {
                            /*TODO fixmE db.putNombreListaID(nombre,
                                listCards[position].idMetadata
                            )*/
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
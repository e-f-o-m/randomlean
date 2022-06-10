package com.efom.randomlearn

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import com.efom.randomlearn.Adapters.Random.CardStackAdapter
import com.efom.randomlearn.Adapters.Random.IClickRandom
import com.efom.randomlearn.MODELS.Tarjeta
import com.efom.randomlearn.SQLITE.DBSQLite
import com.efom.randomlearn.Utiles.CONST
import com.yuyakaido.android.cardstackview.*
import java.util.*
import kotlin.math.roundToInt


class RandomActivity : AppCompatActivity() {
    var id_lista = 0
    var sizeList = 0
    var numAleatorio = 0
    var aprendidos = 0
    var sinAprender = 0
    var mostrarTodo = false
    var position = -1
    var ruta = ""

    var TVNum_RDA: TextView? = null
    var tVAprendidos_RDA: TextView? = null
    var tVObservacion_RDA: TextView? = null
    var raBrDificultad: RatingBar? = null
    private var imgActions_RA: ImageView? = null
    private var iVFavorites_RA: ImageView? = null
    private var imgOrder_RA: ImageView? = null
    private var manager: CardStackLayoutManager? = null
    private var adapter: CardStackAdapter? = null
    var numAleatorioBundle = Bundle()
    var listTarjetas: ArrayList<Tarjeta>? = null
    var DBSQLite: DBSQLite? = null
    var context: Context? = null

    //var random: Random? = null
    var cardStackView: CardStackView? = null
    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var order = 0
    private val ES_SIEMPRE_PREGUNTA = "esSiemprePregunta"
    private val CO: CONST = CONST

    var isFavorite = false
    var isRBChangeUser = false

    var isQuestion = true

    lateinit var textToSpeech: TextToSpeech

    private val TTS_PRE = "tts_pregunta"
    private val TTS_RES = "tts_respuesta"
    private var opTtsPre = 0
    private var opTtsRes = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random)
        setIdViews()
        context = applicationContext
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        editor = prefs.edit()
        order = prefs.getInt("order_list", 0)
        opTtsRes = prefs.getInt(TTS_RES, 0)
        opTtsPre = prefs.getInt(TTS_PRE, 0)

        DBSQLite = DBSQLite(context)

        textToSpeech = TextToSpeech(applicationContext) { i ->
            // if No error is found then only it will run
            if (i != TextToSpeech.ERROR) {
                if (opTtsPre == 2){
                    textToSpeech.language = Locale.ROOT
                } else if (opTtsPre == 1) {
                    textToSpeech.language = Locale.UK
                }

                if (opTtsRes == 2) {
                    textToSpeech.language = Locale.ROOT
                }else if (opTtsRes == 1) {
                    textToSpeech.language = Locale.UK
                }
            }
        }


        //tema
        if (prefs.getBoolean(getString(R.string.temaOscuro), true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        //Generar primer Aleatorio
        if (intent.extras != null) {
            ruta = intent.getStringExtra("ruta").toString()
            id_lista = intent.getIntExtra("id_lista", 1)
            editor = editor.putInt("lista_widget", id_lista)
            editor.apply()
            actionsMoveCard()
            nextTarjet()
        }

        raBrDificultad!!.onRatingBarChangeListener = OnRatingBarChangeListener { _, rating, _ ->
            if (isRBChangeUser)
                Toast.makeText(
                    applicationContext,
                    rating.roundToInt().toString(),
                    Toast.LENGTH_SHORT
                ).show()
        }

        //actions toolbar
        iVFavorites_RA!!.setOnClickListener { v: View? ->
            if (listTarjetas!![numAleatorio].tipo == CO.FAVORITO) {
                listTarjetas!![numAleatorio].tipo = CO.TEXTO
            } else {
                listTarjetas!![numAleatorio].tipo = CO.FAVORITO
            }
            changeFatorite()
        }

        imgActions_RA!!.setOnClickListener { _: View? ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Acciones").setItems(R.array.actions_random) { dialog, which ->
                when (which) {
                    0 -> editar()
                    1 -> ajustes()
                }
                dialog.dismiss()
            }
            builder.create().show()

        }

        imgOrder_RA!!.setOnClickListener { _: View? ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ordenar - Filtrar: $order")
                .setItems(R.array.actions_order) { dialog, which ->
                    when (which) {
                        0, 1, 2, 3, 4, 7, 8 -> {
                            order = which
                        }
                        5 -> {
                            editor.putBoolean(ES_SIEMPRE_PREGUNTA, true).apply()
                        }
                        6 -> {
                            editor.putBoolean(ES_SIEMPRE_PREGUNTA, false).apply()
                        }
                        9 -> {
                            mostrarTodo = !mostrarTodo
                        }
                        //0 Aleatorio
                        //1 Nombre Acendente
                        //2 Nombre Descendente
                        //3 Fecha Acendente
                        //4 Fecha Descendente
                        //5 Primero Pregunta
                        //6 Primero Respuesta
                        //7 Favoritos
                        //8 No Favoritos
                        //9 Mostrar todos
                    }
                    editor.putInt("order_list", order).apply()
                    actionsMoveCard()
                    //nextTarjet()
                    dialog.dismiss()
                }
            builder.create().show()
        }

    } //Fin OnCreat


    fun editar() {
        val intent = Intent(context, EditActivity::class.java)
        intent.putExtra("id_lista", id_lista)
        intent.putExtra(
            "id_tarjeta",
            listTarjetas!![numAleatorioBundle.getInt("numAleatorio")].id_tarjeta
        )
        intent.putExtra("ultimo", listTarjetas!![sizeList - 1].id_tarjeta)
        startActivity(intent)
    }

    fun verTodasDificultades() {
        mostrarTodo = !mostrarTodo
    }

    fun ajustes() {
        val intent = Intent(context, SettingActivity::class.java)
        intent.putExtra("id_lista", id_lista)
        startActivity(intent)
        finish()
    }

    private fun actionsMoveCard() {

        listTarjetas = DBSQLite!!.getTarjetasRandom(id_lista, order)


        if (listTarjetas!!.size == 0) {
            Toast.makeText(context, "Sin existencias", Toast.LENGTH_SHORT).show()
            order = 0
            listTarjetas = DBSQLite!!.getTarjetasRandom(id_lista, order)
        }
        sizeList = listTarjetas!!.size

        for (i in listTarjetas!!.indices) {
            numAleatorio = 0
            if (listTarjetas!![i].dificultad > 1.5) {
                sinAprender++
            } else {
                aprendidos++
            }
        }
        if (sinAprender > 0 && !mostrarTodo) {
            while (listTarjetas!![numAleatorio].dificultad < 1.5 && !mostrarTodo) {
                numAleatorio = if (numAleatorio < sizeList - 1) numAleatorio + 1 else 0
            }
        }

        numAleatorioBundle.putInt("numAleatorio", numAleatorio)
        isRBChangeUser = false
        raBrDificultad!!.rating = listTarjetas!![numAleatorio].dificultad.toFloat()
        isRBChangeUser = true
        changeFatorite()

        manager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardDragging(direction: Direction, ratio: Float) {
                //anterior, guardar para acutalizar
                val k = numAleatorioBundle.getInt("numAleatorio")
                listTarjetas!![k].dificultad = raBrDificultad!!.rating.toDouble()
                listTarjetas!![k].tipo = if (isFavorite) {
                    CO.FAVORITO
                } else {
                    CO.TEXTO
                }
                changeFatorite()
            }

            override fun onCardSwiped(direction: Direction) {
                //Log.d(TAG, "onCardSwiped: p=" + manager.getTopPosition() + " d=" + direction);
                position = manager!!.topPosition
                if (direction == Direction.Right) {
                    nextTarjet()
                }
                if (direction == Direction.Top) {
                    nextTarjet()
                }
                if (direction == Direction.Left) {
                    nextTarjet()
                }
                if (direction == Direction.Bottom) {
                    nextTarjet()
                }
                // Paginating
                if (manager!!.topPosition == adapter!!.itemCount - 1) {
                    cardStackView?.rewind()
                    //paginate();
                }
            }

            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View, position: Int) {}
            override fun onCardDisappeared(view: View, position: Int) {
                DBSQLite!!.updateTarjeta(listTarjetas!![position])
            }
        })


        manager!!.setStackFrom(StackFrom.None)
        manager!!.setVisibleCount(3)
        manager!!.setTranslationInterval(8.0f)
        manager!!.setScaleInterval(0.95f)
        manager!!.setSwipeThreshold(0.3f)
        manager!!.setMaxDegree(20.0f)
        manager!!.setDirections(Direction.FREEDOM)
        manager!!.setCanScrollHorizontal(true)
        manager!!.setSwipeableMethod(SwipeableMethod.Manual)
        manager!!.setOverlayInterpolator(LinearInterpolator())
        adapter = CardStackAdapter(listTarjetas!!, ruta, object : IClickRandom {
            override fun onItemClick(item: View) {
                when(item.id){
                    R.id.imgVTalkQuestion_RD -> talk(listTarjetas!![numAleatorio].pregunta!!)
                    R.id.imgVTalkResponse_RD -> talk(listTarjetas!![numAleatorio].respuesta!!)
                }
            }
        } )

        cardStackView?.layoutManager = manager
        cardStackView?.adapter = adapter
        cardStackView?.itemAnimator = DefaultItemAnimator()


        numAleatorioBundle.putInt("numAleatorio", numAleatorio)
        manager!!.topPosition = numAleatorio

    }

    override fun onResume() {
        super.onResume()
        actionsMoveCard()
    }

    private fun nextTarjet() {
        aprendidos = 0
        sinAprender = 0
        for (i in listTarjetas!!.indices) {
            if (listTarjetas!![i].dificultad > 1.5) {
                sinAprender++
            } else {
                aprendidos++
            }
        }

        //Validar si mostrar todos // generar aleatorio
        numAleatorio = if (numAleatorio < sizeList - 1) numAleatorio + 1 else 0
        if (sinAprender > 0 && !mostrarTodo) {
            while (listTarjetas!![numAleatorio].dificultad < 1.5 && !mostrarTodo) {
                numAleatorio = if (numAleatorio < sizeList - 1) numAleatorio + 1 else 0
            }
        }
        numAleatorioBundle.putInt("numAleatorio", numAleatorio)

        manager!!.topPosition = numAleatorio
        setTextToViews()
    }

    private fun setTextToViews() {
        tVAprendidos_RDA!!.text = "$aprendidos / ${listTarjetas!!.size}"
        TVNum_RDA!!.text = (numAleatorio).toString()
        tVObservacion_RDA!!.text = "" + listTarjetas!![numAleatorio].detalles
        changeFatorite()
        isRBChangeUser = false
        raBrDificultad!!.rating = listTarjetas!![numAleatorio].dificultad.toFloat()
        isRBChangeUser = true

        if (opTtsPre > 0 && isQuestion){
            talk(listTarjetas!![numAleatorio].pregunta!!)
        } else if (opTtsRes > 0 && !isQuestion){
            talk(listTarjetas!![numAleatorio].respuesta!!)
        }
    }
    fun talk (text:String){
        if (opTtsPre == 2 || opTtsRes == 2){
            textToSpeech.language = Locale.ROOT
        } else if (opTtsPre == 1 || opTtsRes == 1){
            textToSpeech.language = Locale.UK
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null, null);
        } else {
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    fun changeFatorite() {
        if (listTarjetas!![numAleatorio].tipo == CO.FAVORITO) {
            iVFavorites_RA!!.background = ContextCompat.getDrawable(this, R.drawable.ic_heart)
            isFavorite = true
        } else {
            iVFavorites_RA!!.background = ContextCompat.getDrawable(this, R.drawable.ic_heart_not)
            isFavorite = false
        }
    }


    private fun setIdViews() {
        //switchTodos_RDA = findViewById<View>(R.id.SwitchTodos_RDA) as Switch
        TVNum_RDA = findViewById<View>(R.id.TVNum_RDA) as TextView
        tVAprendidos_RDA = findViewById<View>(R.id.TVAprendidos_RDA) as TextView
        tVObservacion_RDA = findViewById<View>(R.id.tVObservacion_RDA) as TextView
        imgOrder_RA = findViewById<View>(R.id.imgOrder_RA) as ImageView
        imgActions_RA = findViewById<View>(R.id.imgActions_RA) as ImageView
        raBrDificultad = findViewById<View>(R.id.RaBrDificultad) as RatingBar
        cardStackView = findViewById(R.id.card_stack_view2)
        iVFavorites_RA = findViewById(R.id.iVFavorites_RA)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
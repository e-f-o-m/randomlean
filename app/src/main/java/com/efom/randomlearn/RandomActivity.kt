package com.efom.randomlearn

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.efom.randomlearn.adapters.random.CardStackAdapter
import com.efom.randomlearn.adapters.random.IClickRandom
import com.efom.randomlearn.controllers.RandomController
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.database.MyDB
import com.efom.randomlearn.utils.CONSTS
import com.efom.randomlearn.utils.DT
import com.lorentzos.flingswipe.SwipeFlingAdapterView.onFlingListener
import java.util.*
import kotlin.math.roundToInt
import com.efom.randomlearn.databinding.ActivityRandomBinding
import com.efom.randomlearn.models.Metadata
import kotlin.collections.ArrayList


class RandomActivity : AppCompatActivity() {
    private lateinit var b: ActivityRandomBinding

    private var idMetadata = 0
    private var sizeList = 0
    private var position = 0
    private var aprendidos = 0
    private var sinAprender = 0
    private var mostrarTodo = false
    private var ruta = ""

    lateinit var cardStackView: CardStackAdapter

    private val numAleatorioBundle = Bundle()

    private lateinit var targets: ArrayList<Card>
    private lateinit var db: MyDB
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    var orderPref = 0
    private val ES_SIEMPRE_PREGUNTA = "esSiemprePregunta"

    private var isFavorite = false
    private var isRBChangeUser = false
    private var isQuestion = true

    lateinit var textToSpeech: TextToSpeech

    private var opTtsQuestionPref = 0
    private var opTtsResponsePref = 0
    private var spaceLearnSizePref = 0
    private var mTheme = 0
    var type = -1
    lateinit var metadata: Metadata

    lateinit var ramdomCtlr: RandomController

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRandomBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = MyDB.getDB(applicationContext)!!
        ramdomCtlr = RandomController(applicationContext)
        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        editor = prefs.edit()

        //Generar primer Aleatorio
        if (intent.extras != null) {
            if (prefs.getBoolean(CONSTS.IS_DARK, true)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                mTheme = 0
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                mTheme = 1
            }

            opTtsResponsePref = prefs.getInt(CONSTS.TTS_RES, 0)
            opTtsQuestionPref = prefs.getInt(CONSTS.TTS_QUE, 0)
            spaceLearnSizePref = prefs.getInt(CONSTS.N_SPACE_LEARN, 0)


            ruta = intent.getStringExtra(CONSTS.PATH).toString()
            idMetadata = intent.getIntExtra(CONSTS.ID_LISTA, 1)
            editor = editor.putInt(CONSTS.LISTA_WIDGET, idMetadata)
            numAleatorioBundle.putInt(CONSTS.NUMALEATORIO, position)
            editor.apply()
            metadata = db.metadataDAO().getById(idMetadata)
            orderPref = if(metadata.order == null) CONSTS.RANDOM else metadata.order!!
        }

        textToSpeech = TextToSpeech(applicationContext) { i ->
            if (i != TextToSpeech.ERROR) {
                if (opTtsQuestionPref == 2) {
                    textToSpeech.language = Locale.ROOT
                } else if (opTtsQuestionPref == 1) {
                    textToSpeech.language = Locale.US
                }

                if (opTtsResponsePref == 2) {
                    textToSpeech.language = Locale.ROOT
                } else if (opTtsResponsePref == 1) {
                    textToSpeech.language = Locale.US
                }
            }
        }
        
        actions()
        startTargets()
    }

    private fun actions(){
        b.RaBrDificultad.onRatingBarChangeListener = OnRatingBarChangeListener { _, rating, _ ->
            if (isRBChangeUser)
                Toast.makeText(applicationContext, rating.roundToInt().toString(), Toast.LENGTH_SHORT).show()
        }

        //actions toolbar
        b.iVFavoritesRA.setOnClickListener {
            targets[position].favorite = !isFavorite
            changeFavorite()
        }

        b.imgActionsRA.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Acciones").setItems(R.array.actions_random) { dialog, which ->
                when (which) {
                    0 -> goSettings()
                    1 -> editor.putBoolean(ES_SIEMPRE_PREGUNTA, true).apply()
                    2 -> editor.putBoolean(ES_SIEMPRE_PREGUNTA, false).apply()
                    3 -> mostrarTodo = !mostrarTodo
                }
                dialog.dismiss()
            }
            builder.create().show()
        }

        b.imgOrderRA.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ordenar - Filtrar: $orderPref")
                .setItems(R.array.actions_order) { dialog, which ->
                    //1 Aleatorio
                    //2 Por Nombre
                    //3 Favoritos
                    //4 No Favoritos
                    //5 Aprendizaje Espaciado
                    //6 En orden
                    Log.i(" 📌 157", "RandomActivity🥚actions🍄valido order: " + which)
                    orderPref = which
                    editor.putInt("order_list", orderPref).apply()
                    startTargets()
                    dialog.dismiss()
                }
            builder.create().show()
        }

        b.imgEditRA.setOnClickListener {
            goEdit()
        }

        b.imgRecordRA.setOnClickListener {
            orderPref = if(orderPref == CONSTS.RECORD){
                CONSTS.SPACELEARN_OPTION
            }else{
                CONSTS.RECORD
            }
            editor.putInt("order_list", orderPref).apply()

            startTargets()
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
    }

    private fun startTargets() {
        targets = getData(orderPref)

        aprendidos = 0
        for (i in targets.indices) {
            if (targets[i].difficulty!! >= 0.5) {
                sinAprender++
            } else {
                aprendidos++
            }
        }

        sizeList = targets.size
        targets.add(Card())
        swipeSetup()
        setTextToViews()
    }

    override fun onResume() {
        super.onResume()

        if(type == CONSTS.EDIT_CARD && targets.isNotEmpty()){
            type = -1
            targets[0] = db.cardsDAO().getById(targets[0].idCard!!)
            if(targets[0] == null){
                startTargets()
            }
            cardStackView.update(targets)
        }

        setTextToViews()
    }

    @SuppressLint("ResourceType")
    private fun getData(_order: Int): ArrayList<Card> {
        metadata.order = _order
        db.metadataDAO().update(metadata)
        if (_order == CONSTS.SPACELEARN_OPTION) {
            if (spaceLearnSizePref == 0) {
                goSettings()
                finish()
            }
            b.imgRecordRA.visibility = View.VISIBLE
            return ramdomCtlr.getSpaceLearn(idMetadata, spaceLearnSizePref)
        }

        if(_order == CONSTS.RECORD){
            b.imgRecordRA.visibility = View.VISIBLE
            return  ramdomCtlr.getCardsByRecord(idMetadata, DT.startDay())
        }

        b.imgRecordRA.visibility = View.GONE
        return ramdomCtlr.getCardByOrder(idMetadata, _order)
    }

    @SuppressLint("ResourceType")
    private fun swipeSetup() {
        removeCompleted()

        cardStackView = CardStackAdapter(applicationContext, targets, ruta,mTheme, object : IClickRandom {
            override fun onItemClick(item: View) {
                when (item.id) {
                    R.id.imgVTalkQuestion_RD -> talk(targets[position].question!!)
                    R.id.imgVTalkResponse_RD -> talk(targets[position].answer!!)
                }

            }
        })

        b.cardStackView.adapter = cardStackView

        b.cardStackView.setFlingListener(object : onFlingListener {
            override fun removeFirstObjectInAdapter() {
                buildSetDB()
                targets.removeAt(0)
                isQuestion = true
                nextTarget()
                cardStackView.update(targets)
            }

            override fun onLeftCardExit(dataObject: Any) {
                b.cardStackView.topCardListener.selectLeft()
            }

            override fun onRightCardExit(dataObject: Any) {
                b.cardStackView.topCardListener.selectRight()
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                //cardStackView.update()
            }

            override fun onScroll(p0: Float) {
            }
        })

        b.cardStackView.setOnItemClickListener { itemPosition, dataObject ->
            val view = b.cardStackView.selectedView
            val RLTarjetaPregunta_RD = view.findViewById(R.id.RLTarjetaPregunta_RD) as CardView
            val RLTarjetaRespuesta_RD = view.findViewById(R.id.RLTarjetaRespuesta_RD) as CardView


            rotateAnimation(RLTarjetaRespuesta_RD, RLTarjetaPregunta_RD)
            isQuestion = !isQuestion
        }
    }

    private fun nextTarget() {

        if (!mostrarTodo) {
            removeCompleted()
        }
        if (targets.size == 1) {
            startTargets()
            return
        }
        setTextToViews()
    }

    private fun removeCompleted(){
        if (!mostrarTodo) {
            //Desde la posición siguiente hacia adalante
            while (targets.size > 1) {
                if (targets[position].difficulty!! < 1) {
                    targets.removeAt(position)
                } else {
                    break
                }
            }
        }
    }

    private fun buildSetDB() {
        if (targets.size > 0) {
            val k = numAleatorioBundle.getInt("numAleatorio")
            if(b.RaBrDificultad.rating.toDouble() != targets[k].difficulty){
                if(b.RaBrDificultad.rating.toDouble() < 1){
                    aprendidos++
                }
            }
            targets[k].difficulty = b.RaBrDificultad.rating.toDouble()


            targets[k].favorite = isFavorite
            db.cardsDAO().update(targets[k])
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextToViews() {
        b.TVAprendidosRDA.text = "$aprendidos / ${sizeList}"
        b.TVNumRDA.text = (sizeList - targets.size + 2).toString()
        isRBChangeUser = false

        if (targets.size > 1) {
            changeFavorite()
            b.RaBrDificultad.rating = targets[position].difficulty!!.toFloat()
            isRBChangeUser = true
            if (opTtsQuestionPref > 0 && isQuestion) {
                talk(targets[position].question!!)
            } else if (opTtsResponsePref > 0 && !isQuestion) {
                talk(targets[position].answer!!)
            }
        }
    }

    private fun rotateAnimation(back: View, front: View) {
        val scale = applicationContext.resources.displayMetrics.density
        front.cameraDistance = 8000 * scale
        back.cameraDistance = 8000 * scale

        // Now we will set the front animation
        val front_animation = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.front_animator
        ) as AnimatorSet
        val back_animation = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.back_animator
        ) as AnimatorSet

        if (isQuestion) {
            front_animation.setTarget(front)
            back_animation.setTarget(back)
            front_animation.start()
            back_animation.start()
        } else {
            front_animation.setTarget(back)
            back_animation.setTarget(front)
            back_animation.start()
            front_animation.start()
        }
    }

    private fun talk(text: String) {
        if (opTtsQuestionPref == 2 || opTtsResponsePref == 2) {
            textToSpeech.language = Locale.ROOT
        } else if (opTtsQuestionPref == 1 || opTtsResponsePref == 1) {
            textToSpeech.language = Locale.US
        }

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun changeFavorite() {
        if (targets.size > 1) {
            if (targets[position].favorite!!) {
                b.iVFavoritesRA.background = ContextCompat.getDrawable(this, R.drawable.ic_heart)
                isFavorite = true
            } else {
                b.iVFavoritesRA.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_heart_not)
                isFavorite = false
            }
        }
    }

    private fun goEdit() {
        type = CONSTS.EDIT_CARD
        val intent = Intent(applicationContext, EditActivity::class.java)
        intent.putExtra("id_lista", idMetadata)
        intent.putExtra("id_tarjeta", targets[0].idCard)
        intent.putExtra("ultimo", targets[targets.size-1].idCard)
        intent.putExtra(getString(R.string.type), CONSTS.EDIT_CARD)
        startActivity(intent)
    }

    private fun goSettings() {
        val intent = Intent(applicationContext, SettingActivity::class.java)
        intent.putExtra("id_lista", idMetadata)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
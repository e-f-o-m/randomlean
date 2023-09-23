package com.efom.randomlearn.adapters.random

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.R
import java.io.File
import java.util.*


class CardStackAdapter(
    val context: Context,
    var items: ArrayList<Card>,
    var ruta: String,
    val theme: Int,
    private var listener: IClickRandom
) :
    BaseAdapter() {

    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    private val ES_CONGELAR_CARA = "congelarCara"
    private val ES_SIEMPRE_PREGUNTA = "esSiemprePregunta"
    var esPregunta = true


    @SuppressLint("ResourceAsColor")
    inner class ViewHolder(itemView: View) {
        var tVBarRespuesta_RDA: TextView
        var tVBarPregunta_RDA: TextView
        var tVPregunta_RD: TextView
        var tVRespuesta_RD: TextView
        var rLTarjetaPregunta_RD: CardView
        var rLTarjetaRespuesta_RD: CardView
        var imgVPregunta_RD: ImageView
        var imgVRespuesta_RD: ImageView
        var imgVTalkQuestion_RD: ImageView
        var imgVTalkResponse_RD: ImageView
        var rlResponse: RelativeLayout
        var rlQuestion: RelativeLayout
        var id_lista: Int
        private val prefs: SharedPreferences = context.getSharedPreferences("mPreferences", Context.MODE_PRIVATE)

        init {
            id_lista = prefs.getInt("lista_widget", -1)
            rLTarjetaPregunta_RD =
                itemView.findViewById(R.id.RLTarjetaPregunta_RD)
            rLTarjetaRespuesta_RD =
                itemView.findViewById(R.id.RLTarjetaRespuesta_RD)
            tVBarRespuesta_RDA = itemView.findViewById(R.id.tVBarRespuesta_RDA)
            tVBarPregunta_RDA = itemView.findViewById(R.id.tVBarPregunta_RDA)
            tVPregunta_RD = itemView.findViewById(R.id.tVPregunta_RD)
            tVRespuesta_RD = itemView.findViewById(R.id.tVRespuesta_RD)
            imgVPregunta_RD = itemView.findViewById(R.id.imgVPregunta_RD)
            imgVRespuesta_RD = itemView.findViewById(R.id.imgVRespuesta_RD)
            imgVTalkQuestion_RD = itemView.findViewById(R.id.imgVTalkQuestion_RD)
            imgVTalkResponse_RD = itemView.findViewById(R.id.imgVTalkResponse_RD)

            rlResponse = itemView.findViewById(R.id.rlResponse)
            rlQuestion = itemView.findViewById(R.id.rlQuestion)
        }


        //Oculta y selecciona el view compatible con el contenido
        private fun typeVisivilityView(pregunta: View, respuesta: View) {
            imgVRespuesta_RD.visibility = View.GONE
            tVRespuesta_RD.visibility = View.GONE
            imgVPregunta_RD.visibility = View.GONE
            tVPregunta_RD.visibility = View.GONE
            pregunta.visibility = View.VISIBLE
            respuesta.visibility = View.VISIBLE
        }

        fun setData(numAleatorio: Int) {
            try {
                //FontSize
                tVPregunta_RD.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    prefs.getInt("fontsize", 16).toFloat()
                )
                tVRespuesta_RD.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    prefs.getInt("fontsize", 16).toFloat()
                )
                View.getDefaultSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("fontsize", 16))
                View.getDefaultSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("fontsize", 16))

                //Validar views a mostrar
                var preguntaIsImg = false
                var respuestaIsImg = false
                var preguntaIsMath = false
                var respuestaIsMath = false

                if (items[numAleatorio].question!!.length > 4) {
                    if (items[numAleatorio].question!!.substring(items[numAleatorio].question!!.length - 4)
                            .equals(".png", ignoreCase = true)
                        || items[numAleatorio].question!!.substring(items[numAleatorio].question!!.length - 4)
                            .equals(".jpg", ignoreCase = true)
                        || items[numAleatorio].question!!.substring(items[numAleatorio].question!!.length - 4)
                            .equals("jpeg", ignoreCase = true)
                    ) {
                        preguntaIsImg = true
                    }
                    preguntaIsMath =
                        items[numAleatorio].question!!.substring(items[numAleatorio].question!!.length - 2)
                            .equals("$$", ignoreCase = true)
                }
                if (items[numAleatorio].answer!!.length > 4) {
                    if (items[numAleatorio].answer!!.substring(items[numAleatorio].answer!!.length - 4)
                            .equals(".png", ignoreCase = true)
                        || items[numAleatorio].answer!!.substring(items[numAleatorio].answer!!.length - 4)
                            .equals(".jpg", ignoreCase = true)
                        || items[numAleatorio].answer!!.substring(items[numAleatorio].answer!!.length - 4)
                            .equals("jpeg", ignoreCase = true)
                    ) {
                        respuestaIsImg = true
                    }
                    respuestaIsMath =
                        items[numAleatorio].answer!!.substring(items[numAleatorio].answer!!.length - 2)
                            .equals("$$", ignoreCase = true)
                }

                //Organizar Views, seleccionar views
                if (preguntaIsImg && !respuestaIsMath && !respuestaIsImg) {
                    typeVisivilityView(imgVPregunta_RD, tVRespuesta_RD)
                    val imageUriPregunta =
                        Uri.fromFile(File(ruta + "/" + items[numAleatorio].question))
                    Glide.with(context).load(imageUriPregunta).into(imgVPregunta_RD)
                    tVRespuesta_RD.text = items[numAleatorio].answer!!.replace(
                        "\\n",
                        Objects.requireNonNull(System.getProperty("line.separator"))
                    )
                    // imagen e imagen
                } else if (preguntaIsImg && respuestaIsImg) {
                    typeVisivilityView(imgVPregunta_RD, imgVRespuesta_RD)
                    val imageUriPregunta =
                        Uri.fromFile(File(ruta + "/" + items[numAleatorio].question))
                    Glide.with(context).load(imageUriPregunta).into(imgVPregunta_RD)
                    val imageUriRespuesta =
                        Uri.fromFile(File(ruta + "/" + items[numAleatorio].answer))
                    Glide.with(context).load(imageUriRespuesta).into(imgVRespuesta_RD)
                    // imagen y formula
                } else if (!preguntaIsMath && !respuestaIsMath && !respuestaIsImg && !preguntaIsImg) {
                    typeVisivilityView(tVPregunta_RD, tVRespuesta_RD)
                    tVPregunta_RD.text = items[numAleatorio].question!!.replace(
                        "\\n",
                        Objects.requireNonNull(System.getProperty("line.separator"))
                    )
                    tVRespuesta_RD.text = items[numAleatorio].answer!!.replace(
                        "\\n",
                        Objects.requireNonNull(System.getProperty("line.separator"))
                    )
                } else if (!preguntaIsMath && respuestaIsImg && !preguntaIsImg) {
                    typeVisivilityView(tVPregunta_RD, imgVRespuesta_RD)
                    tVPregunta_RD.text = items[numAleatorio].question!!.replace(
                        "\\n",
                        Objects.requireNonNull(System.getProperty("line.separator"))
                    )
                    val imageUriRespuesta =
                        Uri.fromFile(File(ruta + "/" + items[numAleatorio].answer))
                    Glide.with(context).load(imageUriRespuesta).into(imgVRespuesta_RD)
                }
            } catch (e: Exception) {
                Log.d("78 RandomActivity.kt", "onCreate: " + e.message)
            }
        }
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Card {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val vh: ViewHolder
        val v: View = this.mInflater.inflate(R.layout.card_random, viewGroup, false)
        vh = ViewHolder(v)


        vh.imgVTalkQuestion_RD.setOnClickListener {
            listener.onItemClick(it)
        }
        vh.imgVTalkResponse_RD.setOnClickListener {
            listener.onItemClick(it)
        }

        vh.setData(position)

        if(theme == 1){
            vh.rlResponse.setBackgroundColor(ContextCompat.getColor(context,
                R.color.card_light))
            vh.rlQuestion.setBackgroundColor(ContextCompat.getColor(context,
                R.color.card_light))
            vh.tVPregunta_RD.setTextColor(ContextCompat.getColor(context,
                R.color.tx_black_2))
            vh.tVRespuesta_RD.setTextColor(ContextCompat.getColor(context,
                R.color.tx_black_2))
            vh.tVBarPregunta_RDA.setTextColor(ContextCompat.getColor(context,
                R.color.tx_black_2))
            vh.tVBarRespuesta_RDA.setTextColor(ContextCompat.getColor(context,
                R.color.tx_black_2))
        }else{
            vh.rlResponse.setBackgroundColor(ContextCompat.getColor(context,
                R.color.card_dark))
            vh.rlQuestion.setBackgroundColor(ContextCompat.getColor(context,
                R.color.card_dark))
            vh.tVPregunta_RD.setTextColor(ContextCompat.getColor(context,
                R.color.tx_witte_1))
            vh.tVRespuesta_RD.setTextColor(ContextCompat.getColor(context,
                R.color.tx_witte_1))
            vh.tVBarPregunta_RDA.setTextColor(ContextCompat.getColor(context,
                R.color.tx_witte_1))
            vh.tVBarRespuesta_RDA.setTextColor(ContextCompat.getColor(context,
                R.color.tx_witte_1))
        }

        return v
    }

    fun update(items: ArrayList<Card>) {
        this.items = items
        notifyDataSetChanged()
    }

}
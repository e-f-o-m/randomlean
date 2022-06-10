package com.efom.randomlearn.Adapters.Random

import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.MODELS.Tarjeta
import android.view.ViewGroup
import android.view.LayoutInflater
import com.efom.randomlearn.R
import android.widget.TextView
import android.content.Context
import android.widget.RelativeLayout
import io.github.kexanie.library.MathView
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.File
import java.lang.Exception
import java.util.*

class CardStackAdapter(listPojoTarjetas: List<Tarjeta>, ruta: String, listener: IClickRandom ) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    private  val ES_CONGELAR_CARA = "congelarCara"
    private  val ES_SIEMPRE_PREGUNTA = "esSiemprePregunta"
    var items: List<Tarjeta>
    var esPregunta = true
    var ruta = "/"

    private var listener: IClickRandom

    init {
        this.ruta = ruta
        this.items = listPojoTarjetas
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_card, parent, false)
        return ViewHolder(view, view.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.voltearCaraTarjeta(false)
        holder.setData(position)
        holder.imgVTalkQuestion_RD.setOnClickListener {
            listener.onItemClick(it)
        }
        holder.imgVTalkResponse_RD.setOnClickListener {
            listener.onItemClick(it)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        var tVBarRespuesta_RDA: TextView
        var tVBarPregunta_RDA: TextView
        var tVPregunta_RD: TextView
        var tVRespuesta_RD: TextView
        var rLTarjetaPregunta_RD: RelativeLayout
        var rLTarjetaRespuesta_RD: RelativeLayout
        var imgVPregunta_RD: ImageView
        var imgVRespuesta_RD: ImageView
        var imgVTalkQuestion_RD: ImageView
        var imgVTalkResponse_RD: ImageView
        var mVPregunta_RD: MathView
        var mVRespuesta_RD: MathView
        var id_lista: Int
        private val prefs: SharedPreferences = context.getSharedPreferences("mPreferences", Context.MODE_PRIVATE)
        private val editor: SharedPreferences.Editor = prefs.edit()


        //Oculta y selecciona el view compatible con el contenido
        private fun typeVisivilityView(pregunta: View, respuesta: View) {
            imgVRespuesta_RD.visibility = View.GONE
            mVRespuesta_RD.visibility = View.GONE
            tVRespuesta_RD.visibility = View.GONE
            imgVPregunta_RD.visibility = View.GONE
            mVPregunta_RD.visibility = View.GONE
            tVPregunta_RD.visibility = View.GONE
            pregunta.visibility = View.VISIBLE
            respuesta.visibility = View.VISIBLE
        }

        fun setData(numAleatorio: Int) {
            //FIXME: BORAR
            Log.d("78 CardStackAdapter.kt", "setData: nez")
        
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

            //Enviar color
            if (items[numAleatorio].color !== "" && items[numAleatorio].color!!.length >= 6) {
                tVBarPregunta_RDA.setBackgroundColor(Color.parseColor(items[numAleatorio].color))
                tVBarRespuesta_RDA.setBackgroundColor(Color.parseColor(items[numAleatorio].color))
            }

            //Envair observación
            //tVObservacion_RDA.setText("" + listPojoTarjetas.get(numAleatorio).getObservacion());

            //Validar views a mostrar
            var preguntaIsImg = false
            var respuestaIsImg = false
            var preguntaIsMath = false
            var respuestaIsMath = false
            if (items[numAleatorio].pregunta!!.length > 4) {
                if (items[numAleatorio].pregunta!!.substring(items[numAleatorio].pregunta!!.length - 4)
                        .equals(".png", ignoreCase = true)
                    || items[numAleatorio].pregunta!!.substring(items[numAleatorio].pregunta!!.length - 4)
                        .equals(".jpg", ignoreCase = true)
                    || items[numAleatorio].pregunta!!.substring(items[numAleatorio].pregunta!!.length - 4)
                        .equals("jpeg", ignoreCase = true)
                ) {
                    preguntaIsImg = true
                }
                preguntaIsMath =
                    items[numAleatorio].pregunta!!.substring(items[numAleatorio].pregunta!!.length - 2)
                        .equals("$$", ignoreCase = true)
            }
            if (items[numAleatorio].respuesta!!.length > 4) {
                if (items[numAleatorio].respuesta!!.substring(items[numAleatorio].respuesta!!.length - 4)
                        .equals(".png", ignoreCase = true)
                    || items[numAleatorio].respuesta!!.substring(items[numAleatorio].respuesta!!.length - 4)
                        .equals(".jpg", ignoreCase = true)
                    || items[numAleatorio].respuesta!!.substring(items[numAleatorio].respuesta!!.length - 4)
                        .equals("jpeg", ignoreCase = true)
                ) {
                    respuestaIsImg = true
                }
                respuestaIsMath =
                    items[numAleatorio].respuesta!!.substring(items[numAleatorio].respuesta!!.length - 2)
                        .equals("$$", ignoreCase = true)
            }


            //Organizar Views, seleccionar views
            if (preguntaIsMath && !respuestaIsMath && !respuestaIsImg) {
                typeVisivilityView(mVPregunta_RD, tVRespuesta_RD)
                mVPregunta_RD.text = items[numAleatorio].pregunta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                tVRespuesta_RD.text = items[numAleatorio].respuesta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
            } else if (preguntaIsMath && respuestaIsImg) {
                typeVisivilityView(mVPregunta_RD, imgVRespuesta_RD)
                mVPregunta_RD.text = items[numAleatorio].pregunta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                val imageUriRespuesta =
                    Uri.fromFile(File(ruta + "/" + items[numAleatorio].respuesta))
                Glide.with(context).load(imageUriRespuesta).into(imgVRespuesta_RD)
            } else if (preguntaIsMath && respuestaIsMath) {
                typeVisivilityView(mVPregunta_RD, mVRespuesta_RD)
                mVPregunta_RD.text = items[numAleatorio].pregunta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                mVRespuesta_RD.text = items[numAleatorio].respuesta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                /** LOGICA MOSTRAR IMAGENES  */
                // imagen y texto
            } else if (preguntaIsImg && !respuestaIsMath && !respuestaIsImg) {
                typeVisivilityView(imgVPregunta_RD, tVRespuesta_RD)
                val imageUriPregunta = Uri.fromFile(File(ruta + "/" + items[numAleatorio].pregunta))
                Glide.with(context).load(imageUriPregunta).into(imgVPregunta_RD)
                tVRespuesta_RD.text = items[numAleatorio].respuesta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                // imagen e imagen
            } else if (preguntaIsImg && respuestaIsImg) {
                typeVisivilityView(imgVPregunta_RD, imgVRespuesta_RD)
                val imageUriPregunta = Uri.fromFile(File(ruta + "/" + items[numAleatorio].pregunta))
                Glide.with(context).load(imageUriPregunta).into(imgVPregunta_RD)
                val imageUriRespuesta =
                    Uri.fromFile(File(ruta + "/" + items[numAleatorio].respuesta))
                Glide.with(context).load(imageUriRespuesta).into(imgVRespuesta_RD)
                // imagen y formula
            } else if (preguntaIsImg && respuestaIsMath) {
                typeVisivilityView(imgVPregunta_RD, mVRespuesta_RD)
                val imageUriPregunta = Uri.fromFile(File(ruta + "/" + items[numAleatorio].pregunta))
                Glide.with(context).load(imageUriPregunta).into(imgVPregunta_RD)
                mVRespuesta_RD.text = items[numAleatorio].respuesta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
            } else if (!preguntaIsMath && !respuestaIsMath && !respuestaIsImg && !preguntaIsImg) {
                typeVisivilityView(tVPregunta_RD, tVRespuesta_RD)
                tVPregunta_RD.text = items[numAleatorio].pregunta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                tVRespuesta_RD.text = items[numAleatorio].respuesta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
            } else if (!preguntaIsMath && respuestaIsImg && !preguntaIsImg) {
                typeVisivilityView(tVPregunta_RD, imgVRespuesta_RD)
                tVPregunta_RD.text = items[numAleatorio].pregunta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                val imageUriRespuesta =
                    Uri.fromFile(File(ruta + "/" + items[numAleatorio].respuesta))
                Glide.with(context).load(imageUriRespuesta).into(imgVRespuesta_RD)
            } else if (!preguntaIsMath && respuestaIsMath && !preguntaIsImg) {
                typeVisivilityView(tVPregunta_RD, mVRespuesta_RD)
                tVPregunta_RD.text = items[numAleatorio].pregunta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
                mVRespuesta_RD.text = items[numAleatorio].respuesta!!.replace(
                    "\\n",
                    Objects.requireNonNull(System.getProperty("line.separator"))
                )
            }
            }catch (e: Exception){
                Log.d("78 RandomActivity.kt", "onCreate: "+e.message)
            }
        } //fin Generar Aleatorio


        //Oculta y muestra el view de respuesta o pregunta segun su ajuste
        fun voltearCaraTarjeta(esClick: Boolean) {
            if (esClick) {
                if (rLTarjetaPregunta_RD.visibility == View.VISIBLE) {
                    rLTarjetaPregunta_RD.visibility = View.GONE
                    rLTarjetaRespuesta_RD.visibility = View.VISIBLE
                } else {
                    rLTarjetaPregunta_RD.visibility = View.VISIBLE
                    rLTarjetaRespuesta_RD.visibility = View.GONE
                }
                esPregunta = !esPregunta
            } else if (prefs.getBoolean(ES_CONGELAR_CARA, true)) {
                if (esPregunta) {
                    rLTarjetaPregunta_RD.visibility = View.VISIBLE
                    rLTarjetaRespuesta_RD.visibility = View.GONE
                } else {
                    rLTarjetaPregunta_RD.visibility = View.GONE
                    rLTarjetaRespuesta_RD.visibility = View.VISIBLE
                }
            } else {
                if (prefs.getBoolean(ES_SIEMPRE_PREGUNTA, true)) {
                    rLTarjetaPregunta_RD.visibility = View.VISIBLE
                    rLTarjetaRespuesta_RD.visibility = View.GONE
                } else {
                    rLTarjetaPregunta_RD.visibility = View.GONE
                    rLTarjetaRespuesta_RD.visibility = View.VISIBLE
                }
            }
        }

        init {
            id_lista = prefs.getInt("lista_widget", -1)
            rLTarjetaPregunta_RD =
                itemView.findViewById<View>(R.id.RLTarjetaPregunta_RD) as RelativeLayout
            rLTarjetaRespuesta_RD =
                itemView.findViewById<View>(R.id.RLTarjetaRespuesta_RD) as RelativeLayout
            mVPregunta_RD = itemView.findViewById<View>(R.id.mVPregunta_RD) as MathView
            mVRespuesta_RD = itemView.findViewById<View>(R.id.mVRespuesta_RD) as MathView
            tVBarRespuesta_RDA = itemView.findViewById<View>(R.id.tVBarRespuesta_RDA) as TextView
            tVBarPregunta_RDA = itemView.findViewById<View>(R.id.tVBarPregunta_RDA) as TextView
            tVPregunta_RD = itemView.findViewById<View>(R.id.tVPregunta_RD) as TextView
            tVRespuesta_RD = itemView.findViewById<View>(R.id.tVRespuesta_RD) as TextView
            //tVObservacion_RDA    = (TextView)  itemView.findViewById(R.id.tVObservacion_RDA  );
            imgVPregunta_RD = itemView.findViewById<View>(R.id.imgVPregunta_RD) as ImageView
            imgVRespuesta_RD = itemView.findViewById<View>(R.id.imgVRespuesta_RD) as ImageView
            imgVTalkQuestion_RD = itemView.findViewById<View>(R.id.imgVTalkQuestion_RD) as ImageView
            imgVTalkResponse_RD = itemView.findViewById<View>(R.id.imgVTalkResponse_RD) as ImageView


            //Pregunta: Click, Deslizar Izquierda y/o Derecha
            rLTarjetaPregunta_RD.setOnClickListener { voltearCaraTarjeta(true) }
            //Respuesta: Click, Deslizar Izquierda y/o Derecha
            rLTarjetaRespuesta_RD.setOnClickListener { voltearCaraTarjeta(true) }
        }
    }
}
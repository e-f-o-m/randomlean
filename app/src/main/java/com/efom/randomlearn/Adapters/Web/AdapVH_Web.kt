package com.efom.randomlearn.Adapters.Web

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.Adapters.Main.RecyVH_Main
import com.efom.randomlearn.MODELS.Tarjeta
import com.efom.randomlearn.Utiles.CallBackAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import com.efom.randomlearn.R
import android.content.Context
import android.view.View
import java.lang.RuntimeException
import java.util.ArrayList

class AdapVH_Web(private val c: Context, activity: Activity) : RecyclerView.Adapter<RecyVH_Main>() {
    private val listMensaje: MutableList<Tarjeta> = ArrayList()
    var recyVH_historial: ArrayList<RecyVH_Main>
    var activity: Activity
    private var mListener: CallBackAdapter? = null
    var nombre = ""

    //Insertar iten notificar los cambios en el adaptador
    fun addLista(_Tarjeta: Tarjeta) {
        listMensaje.add(_Tarjeta)
        notifyItemInserted(listMensaje.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyVH_Main {
        val view_grupo = LayoutInflater.from(c).inflate(R.layout.card_main, parent, false)
        return RecyVH_Main(view_grupo)
    }

    override fun onBindViewHolder(holder: RecyVH_Main, position: Int) {
        holder.gettVItem_CM().text = (position + 1).toString()
        //fixme: listMensaje.get(position).getNombre_lista()
        holder.tVNombre_CM.text = nombre
        holder.gettVTAprendidos_CM().text = "APRENDIDOS: " + 0
        holder.gettVTTotal_CM().text = "ESTUDIANDO: " + 0 + " / " + 0
        holder.imgBtn_edit.visibility = View.GONE
        holder.imgBtn_play.setOnClickListener { mListener!!.intrfClick(position) }
        recyVH_historial.add(holder)
    }

    override fun getItemCount(): Int {
        return listMensaje.size
    }

    init {
        recyVH_historial = ArrayList()
        this.activity = activity
        mListener = if (c is CallBackAdapter) {
            c
        } else {
            throw RuntimeException("$c Error con callbackAdapter")
        }
    }
}
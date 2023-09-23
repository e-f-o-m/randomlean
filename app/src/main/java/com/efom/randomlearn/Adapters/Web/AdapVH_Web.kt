package com.efom.randomlearn.adapters.web

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.adapters.main.RecyVH_Main
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.R
import com.efom.randomlearn.adapters.CallBackAdapter

class AdapVH_Web(private val c: Context, activity: Activity) : RecyclerView.Adapter<RecyVH_Main>() {
    private val listMensaje: MutableList<Card> = ArrayList()
    var recyVH_historial: ArrayList<RecyVH_Main>
    var activity: Activity
    private var mListener: CallBackAdapter? = null
    var nombre = ""

    //Insertar iten notificar los cambios en el adaptador
    fun addLista(_Card: Card) {
        listMensaje.add(_Card)
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
        holder.framBtn_play.setOnClickListener { mListener!!.intrfClick(position) }
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
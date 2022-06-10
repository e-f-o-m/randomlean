package com.efom.randomlearn.Adapters.Main

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.efom.randomlearn.R
import com.efom.randomlearn.MODELS.Lista
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.efom.randomlearn.RandomActivity
import com.efom.randomlearn.ListActivity
import java.util.ArrayList

class AdapVH_Main(private val c: Context, activity: Activity) :
    RecyclerView.Adapter<RecyVH_Main>() {
    private val listMensaje: MutableList<Lista> = ArrayList()

    var recyVH_historial: ArrayList<RecyVH_Main>
    var listContador: ArrayList<IntArray>
    var activity: Activity

    //Insertar iten notificar los cambios en el adaptador
    fun addLista(_Tarjeta: Lista, contador: IntArray) {
        listMensaje.add(_Tarjeta)
        listContador.add(contador)
        notifyItemInserted(listMensaje.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyVH_Main {
        val view_grupo = LayoutInflater.from(c).inflate(R.layout.card_main, parent, false)
        return RecyVH_Main(view_grupo)
    }

    override fun onBindViewHolder(
        holder: RecyVH_Main,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.gettVItem_CM().text = (position + 1).toString()
        holder.tVNombre_CM.text = listMensaje[position].nombre
        holder.gettVTAprendidos_CM().text = "APRENDIDOS: " + listContador[position][0]
        holder.gettVTTotal_CM().text =
            "ESTUDIANDO: " + (listContador[position][1] - listContador[position][0]) + " / " + listContador[position][1]
        holder.imgBtn_edit.setOnClickListener {
            val intent = Intent(c.applicationContext, ListActivity::class.java)
            intent.putExtra("id_lista", listMensaje[position].id_lista)
            intent.putExtra("nombre_lista", listMensaje[position].nombre)
            c.startActivity(intent)
            activity.finish()
        }
        holder.imgBtn_play.setOnClickListener {
            if (listContador[position][1] != 0 || listContador[position][0] != 0) {
                val intent = Intent(c.applicationContext, RandomActivity::class.java)
                intent.putExtra("id_lista", listMensaje[position].id_lista)
                intent.putExtra("ruta", listMensaje[position].ruta)
                c.startActivity(intent)
                activity.finish()
            }
        }
        recyVH_historial.add(holder)
    }

    override fun getItemCount(): Int {
        return listMensaje.size
    }

    init {
        recyVH_historial = ArrayList()
        listContador = ArrayList()
        this.activity = activity
    }
}
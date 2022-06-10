package com.efom.randomlearn.Adapters.Lista

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.MODELS.Tarjeta
import android.view.ViewGroup
import android.view.LayoutInflater
import com.efom.randomlearn.R
import android.content.Intent
import com.efom.randomlearn.EditActivity
import java.util.ArrayList

class AdapVH_Lista(private val c: Activity) : RecyclerView.Adapter<RecyVH_Listas>() {
    private val listMensaje: MutableList<Tarjeta> = ArrayList()
    var recyVH_historial: ArrayList<RecyVH_Listas>

    //Insertar iten notificar los cambios en el adaptador
    fun addLista(_Tarjeta: Tarjeta) {
        listMensaje.add(_Tarjeta)
        notifyItemInserted(listMensaje.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyVH_Listas {
        val view_grupo = LayoutInflater.from(c).inflate(R.layout.card_list, parent, false)
        view_grupo.setOnClickListener {
            val intent = Intent(c.applicationContext, EditActivity::class.java)
            intent.putExtra("id_lista", listMensaje[viewType].id_lista)
            intent.putExtra("id_tarjeta", listMensaje[viewType].id_tarjeta)
            intent.putExtra("ultimo", listMensaje[listMensaje.size - 1].id_tarjeta)
            c.startActivity(intent)
        }
        return RecyVH_Listas(view_grupo)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: RecyVH_Listas, position: Int) {
        holder.gettVItemCunt_CL().text = (position + 1).toString()
        holder.gettVItemPregunta_CL().text = listMensaje[position].pregunta.toString()
        holder.gettVItemRespuesta_CL().text = listMensaje[position].respuesta.toString()
        recyVH_historial.add(holder)
    }

    override fun getItemCount(): Int {
        return listMensaje.size
    }

    init {
        recyVH_historial = ArrayList()
    }
}
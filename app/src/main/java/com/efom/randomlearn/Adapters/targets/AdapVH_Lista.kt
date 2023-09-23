package com.efom.randomlearn.adapters.targets

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.EditActivity
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.R

class AdapVH_Lista(private val c: Activity) : RecyclerView.Adapter<RecyVH_Listas>() {
    private val listMensaje: MutableList<Card> = ArrayList()
    var recyVH_historial: ArrayList<RecyVH_Listas>

    //Insertar iten notificar los cambios en el adaptador
    fun addLista(_Card: Card) {
        listMensaje.add(_Card)
        notifyItemInserted(listMensaje.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyVH_Listas {
        val view_grupo = LayoutInflater.from(c).inflate(R.layout.card_list, parent, false)
        view_grupo.setOnClickListener {
            val intent = Intent(c.applicationContext, EditActivity::class.java)
            intent.putExtra("id_lista", listMensaje[viewType].idMetadata)
            intent.putExtra("id_tarjeta", listMensaje[viewType].idCard)
            intent.putExtra("ultimo", listMensaje[listMensaje.size - 1].idCard)
            c.startActivity(intent)
        }
        return RecyVH_Listas(view_grupo)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: RecyVH_Listas, position: Int) {
        holder.gettVItemCunt_CL().text = (position + 1).toString()
        holder.gettVItemPregunta_CL().text = listMensaje[position].answer.toString()
        holder.gettVItemRespuesta_CL().text = listMensaje[position].question.toString()
        recyVH_historial.add(holder)
    }

    override fun getItemCount(): Int {
        return listMensaje.size
    }

    init {
        recyVH_historial = ArrayList()
    }
}
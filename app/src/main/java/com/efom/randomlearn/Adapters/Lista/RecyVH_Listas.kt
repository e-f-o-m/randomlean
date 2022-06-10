package com.efom.randomlearn.Adapters.Lista
import androidx.recyclerview.widget.RecyclerView
import com.efom.randomlearn.R
import android.widget.TextView
import android.view.View

class RecyVH_Listas(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tVItemCunt_CL: TextView
    private val tVItemPregunta_CL: TextView
    private val tVItemRespuesta_CL: TextView
    fun gettVItemCunt_CL(): TextView {
        return tVItemCunt_CL
    }

    fun gettVItemPregunta_CL(): TextView {
        return tVItemPregunta_CL
    }

    fun gettVItemRespuesta_CL(): TextView {
        return tVItemRespuesta_CL
    }

    init {
        tVItemCunt_CL = itemView.findViewById<View>(R.id.tVItemCunt_CL) as TextView
        tVItemPregunta_CL = itemView.findViewById<View>(R.id.tVItemPregunta_CL) as TextView
        tVItemRespuesta_CL = itemView.findViewById<View>(R.id.tVItemRespuesta_CL) as TextView
    }
}
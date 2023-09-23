package com.efom.randomlearn.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.efom.randomlearn.database.MyDB
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.utils.CONSTS
import com.efom.randomlearn.utils.DT

class RandomController(context: Context) {
    val db: MyDB = MyDB.getDB(context)!!

    fun getCardByOrder(idMetadata: Int, order: Int): ArrayList<Card> {
        return ArrayList(db.cardsDAO().getAllByMetaOrder(idMetadata, order))
    }

    fun getCardsByRecord(idMetadata: Int, start: Long): ArrayList<Card> {
        return ArrayList(db.cardsDAO().getRecord(idMetadata, start))
    }

    @SuppressLint("ResourceType")
    fun getSpaceLearn(idMetadata: Int, spaceLearnSize: Int): ArrayList<Card> {
        val data = getCardByOrder(idMetadata, CONSTS.SPACELEARN_OPTION)

        data.forEach {
            Log.i(" 📌 27", "RandomController🥚getSpaceLearn🍄name: " +it.question)
        }

        //Actuales
        if (data.size < spaceLearnSize) {
            val iterator = getCardByOrder(idMetadata, CONSTS.RANDOM).iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (data.size < spaceLearnSize && item.type != CONSTS.SPACE_REPEAT_DB && item.difficulty!! > 0) {
                    item.startDate = DT.startDay()
                    item.endDate = DT.endDay()
                    item.type = CONSTS.SPACE_REPEAT_DB
                    db.cardsDAO().update(item)
                    data.add(item)
                } else {
                    break
                }
            }
        }

        //
        //Nuevos
        if (data.size > spaceLearnSize) {
            val size = (data.size - spaceLearnSize) - 1
            for (i in 0..size) {
                data[i].type = CONSTS.TEXTO_DB
                data[i].startDate = 0
                data[i].endDate = 0
                db.cardsDAO().update(data[i])
                data.removeFirst()
            }
        }
        return data
    }

}
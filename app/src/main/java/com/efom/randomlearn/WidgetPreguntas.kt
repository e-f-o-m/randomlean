package com.efom.randomlearn

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RemoteViews
import com.efom.randomlearn.database.MyDB
import com.efom.randomlearn.models.Card
import org.apache.commons.lang3.BooleanUtils
import java.util.*
import kotlin.collections.ArrayList

class WidgetPreguntas : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_TRUE == intent.action || ACTION_FALSE == intent.action) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, this.javaClass)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (i in appWidgetIds.indices) {
                esNext = BooleanUtils.toBoolean(intent.action)
                val views = RemoteViews(context.packageName, R.layout.preguntas_widget)
                updateAppWidget(context, appWidgetManager, appWidgetIds[i])
                appWidgetManager.updateAppWidget(appWidgetIds[i], views)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Companion.appWidgetIds = appWidgetIds
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        private const val ACTION_TRUE = "true"
        private const val ACTION_FALSE = "false"
        private var esPregunta = true
        private var esNext = true
        private var numAleatorio = -1

        /*    private static long UPDATE_INTERVAL = 8000L;
    private static Runnable updateTimeTask;
    private static Handler handler;*/
        lateinit var appWidgetIds: IntArray
        private val random = Random()
        private var sizeArray = 0
        private var idMetadata = 0
        private var prefs: SharedPreferences? = null
        lateinit var listCards: ArrayList<Card>
        lateinit var db : MyDB
        fun getRefreshPendingIntent(context: Context?, esNext: String?): PendingIntent {
            val intent = Intent(context, WidgetPreguntas::class.java)
            intent.action = esNext
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        //TODO mUpdate
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val view = RemoteViews(context.packageName, R.layout.preguntas_widget)
            view.setOnClickPendingIntent(R.id.appwidget_tipo_PoR, getRefreshPendingIntent(context, ACTION_FALSE))
            view.setOnClickPendingIntent(R.id.appwidget_pregunta_o_respuesta, getRefreshPendingIntent(context, ACTION_TRUE))
            prefs = context.getSharedPreferences("mPreferences", Context.MODE_PRIVATE)
            db = MyDB.getDB(context)!!
            idMetadata = prefs!!.getInt(context.getString(R.string.lista_widget) , -1)
            listCards = ArrayList(db.cardsDAO().getAllByMetadata(idMetadata))
            if (idMetadata != -1 && listCards!!.size > 0) {
                sizeArray = listCards!!.size
                if (esNext || numAleatorio == -1) {
                    numAleatorio = random.nextInt(sizeArray)
                    esPregunta = true
                }
                if (esPregunta) {
                    view.setTextViewText(R.id.appwidget_tipo_PoR, "Pregunta")
                    view.setTextColor(R.id.appwidget_tipo_PoR, context.resources.getColor(R.color.text_d))
                    view.setTextViewText(R.id.appwidget_pregunta_o_respuesta, listCards!!.get(numAleatorio).answer)
                    esPregunta = false
                } else {
                    view.setTextViewText(R.id.appwidget_tipo_PoR, "Respuesta")
                    view.setTextColor(R.id.appwidget_tipo_PoR, context.resources.getColor(R.color.colorPrimary))
                    view.setTextViewText(R.id.appwidget_pregunta_o_respuesta, listCards!!.get(numAleatorio).question)
                    esPregunta = true
                }
                appWidgetManager.updateAppWidget(appWidgetId, view)
            } else {
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                view.setOnClickPendingIntent(R.id.appwidget_tipo_PoR, pendingIntent)
                view.setOnClickPendingIntent(R.id.appwidget_pregunta_o_respuesta, pendingIntent)
            }
        }
    }

    
}
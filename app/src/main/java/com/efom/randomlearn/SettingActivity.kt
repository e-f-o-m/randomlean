package com.efom.randomlearn

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.efom.randomlearn.databinding.ActivitySettingBinding
import com.efom.randomlearn.utils.AlarmReceiver
import com.efom.randomlearn.utils.CONSTS
import com.efom.randomlearn.utils.Pickers
import java.util.Calendar


class SettingActivity : AppCompatActivity() {
    private lateinit var b: ActivitySettingBinding

    val TTS_PRE = "tts_pregunta"
    val TTS_RES = "tts_respuesta"
    var opTtsPre = 0
    var opTtsRes = 0
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    var notifyDays = arrayListOf(false, false, false, false, false, false, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(b.root)

        prefs = getSharedPreferences("mPreferences", MODE_PRIVATE)
        editor = prefs.edit()

        /* TALK SETTINGS */
        val languages = resources.getStringArray(R.array.lang_options)
        val adapter = ArrayAdapter(this, R.layout.menu_tv, languages)
        b.sPPreguntaTTSSA.adapter = adapter
        b.sPRespuestaTTSSA.adapter = adapter

        opTtsRes = prefs.getInt(TTS_RES, 0)
        opTtsPre = prefs.getInt(TTS_PRE, 0)
        b.sPPreguntaTTSSA.setSelection(opTtsPre)
        b.sPRespuestaTTSSA.setSelection(opTtsRes)

        //Definir? tema
        if (prefs.getBoolean(CONSTS.IS_DARK, true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        b.eTFontSizeSA.setText(prefs.getInt("fontsize", 16).toString())
        b.eTSeparadorSA.setText(prefs.getString("separador", null))

        b.eTNSpaceLearnSA.setText(prefs.getInt(CONSTS.N_SPACE_LEARN, 0).toString())


        b.sWTemaSA.isChecked = prefs.getBoolean(CONSTS.IS_DARK, true)

        editor.apply()

        if (b.sWTemaSA.isChecked) {
            b.sWTemaSA.text = "Tema Nocturno"
        } else {
            b.sWTemaSA.text = "Tema Claro"
        }


        val path = getExternalFilesDir("")!!.path.split("/0/").toTypedArray()[1]
        b.tVRutaAA.text = path

        actions()
        createChannel()
        settingsNotifications()
    }

    private fun actions() {
        b.sWTemaSA.setOnCheckedChangeListener { _, isChecked ->
            if (b.sWTemaSA.isChecked) {
                b.sWTemaSA.text = "Tema Nocturno"
            } else {
                b.sWTemaSA.text = "Tema Claro"
            }
            editor.putBoolean(CONSTS.IS_DARK, isChecked).apply()

            recreate()
        }

        /* TALK */
        b.sPPreguntaTTSSA.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                editor.putInt(TTS_PRE, position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        b.sPRespuestaTTSSA.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                editor.putInt(TTS_RES, position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }


        /* ACCEPT */

        b.btnAceparSA.setOnClickListener {
            editor.putString("separador", b.eTSeparadorSA.text.toString().trim { it <= ' ' })
            editor.putBoolean(CONSTS.IS_DARK, b.sWTemaSA.isChecked)
            editor.putInt("fontsize", b.eTFontSizeSA.text.toString().trim().toInt())
            editor.putInt(CONSTS.N_SPACE_LEARN, b.eTNSpaceLearnSA.text.toString().trim().toInt())
            editor.apply()
            onBackPressed()
        }

        /* GUIA */
        b.btnGiaSA.setOnClickListener { v: View? ->
            b.fLGiaSA.visibility = View.VISIBLE
        }
        b.fLGiaSA.setOnClickListener { v: View? ->
            b.fLGiaSA.visibility = View.GONE
        }
    }

    private fun setNotifications() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateTime = prefs.getString(CONSTS.PREFERENCE_NOTIFY.HOUR, ":")!!.split(":")
        if (dateTime.isEmpty()) {
            Toast.makeText(applicationContext, "Falta la hora", Toast.LENGTH_SHORT).show()
            return
        }
        for ((indexDay, state) in notifyDays.withIndex()) {
            if(!state) continue
            val calendar: Calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, dateTime[0].toInt())
                set(Calendar.MINUTE, dateTime[1].toInt())
                add(Calendar.DAY_OF_WEEK, indexDay+1)
            }

            intent.putExtra("notificationId", indexDay)

            // Configurar el repetir diariamente
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                getPendingIntent(applicationContext, indexDay)
            )
        }
        Toast.makeText(applicationContext, "Notificaciones activadas con exito", Toast.LENGTH_SHORT).show()
    }

    private fun getPendingIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "myChannel",
                "MySuperChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = ""
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun settingsNotifications() {
        //get state, get days, get hour -> preferences
        notifyDays = getPrefDays()

        val isChecked =
            if (prefs.contains(CONSTS.PREFERENCE_NOTIFY.STATE) && prefs.all[CONSTS.PREFERENCE_NOTIFY.STATE] is Boolean) {
                prefs.getBoolean(CONSTS.PREFERENCE_NOTIFY.STATE, false)
            } else {
                false
            }

        b.sWNotificationsAS.isChecked = isChecked

        b.lLOptionsNotificationsAS.visibility =
            if (b.sWNotificationsAS.isChecked) View.VISIBLE else View.GONE
        b.btnNotificationPickerAS.text = prefs.getString(CONSTS.PREFERENCE_NOTIFY.HOUR, "HH:MM")

        b.sWNotificationsAS.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                b.lLOptionsNotificationsAS.visibility = View.VISIBLE
            } else {
                for ((indexDay, state) in notifyDays.withIndex()) {
                    val intent = Intent(applicationContext, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        applicationContext,
                        indexDay,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmManager =
                        applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
                
                b.lLOptionsNotificationsAS.visibility = View.GONE
            }


            editor.putBoolean(CONSTS.PREFERENCE_NOTIFY.STATE, isChecked).apply()
        }

        b.btnLAS.setOnClickListener {
            notifyDays[0] = !notifyDays[0]
            b.btnLAS.setTextColor(if (notifyDays[0]) Color.MAGENTA else Color.GRAY)
            putPrefDays()
        }
        b.btnMAS.setOnClickListener {
            notifyDays[1] = !notifyDays[1]
            b.btnMAS.setTextColor(if (notifyDays[1]) Color.MAGENTA else Color.GRAY)
            putPrefDays()
        }
        b.btnXAS.setOnClickListener {
            notifyDays[2] = !notifyDays[2]
            b.btnXAS.setTextColor(if (notifyDays[2]) Color.MAGENTA else Color.GRAY)
            putPrefDays()
        }
        b.btnJAS.setOnClickListener {
            notifyDays[3] = !notifyDays[3]
            b.btnJAS.setTextColor(if (notifyDays[3]) Color.MAGENTA else Color.GRAY)
            putPrefDays()
        }
        b.btnVAS.setOnClickListener {
            notifyDays[4] = !notifyDays[4]
            b.btnVAS.setTextColor(if (notifyDays[4]) Color.MAGENTA else Color.GRAY)
            putPrefDays()
        }
        b.btnSAS.setOnClickListener {
            notifyDays[5] = !notifyDays[5]
            b.btnSAS.setTextColor(if (notifyDays[5]) Color.MAGENTA else Color.GRAY)
            putPrefDays()
        }
        b.btnDAS.setOnClickListener {
            notifyDays[6] = !notifyDays[6]
            b.btnDAS.setTextColor(if (notifyDays[6]) Color.MAGENTA else Color.GRAY)
            putPrefDays()
        }

        b.btnNotificationPickerAS.setOnClickListener {
            Pickers(this, supportFragmentManager).showTimePicker { res ->
                b.btnNotificationPickerAS.text = res
                editor.putString(CONSTS.PREFERENCE_NOTIFY.HOUR, res).apply()
            }
        }

        //Save
        b.btnSaveNotifyAS.setOnClickListener {
            setNotifications()
        }
    }

    fun putPrefDays() {
        editor.putString(CONSTS.PREFERENCE_NOTIFY.DAYS, notifyDays.joinToString(separator = ","))
            .apply()
    }

    fun getPrefDays(): ArrayList<Boolean> {
        val notifyDaysString = prefs.getString(CONSTS.PREFERENCE_NOTIFY.DAYS, "")
        val notifyDaysList = notifyDaysString?.split(",")?.map { it.toBoolean() } ?: emptyList()
        b.btnLAS.setTextColor(if (notifyDaysList[0]) Color.MAGENTA else Color.GRAY)
        b.btnMAS.setTextColor(if (notifyDaysList[1]) Color.MAGENTA else Color.GRAY)
        b.btnXAS.setTextColor(if (notifyDaysList[2]) Color.MAGENTA else Color.GRAY)
        b.btnJAS.setTextColor(if (notifyDaysList[3]) Color.MAGENTA else Color.GRAY)
        b.btnVAS.setTextColor(if (notifyDaysList[4]) Color.MAGENTA else Color.GRAY)
        b.btnSAS.setTextColor(if (notifyDaysList[5]) Color.MAGENTA else Color.GRAY)
        b.btnDAS.setTextColor(if (notifyDaysList[6]) Color.MAGENTA else Color.GRAY)
        return ArrayList(notifyDaysList)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
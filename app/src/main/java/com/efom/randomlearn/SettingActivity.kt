package com.efom.randomlearn
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.efom.randomlearn.utils.CONSTS
import com.efom.randomlearn.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var b: ActivitySettingBinding

    val TTS_PRE = "tts_pregunta"
    val TTS_RES = "tts_respuesta"
    var opTtsPre = 0
    var opTtsRes = 0
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

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
    }

    fun actions(){
        b.sWTemaSA.setOnCheckedChangeListener { buttonView, isChecked ->
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                editor.putInt(TTS_PRE, position)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        b.sPRespuestaTTSSA.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
            b.fLGiaSA .visibility = View.VISIBLE
        }
        b.fLGiaSA.setOnClickListener { v: View? ->
            b.fLGiaSA.visibility = View.GONE
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
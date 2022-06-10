package com.efom.randomlearn
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent
import android.view.View
import android.widget.*

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val eTSeparador_Sett: EditText = findViewById(R.id.eTSeparador_Sett)
        val eTFontSize_Sett: EditText = findViewById(R.id.eTFontSize_Sett)
        val sWFijarCara_Sett: Switch = findViewById(R.id.sWFijarCara_Sett)
        val sWTema_Sett: Switch = findViewById(R.id.sWTema_Sett)
        val sPPreguntaTTS_Sett: Spinner = findViewById(R.id.sPPreguntaTTS_Sett)
        val sPRespuestaTTS_Sett: Spinner = findViewById(R.id.sPRespuestaTTS_Sett)
        val cBLibreCara_Sett: CheckBox = findViewById(R.id.cBLibreCara_Sett)
        val btnAcepar_Sett: Button = findViewById(R.id.btnAcepar_Sett)
        val btnGia_Sett: Button = findViewById(R.id.btnGia_Sett)
        val tVTituloFijado_Sett: TextView = findViewById(R.id.tVTituloFijado_Sett)
        val tVRuta_AA: TextView = findViewById(R.id.tVRuta_AA)
        val fL_gia_sett: FrameLayout = findViewById(R.id.fL_gia_sett)

        val prefs: SharedPreferences = getSharedPreferences("mPreferences", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()

        //Definir? tema
        if (prefs.getBoolean(getString(R.string.temaOscuro), true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        eTFontSize_Sett.setText("" + prefs.getInt("fontsize", 16))
        eTSeparador_Sett.setText(prefs.getString("separador", null))
        sWFijarCara_Sett.isChecked = prefs.getBoolean(ES_SIEMPRE_PREGUNTA, true)
        sWTema_Sett.isChecked = prefs.getBoolean(getString(R.string.temaOscuro), true)
        cBLibreCara_Sett.isChecked = prefs.getBoolean(ES_CONGELAR_CARA, false)

        editor.apply()
        if (sWFijarCara_Sett.isChecked) {
            tVTituloFijado_Sett.text = "La tarjeta siguiente siempre es pregunta"
        } else {
            tVTituloFijado_Sett.text = "La tarjeta siguiente siempre es respuesta"
        }
        if (sWTema_Sett.isChecked) {
            sWTema_Sett.text = "Tema Nocturno"
        } else {
            sWTema_Sett.text = "Tema Claro"
        }
        sWFijarCara_Sett.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                tVTituloFijado_Sett.text = "La tarjeta siguiente siempre es pregunta"
            } else {
                tVTituloFijado_Sett.text = "La tarjeta siguiente siempre es respuesta"
            }
        })
        sWTema_Sett.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (sWTema_Sett.isChecked) {
                sWTema_Sett.text = "Tema Nocturno"
            } else {
                sWTema_Sett.text = "Tema Claro"
            }
        })

        val languages = resources.getStringArray(R.array.lang_options)
        if (sPPreguntaTTS_Sett != null && sPRespuestaTTS_Sett != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
            sPPreguntaTTS_Sett.adapter = adapter
            sPRespuestaTTS_Sett.adapter = adapter
            sPPreguntaTTS_Sett.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    editor.putInt(TTS_PRE, position)
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
            sPRespuestaTTS_Sett.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    editor.putInt(TTS_RES, position)
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }


        cBLibreCara_Sett.setOnCheckedChangeListener { _, isChecked ->
            sWFijarCara_Sett.isChecked = !isChecked
            sWFijarCara_Sett.isEnabled = !isChecked
        }
        btnAcepar_Sett.setOnClickListener(View.OnClickListener {
            editor.putString("separador", eTSeparador_Sett.text.toString().trim { it <= ' ' })
            editor.putBoolean(ES_SIEMPRE_PREGUNTA, sWFijarCara_Sett.isChecked)
            editor.putBoolean(getString(R.string.temaOscuro), sWTema_Sett.isChecked)
            editor.putBoolean(ES_CONGELAR_CARA, cBLibreCara_Sett.isChecked)
            editor.putInt(
                "fontsize",
                eTFontSize_Sett.text.toString().trim { it <= ' ' }.toInt()
            )
            editor.apply()
            onBackPressed()
        })
        btnGia_Sett.setOnClickListener { v: View? ->
            fL_gia_sett.visibility = View.VISIBLE
        }
        fL_gia_sett.setOnClickListener { v: View? ->
            fL_gia_sett.visibility = View.GONE
        }
        val path = getExternalFilesDir("")!!.path.split("/0/").toTypedArray()[1]
        tVRuta_AA.text = path
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val ES_CONGELAR_CARA = "congelarCara"
        private const val TTS_PRE = "tts_pregunta"
        private const val TTS_RES = "tts_respuesta"
        private const val ES_SIEMPRE_PREGUNTA = "esSiemprePregunta"
    }
}
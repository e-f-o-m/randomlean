package com.efom.randomlearn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.efom.randomlearn.utils.ConstFIREBASE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    var isLogin = true
    private lateinit var dbFrb: DatabaseReference
    private lateinit var constFR: ConstFIREBASE
    private lateinit var auth: FirebaseAuth

    private lateinit var tVLogin_LA: TextView
    private lateinit var btnAcepar_LA: Button
    private lateinit var eTUser_LA: EditText
    private lateinit var eTPassword_LA: EditText
    private lateinit var eTMail_LA: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tVLogin_LA = findViewById(R.id.tVLogin_LA)
        btnAcepar_LA = findViewById(R.id.btnAcepar_LA)
        eTUser_LA = findViewById(R.id.eTUser_LA)
        eTPassword_LA = findViewById(R.id.eTPassword_LA)
        eTMail_LA = findViewById(R.id.eTMail_LA)

        setContentView(R.layout.activity_login)


        dbFrb = Firebase.database.reference
        constFR = ConstFIREBASE
        
        auth = FirebaseAuth.getInstance()

        showHiddenViews(true)

        tVLogin_LA.setOnClickListener {
            showHiddenViews(!isLogin)
        }

        btnAcepar_LA.setOnClickListener {
            Toast.makeText(this, "Por favor espere", Toast.LENGTH_SHORT).show()
            if(isLogin && eTPassword_LA.text.trim().isNotEmpty()
                    && eTPassword_LA.text.length > 5
                    && eTMail_LA.text.trim().isNotEmpty()
            ){
                signIn()
            }else if(eTMail_LA.text.trim().isNotEmpty()
                    && eTUser_LA.text.trim().isNotEmpty()
                    && eTPassword_LA.text.trim().isNotEmpty()
                    && eTUser_LA.text.length > 4
                    && eTPassword_LA.text.length > 5
            ){
                signUp()
            }else{
                Toast.makeText(this, "Error: Verificar los campos", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.let {
            if(it.email.toString().isNotEmpty()) startActivity(Intent(this, ListsWebActivity::class.java))
        }
    }

    fun showHiddenViews(isLogin : Boolean){

        if (isLogin) {
            eTUser_LA.visibility = View.GONE
            eTPassword_LA.visibility = View.VISIBLE
            eTMail_LA.visibility = View.VISIBLE
            tVLogin_LA.text = "Registrarse"
        }else{
            eTUser_LA.visibility = View.VISIBLE
            eTPassword_LA.visibility = View.VISIBLE
            eTMail_LA.visibility = View.VISIBLE
            tVLogin_LA.text = "Iniciar"
        }
        this.isLogin = isLogin
        Log.d("81 LoginActivity.kt", "showHiddenViews: is "+isLogin)
    }


    private fun signIn() {
        val email = eTMail_LA.text.toString()
        val password = eTPassword_LA.text.toString()
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, ListsWebActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
    }
    private fun signUp() {
        val email = eTMail_LA.text.toString()
        val stUser = eTUser_LA.text.toString()
        val password = eTPassword_LA.text.toString()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                dbFrb.child(constFR.USERS).child(task.result!!.user!!.uid).setValue( User(stUser, email) )
                showHiddenViews(true)
                Toast.makeText(this, "Registro Exitoso!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("tyex ", "createUser:onComplete:" + task.isSuccessful)
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@IgnoreExtraProperties
data class User(
        var user_name: String? = "",
        var email: String? = ""
)

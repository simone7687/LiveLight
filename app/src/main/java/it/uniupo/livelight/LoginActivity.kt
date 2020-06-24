package it.uniupo.livelight

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.button_signIn -> {
                // Navigate to RegisterActivity
                val intentRegister = Intent(applicationContext, RegistrationActivity::class.java)
                startActivity(intentRegister)
            }
        }
    }
}

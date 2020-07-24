package it.uniupo.livelight

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener(this)
        button_registration.setOnClickListener(this)

        //Set activity title and disable back button
        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.login)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_login -> {
                // TODO: login check
                // Navigate to MainActivity
                val intentMain = Intent(this, MainActivity::class.java)
                startActivity(intentMain)
            }
            R.id.button_registration -> {
                // Navigate to RegistrationActivity
                val intentRegistration = Intent(this, RegistrationActivity::class.java)
                startActivity(intentRegistration)
            }
        }
    }
}

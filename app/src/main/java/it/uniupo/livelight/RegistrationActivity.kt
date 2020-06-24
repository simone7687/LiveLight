package it.uniupo.livelight

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> finish()
        }
    }
}

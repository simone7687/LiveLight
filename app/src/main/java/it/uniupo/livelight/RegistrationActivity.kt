package it.uniupo.livelight

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class RegistrationActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        button_registration.setOnClickListener(this)

        // Back button
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_registration -> {
                // TODO: Regitration check
            }
        }
    }

    /**
     * Back button action
     */
    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }
}

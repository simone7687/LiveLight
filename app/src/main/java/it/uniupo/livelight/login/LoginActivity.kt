package it.uniupo.livelight.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import it.uniupo.livelight.MainActivity
import it.uniupo.livelight.R
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Activity to login
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        button_login.setOnClickListener(this)
        button_registration.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_login -> {
                // check that all fields have been filled
                if (editText_email.text.toString()
                        .isEmpty() || editText_password.text.toString().isEmpty()
                ) {
                    Toast.makeText(
                        baseContext, getString(R.string.empty_input_field),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                // check if it's a valid email
                if (TextUtils.isEmpty(editText_email.text.toString()) || !android.util.Patterns.EMAIL_ADDRESS.matcher(
                        editText_email.text.toString()
                    ).matches()
                ) {
                    Toast.makeText(
                        baseContext, R.string.invalid_email,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                // TODO: start Loading Activity

                // authentication with email and password
                login(editText_email.text.toString(), editText_password.text.toString())

                // TODO: close Loading Activity
            }
            R.id.button_registration -> {
                // navigate to RegistrationActivity
                val intentRegistration = Intent(this, RegistrationActivity::class.java)
                startActivity(intentRegistration)
            }
        }
    }

    /**
     * Try logging in:
     * if the connection was successful, it switches to the Main activity,
     * otherwise it gives an error signal.
     */
    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(this) {
                // navigate to MainActivity
                val intentMain = Intent(this, MainActivity::class.java)
                startActivity(intentMain)
                finish()
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Show Password and Hide Password
     */
    fun showHidePass(view: View) {
        if (view.getId() == R.id.show_pass_btn) {
            if (editText_password.getTransformationMethod() == PasswordTransformationMethod.getInstance()
            ) {
                // Show Password
                editText_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide Password
                editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        }
    }
}

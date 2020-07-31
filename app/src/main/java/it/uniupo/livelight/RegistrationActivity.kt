package it.uniupo.livelight

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegistrationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        button_registration.setOnClickListener(this)

        // Back button
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = getString(R.string.registration)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_registration -> {
                // check for empty fields
                if (!checkEmptyFields()) {
                    Toast.makeText(
                        baseContext, R.string.empty_input_field,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                // check if the password exceeds 6 characters
                if (!(editText_password.text.toString().length < 6)) {
                    Toast.makeText(
                        baseContext, R.string.short_password,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                // check if it is a robust password
                if (!checkPasswordCharacters(editText_password.toString())) {
                    Toast.makeText(
                        baseContext, R.string.weak_password,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                // check if the password is the same as the verification password
                if (editText_password.toString() != editText_verifyPassword.toString()) {
                    Toast.makeText(
                        baseContext, R.string.different_passwords,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                // check whether the data processing has been authorized
                if (!checkBox_authorizesData.isActivated) {
                    Toast.makeText(
                        baseContext, R.string.unauthorized_data_processing,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                // check if it's a valid email and check if the email has already been used
                if (!TextUtils.isEmpty(editText_email.toString()) && android.util.Patterns.EMAIL_ADDRESS.matcher(
                        editText_email.toString()
                    ).matches()
                ) {
                    // TODO: check if the email has already been used
                } else {
                    Toast.makeText(
                        baseContext, R.string.invalid_email,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                // TODO: registration
            }
        }
    }

    /**
     * Back button action
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Check for empty mandatory fields
     *
     * Returns false if it finds an empty field
     */
    private fun checkEmptyFields(): Boolean {
        if (editText_name.text.isNullOrEmpty())
            return false
        if (editText_surname.text.isNullOrEmpty())
            return false
        if (editText_email.text.isNullOrEmpty())
            return false
        if (editText_city.text.isNullOrEmpty())
            return false
        if (editText_address.text.isNullOrEmpty())
            return false
        if (editText_password.text.isNullOrEmpty())
            return false
        if (editText_verifyPassword.text.isNullOrEmpty())
            return false
        return true
    }

    /**
     * Check if the password has the required characters, return true if it has them.
     *
     * Characters required:
     * - lowercase letter [a-z]
     * - uppercase letter [A-Z]
     * - number [0-9]
     * - special character [@#$%^&+=]
     */
    private fun checkPasswordCharacters(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val charactersRequired = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(charactersRequired)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }
}

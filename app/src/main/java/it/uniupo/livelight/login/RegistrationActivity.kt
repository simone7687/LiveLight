package it.uniupo.livelight.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.MainActivity
import it.uniupo.livelight.R
import it.uniupo.livelight.dialog.ProcessFragment
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.regex.Pattern

/**
 * Activity to register
 */
class RegistrationActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val processDialog = ProcessFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Back button
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        button_registration.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_registration -> {
                val fm: FragmentManager = supportFragmentManager
                val b = Bundle()
                processDialog.arguments = b
                processDialog.isCancelable = false
                processDialog.show(fm, "fragment_process")

                // check for empty fields
                if (!checkEmptyFields()) {
                    Toast.makeText(
                        baseContext,
                        R.string.empty_input_field,
                        Toast.LENGTH_SHORT
                    ).show()
                    processDialog.dismissDialog()
                    return
                }
                // check if it is a robust password
                if (!checkPasswordCharacters(editText_password.text.toString())) {
                    Toast.makeText(
                        baseContext, R.string.password_requirements,
                        Toast.LENGTH_SHORT
                    ).show()
                    processDialog.dismissDialog()
                    return
                }
                // check if the password is the same as the verification password
                if (editText_password.text.toString() != editText_verifyPassword.text.toString()
                ) {
                    Toast.makeText(
                        baseContext,
                        R.string.different_passwords,
                        Toast.LENGTH_SHORT
                    ).show()
                    processDialog.dismissDialog()
                    return
                }
                // check whether the data processing has been authorized
                if (!checkBox_authorizesData.isChecked) {
                    Toast.makeText(
                        baseContext,
                        R.string.unauthorized_data_processing,
                        Toast.LENGTH_SHORT
                    ).show()
                    processDialog.dismissDialog()
                    return
                }

                // check if it's a valid email and check if the email has already been used
                if (TextUtils.isEmpty(editText_email.text.toString()) || !android.util.Patterns.EMAIL_ADDRESS.matcher(
                        editText_email.text.toString()
                    ).matches()
                ) {
                    Toast.makeText(
                        baseContext, R.string.invalid_email,
                        Toast.LENGTH_SHORT
                    ).show()
                    processDialog.dismissDialog()
                    return
                }

                // create an account
                createAccount(
                    editText_email.text.toString(),
                    editText_password.text.toString(),
                    editText_name.text.toString(),
                    editText_surname.text.toString(),
                    editText_city.text.toString(),
                    editText_address.text.toString()
                )
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
     * Check if the password exceeds 6 characters and if the password has the required characters, return true if it has them.
     *
     * Characters required:
     * - lowercase letter [a-z]
     * - uppercase letter [A-Z]
     * - number [0-9]
     * - special character [$%'^&@*#+=]
     */
    private fun checkPasswordCharacters(password: String?): Boolean {
        if (password == null)
            return false
        if (password.length < 6)
            return false
        val charactersRequired =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$%'^&@*#+=])(?=\\S+$).{4,}$"
        val pattern = Pattern.compile(charactersRequired)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }

    /**
     * Try creating an account, if it fails by an error message.
     */
    private fun createAccount(
        email: String,
        password: String,
        name: String,
        surname: String,
        city: String,
        address: String
    ) {
        auth.createUserWithEmailAndPassword(
            email,
            password
        )
            .addOnSuccessListener(this) {
                // User data
                val userDetailsData = hashMapOf(
                    getString(R.string.db__name) to name,
                    getString(R.string.db__surname) to surname,
                    getString(R.string.db__city) to city,
                    getString(R.string.db__address) to address
                )
                // Enters the user's data into the database and logs in
                db.collection(getString(R.string.db_user_details))
                    .document(auth.currentUser?.uid.toString())
                    .set(userDetailsData as Map<String, Any>)
                    .addOnSuccessListener {
                        val intentMain = Intent(this, MainActivity::class.java)
                        startActivity(intentMain)
                        finish()
                        processDialog.dismissDialog()
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                        // if not, delete the user who has just been created
                        val user = auth.currentUser!!
                        user.delete()
                        processDialog.dismissDialog()
                    }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
                processDialog.dismissDialog()
            }
    }
}

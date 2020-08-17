package it.uniupo.livelight.post

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R
import java.util.*
import kotlin.collections.ArrayList

/**
 * Activity to publish a new post
 */
class PostPublisherActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.fragment_post_publisher)

        loadCategoriesSpinner(Locale.getDefault().language)

        // Back button
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Back button action
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Load the categories from the database in the appropriate language per parameter
     * and then enter them in the appropriate Spinner
     */
    private fun loadCategoriesSpinner(localeLanguage: String) {
        val categoriesSpinner = findViewById<Spinner>(R.id.spinner_categories)
        val categories = ArrayList<String>()

        // Enter the first category with instructions
        categories.add(getString(R.string.category_selection))

        // Load the categories from the database
        db.collection("categories").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // If the category in the Italian language is null then insert the category in English
                for (item in task.result!!.documents)
                    categories.add(
                        when (val v = item.getString("name_$localeLanguage")) {
                            null -> {
                                item.getString("name_en").toString()
                            }
                            else -> {
                                v.toString()
                            }
                        }
                    )

                // Enter categories in the Spinner
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
                categoriesSpinner.adapter = adapter
            }
        }.addOnFailureListener { exception ->
            // Try again in English
            loadCategoriesSpinner("en")
            Toast.makeText(
                baseContext, exception.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
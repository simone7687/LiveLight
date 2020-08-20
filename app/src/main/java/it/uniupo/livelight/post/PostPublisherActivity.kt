package it.uniupo.livelight.post

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R
import kotlinx.android.synthetic.main.activity_post_publisher.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Activity to publish a new post
 */
class PostPublisherActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private val REQUEST_CODE_GALLERY = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_publisher)

        loadCategoriesSpinner(Locale.getDefault().language)

        button_image.setOnClickListener {
            // Check permissions
            // if OS < Marshmallow
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                openGalleryForImage()
            } else {
                // Check READ_EXTERNAL_STORAGE permission
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, REQUEST_CODE_GALLERY)
                } else {
                    openGalleryForImage()
                }
            }
        }

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

    /**
     * Launch the gallery activity to select an image
     */
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    // Requested permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // open Gallery
                    openGalleryForImage()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_GALLERY && data != null) {
            // Inserts the image and makes it visible
            image_view.setImageURI(data.data)
            image_view.visibility = View.VISIBLE
        }
    }
}
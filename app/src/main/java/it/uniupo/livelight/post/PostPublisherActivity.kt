package it.uniupo.livelight.post

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.uniupo.livelight.R
import kotlinx.android.synthetic.main.activity_post_publisher.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Activity to publish a new post
 */
class PostPublisherActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val REQUEST_CODE_GALLERY = 100
    private val REQUEST_CODE_CAMERA = 200
    private val REQUEST_CODE_LOCATION = 300

    private lateinit var image: Uri
    private var categorySelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_publisher)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()

        loadCategoriesSpinner()

        button_image.setOnClickListener {
            // Check permissions
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, REQUEST_CODE_GALLERY)
            } else {
                openGalleryForImage()
            }
        }

        button_camera.setOnClickListener {
            // Check permissions
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.CAMERA)
                requestPermissions(permissions, REQUEST_CODE_CAMERA)
            } else {
                openCameraForImage()
            }
        }

        editTextDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                openDatePickerDialog()
        }

        spinner_categories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // p2: position
                categorySelected = p2
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        button_publish.setOnClickListener {
            publishPostWhichFields()
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
     * Load the categories from the database in the appropriate language
     * and then enter them in the appropriate Spinner
     */
    private fun loadCategoriesSpinner() {
        val categoriesSpinner = findViewById<Spinner>(R.id.spinner_categories)
        val categories = ArrayList<String>()

        // Enter the first category with instructions
        categories.add(getString(R.string.category_selection))

        // Load the categories from the database
        db.collection(getString(R.string.db_categories)).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // If the category in the Italian language is null then insert the category in English
                for (item in task.result!!.documents)
                    categories.add(
                        when (val v = item.getString(getString(R.string.db_categories_name))) {
                            null -> {
                                item.getString(getString(R.string.db_categories_name_en)).toString()
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

    /**
     * Launch the camera activity to take a photo
     */
    private fun openCameraForImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
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
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // open Camera
                    openCameraForImage()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // update the last Localization
                    publishPostWhichFields()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Manages permit requests
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_GALLERY && data != null) {
            // Inserts the image and makes it visible
            image_view.setImageURI(data.data)
            image_view.visibility = View.VISIBLE
            image = data.data!!
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA && data != null) {
            image_view.setImageBitmap(data.extras?.get("data") as Bitmap)
            image_view.visibility = View.VISIBLE
        }
    }

    /**
     * Open Date Picker Dialog to select the day
     */
    @SuppressLint("SetTextI18n")
    private fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            var mon: String = monthOfYear.toString()
            if (mon.length < 2)
                mon = "0$monthOfYear"

            var d: String = dayOfMonth.toString()
            if (d.length < 2)
                d = "0$monthOfYear"

            editTextDate.setText("${d}/${mon}/${year}")
        }, year, month, day)
        dpd.show()
    }

    /**
     * Check for empty mandatory fields
     *
     * Returns false if it finds an empty field and sends a message
     */
    private fun checkEmptyFields(): Boolean {
        if (editText_title.text.isNullOrEmpty() || textView_description.text.isNullOrEmpty() || editTextDate.text.isNullOrEmpty() || image.toString()
                .isEmpty() || categorySelected == 0
        ) {
            Toast.makeText(
                baseContext, getString(R.string.empty_input_field),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    /**
     * Publish the post by getting the data from complicated fields
     * Aggregate loading status with a Loading Activity
     */
    private fun publishPostWhichFields() {
        // TODO: update Loading Activity: Loading in progress

        if (checkEmptyFields()) {
            // Requests location permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                requestPermissions(permissions, REQUEST_CODE_LOCATION)
            }
            // Gets last position
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient!!.lastLocation
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        // Publication through local image
                        publishPostWhichLocalImage(
                            editText_title.text.toString(),
                            editTextTextMultiLine_description.text.toString(),
                            editTextDate.text.toString(),
                            categorySelected,
                            task.result!!,
                            image
                        )
                    } else {
                        // TODO: close Loading Activity
                        Toast.makeText(this, R.string.no_location, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * Publish a post by uploading a local image
     * Aggregate loading status with a Loading Activity
     */
    private fun publishPostWhichLocalImage(
        title: String,
        description: String,
        date: String,
        categorySelected: Int,
        lastLocation: Location,
        imagePath: Uri
    ) {
        // TODO: Check if it is an image

        // TODO: update Loading Activity: Upload Image

        // Generate the name file: "use id"_"random ID"
        val path =
            storage.reference.child(getString(R.string.storage_folder_post_images) + auth.currentUser!!.uid + "_" + UUID.randomUUID())

        // Given image ulr path
        val url = path.putFile(imagePath)

        // Upload process management
        url.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            path.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Last step for publication
                task.result?.let {
                    publishPostWhichServerImage(
                        title,
                        description,
                        date,
                        categorySelected,
                        lastLocation,
                        it
                    )
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.image_upload_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Publish the post
     * Aggregate loading status with a Loading Activity
     */
    @SuppressLint("SimpleDateFormat")
    private fun publishPostWhichServerImage(
        title: String,
        description: String,
        date: String,
        categorySelected: Int,
        lastLocation: Location,
        image: Uri
    ) {
        // TODO: start Loading Activity: Loading in progress

        // Check the parameters 
        if (image.toString().isEmpty()) {
            Toast.makeText(this, R.string.image_upload_error, Toast.LENGTH_SHORT).show()
            return
        }
        if (title.isEmpty() || description.isEmpty() || editTextDate.text.isNullOrEmpty() || categorySelected == 0) {
            Toast.makeText(
                baseContext, getString(R.string.empty_input_field),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        // Create a list of data to upload
        val data = hashMapOf(
            getString(R.string.db__userId) to auth.currentUser?.uid,
            getString(R.string.db__title) to title,
            getString(R.string.db__description) to description,
            getString(R.string.db__category) to categorySelected,
            getString(R.string.db__coordinates) to doubleArrayOf(
                lastLocation.latitude,
                lastLocation.longitude
            ).toList(),
            getString(R.string.db__dateExpire) to date,
            getString(R.string.db__datePosted) to dateFormat.format(Date()).toString(),
            getString(R.string.db__imageUrl) to image.toString(),
            getString(R.string.db__keywords) to title.toLowerCase(Locale.getDefault()).split(" ")
                .toMutableList()
        )

        // Upload data
        db.collection(getString(R.string.db_post)).document(UUID.randomUUID().toString())
            .set(data as Map<*, *>)
            .addOnSuccessListener {
                onBackPressed()
                Toast.makeText(this, R.string.loading_completed, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }
}
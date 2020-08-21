package it.uniupo.livelight.post

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.uniupo.livelight.R
import kotlinx.android.synthetic.main.activity_post_publisher.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Activity to publish a new post
 */
class PostPublisherActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val REQUEST_CODE_GALLERY = 100
    private val REQUEST_CODE_CAMERA = 200
    private val REQUEST_CODE_LOCATION = 300

    private var lastLocation: Location? = null
    private var image: Uri? = null
    private var categorySelected: Int = 0

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

        button_camera.setOnClickListener {
            // Check permissions
            // if OS < Marshmallow
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                openCameraForImage()
            } else {
                // Check READ_EXTERNAL_STORAGE permission
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    val permissions = arrayOf(Manifest.permission.CAMERA)
                    requestPermissions(permissions, REQUEST_CODE_CAMERA)
                } else {
                    openCameraForImage()
                }
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
            // Check permissions
            // if OS < Marshmallow
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                lastLocation = getLastLocation()
            } else {
                // Check READ_EXTERNAL_STORAGE permission
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                    requestPermissions(permissions, REQUEST_CODE_LOCATION)
                } else {
                    lastLocation = getLastLocation()
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
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // open Camera
                    openCameraForImage()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // update the last Localization
                    lastLocation = getLastLocation()
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
            image = data.data
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA && data != null) {
            image_view.setImageBitmap(data.extras?.get("data") as Bitmap)
            image_view.visibility = View.VISIBLE
        }
    }

    /**
     * Open Date Picker Dialog to select the day
     */
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
     * Requires location permit
     */
    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE_LOCATION
        )
    }

    /**
     * Returns the current position.
     * If you have not been able to get the position returns null and void.
     */
    private fun getLastLocation(): Location? {
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        var lastLocation: Location? = null
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        }
        fusedLocationClient!!.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    lastLocation = task.result
                } else {
                    Toast.makeText(this, R.string.no_location, Toast.LENGTH_SHORT).show()
                }
            }
        return lastLocation
    }

    /**
     * Upload the image to the server.
     * In case it has had any errors it returns null.
     */
    private fun uploadImage(imagePath: Uri?): Uri? {
        if (imagePath == null) {
            Toast.makeText(
                this,
                getString(R.string.empty_input_field),
                Toast.LENGTH_SHORT
            ).show()
            return null;
        }

        // Generate the name file: "use id"_"random ID"
        val path =
            storage.reference.child("uploaded_images/${auth.currentUser!!.uid + "_" + UUID.randomUUID()}")

        // Given image ulr path
        val url = path.putFile(imagePath)

        // Upload process management
        var imageURL: Uri? = null
        url.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            path.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageURL = task.result
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.image_upload_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return imageURL
    }
}
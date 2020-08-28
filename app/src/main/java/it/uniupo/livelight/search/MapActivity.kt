package it.uniupo.livelight.search

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R
import it.uniupo.livelight.post.PostModel

/**
 * Posts map
 */
class MapActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var googleMap: GoogleMap

    private val REQUEST_CODE_LOCATION = 300
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        db = FirebaseFirestore.getInstance()

        // Create map
        map = findViewById(R.id.map)
        map.onCreate(savedInstanceState)
        map.onResume()

        MapsInitializer.initialize(applicationContext)

        map.getMapAsync { mMap ->
            googleMap = mMap
            showCurrentPosition(mMap)
            updatePostMap(mMap)
        }

        // Back button
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Shows the current location through a zoom, but first with position permissions
     */
    private fun showCurrentPosition(googleMap: GoogleMap) {
        // Check permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_DENIED
        ) {
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissions, REQUEST_CODE_LOCATION)
        } else {
            // show my location button
            googleMap.isMyLocationEnabled = true
            // Get last location
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Zoom animation
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    val cameraPosition =
                        CameraPosition.Builder().target(currentLocation).zoom(12f).build()
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
    }

    // Requested permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    showCurrentPosition(googleMap)
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Updates the map of posts
     */
    private fun updatePostMap(googleMap: GoogleMap) {
        db.collection(getString(R.string.db_post)).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (item in task.result!!.documents) {
                    val model = PostModel(item.id)
                    model.title = item.get(getString(R.string.db__title)) as String
                    model.description = item.get(getString(R.string.db__description)) as String
                    model.coordinates = item.get(getString(R.string.db__coordinates)) as ArrayList<Double>

                    // Add posts to the map
                    val itemLocation = LatLng(model.coordinates[0], model.coordinates[1])
                    googleMap.addMarker(
                        MarkerOptions().position(itemLocation).title(model.title)
                            .snippet(model.description)
                    )
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                baseContext, exception.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Back button action
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
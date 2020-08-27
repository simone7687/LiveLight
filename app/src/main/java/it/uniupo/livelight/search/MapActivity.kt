package it.uniupo.livelight.search

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import it.uniupo.livelight.R

class MapActivity : AppCompatActivity() {
    private val REQUEST_CODE_LOCATION = 300
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Create map
        map = findViewById(R.id.map)
        map.onCreate(savedInstanceState)
        map.onResume()

        // Requests location permission
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE_LOCATION
        )

        MapsInitializer.initialize(applicationContext)

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
}
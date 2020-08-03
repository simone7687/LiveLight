package it.uniupo.livelight

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (FirebaseAuth.getInstance().currentUser == null) {
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
            finish()
            return
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_settings -> {
                val intentSettings = Intent(this, SettingsActivity::class.java)
                startActivity(intentSettings)
            }
            R.id.navigation_logout -> {
                logout()
            }
        }
        return true
    }

    private fun logout() {
        val auth = FirebaseAuth.getInstance()
        try {
            auth.signOut()
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
            finish()
        } catch (e: Exception) {

        }
    }
}
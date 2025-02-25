package it.uniupo.livelight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import it.uniupo.livelight.dialog.ErrorActivity
import it.uniupo.livelight.login.LoginActivity
import it.uniupo.livelight.post.PostPublisherActivity

/**
 * MainActivity is the primary and main activity that allows navigation in Fragments
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: if there is no connection shows a message 

        // check if you have already logged in. if not, navigate to LoginActivity
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
            finish()
            return
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each bar_main_menu ID as a set of Ids because each
        // bar_main_menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_profile,
                R.id.navigation_search,
                R.id.navigation_messages
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Handle Floating Action Button
        findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            val intent = Intent(this, PostPublisherActivity()::class.java)
            startActivity(intent)
        }

        // hide FloatingActionButton
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // hide FloatingActionButton
            if (destination.id in arrayOf(R.id.navigation_messages)) {
                findViewById<FloatingActionButton>(R.id.fab)?.hide()
            } else {
                findViewById<FloatingActionButton>(R.id.fab)?.show()
            }
        }
    }

    /**
     * Adds the bar_main_menu "with 3 dots"
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_settings -> {
                val intentSettings = Intent(this, SettingsActivity::class.java)
                startActivity(intentSettings)
            }
            R.id.item_logout -> {
                logout()
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        // If it is not connected to the internet it opens an error message
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    /**
     * If it is not connected to the internet it opens an error message
     */
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notConnected =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            if (notConnected) {
                val intent = Intent(this@MainActivity, ErrorActivity::class.java)
                intent.putExtra("popuptitle", getString(R.string.error))
                intent.putExtra("popuptext", getString(R.string.must_connected_internet_use_app))
                intent.putExtra("popupbtn", getString(R.string.ok))
                intent.putExtra("darkstatusbar", false)
                startActivity(intent)
            }
        }
    }

    /**
     * Logs out and navigate to LoginActivity
     */
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
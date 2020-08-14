package it.uniupo.livelight.post

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.uniupo.livelight.R

/**
 * Activity to publish a new post
 */
class PostPublisherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.fragment_post_publisher)

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
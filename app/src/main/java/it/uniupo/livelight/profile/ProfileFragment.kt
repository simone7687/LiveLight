package it.uniupo.livelight.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R
import it.uniupo.livelight.post.PostModel
import it.uniupo.livelight.post.PostPublisherActivity

/**
 * ProfileFragment is a fragment used the status of the user, his posts and publish new content
 */
class ProfileFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        // Handle Floating Action Button
        root?.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            val intent = Intent(activity, PostPublisherActivity()::class.java)
            startActivity(intent)
        }

        return root
    }
}
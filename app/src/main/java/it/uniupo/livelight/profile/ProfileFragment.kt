package it.uniupo.livelight.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R
import it.uniupo.livelight.post.PostActivity
import it.uniupo.livelight.post.PostListAdapter
import it.uniupo.livelight.post.PostModel

/**
 * ProfileFragment is a fragment used to display the user's status, his posts and publish new content
 */
class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Upload profle data
        updateUserDescription(root.findViewById(R.id.textView_description))
        updateUserPostList(
            root.findViewById(R.id.list_user_post)!!,
            root.findViewById(R.id.textView_posts_posted)
        )

        return root
    }

    /**
     * Upload the necessary information and update the profile description
     */
    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun updateUserDescription(userDescription: TextView) {
        db.collection(getString(R.string.db_user_details))
            .whereEqualTo(FieldPath.documentId(), auth.currentUser?.uid.toString()).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result!!.documents.size > 0) {
                    // Upload user name for a better conversation
                    val userName =
                        task.result!!.documents.first().getString(getString(R.string.db__name))
                    userDescription.text =
                        getString(R.string.hi) + " " + "$userName. " + getString(R.string.profile_description)
                } else {
                    userDescription.text =
                        getString(R.string.hi) + ". " + getString(R.string.profile_description)
                }
            }
    }

    /**
     * Loads all the posts from the db owned by the current user
     */
    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun updateUserPostList(postList: ListView, textPostsPosted: TextView) {
        db.collection(getString(R.string.db_post))
            .whereEqualTo(getString(R.string.db__userId), auth.currentUser?.uid.toString()).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var countPost = 0
                    // for PostListAdapter
                    val posts: ArrayList<PostModel> = ArrayList()

                    for (item in task.result!!.documents) {
                        val model = PostModel(item.id)
                        model.user = item.get(getString(R.string.db__userId)) as String
                        model.title = item.get(getString(R.string.db__title)) as String
                        model.description = item.get(getString(R.string.db__description)) as String
                        model.datePosted = item.get(getString(R.string.db__datePosted)) as String
                        model.image = item.get(getString(R.string.db__imageUrl)) as String

                        // for PostListAdapter
                        posts.add(model)

                        countPost++
                    }

                    // Update the list of posts
                    val postsAdapter = PostListAdapter(this.requireActivity(), posts)
                    postList.adapter = postsAdapter
                    postList.setOnItemClickListener { parent, view, position, id ->
                        postsAdapter.getItem(position)?.let { viewPost(it) }
                    }
                    // Description of user data
                    textPostsPosted.text = getString(R.string.posts_posted) + ": " + countPost
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    activity?.baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun viewPost(post: PostModel) {
        val aa = PostActivity()
        val intent = Intent(activity, aa::class.java)
        intent.putExtra("post_id", post.id)
        intent.putExtra("post_user", post.user)
        intent.putExtra("post_title", post.title)
        intent.putExtra("post_description", post.description)
        intent.putExtra("post_datePosted", post.datePosted)
        intent.putExtra("post_image", post.image)
        startActivity(intent)
    }
}
package it.uniupo.livelight.post

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R
import it.uniupo.livelight.dialog.ApproveFragment
import it.uniupo.livelight.dialog.InsertTextInitiationFragment
import kotlinx.android.synthetic.main.activity_post.*
import java.util.*
import kotlin.math.roundToInt

/**
 * Activity to view the Post
 */
class PostActivity : AppCompatActivity(), ApproveFragment.ApproveDialogListener,
    InsertTextInitiationFragment.InsertTextListener {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var post: PostModel

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Collects data
        post = PostModel(intent.getStringExtra("post_id").toString())
        post.user = intent.getStringExtra("post_user").toString()
        post.title = intent.getStringExtra("post_title").toString()
        post.description = intent.getStringExtra("post_description").toString()
        post.datePosted = intent.getStringExtra("post_datePosted").toString()
        post.image = intent.getStringExtra("post_image").toString()

        setPostDataView(post.title, post.description, post.datePosted, post.image)

        // Fab handler
        if (auth.currentUser?.uid.toString() == post.user) {
            fab_post.setImageDrawable(getDrawable(R.drawable.ic_baseline_delete_white_24))
            fab_post.setOnClickListener {
                val fm: FragmentManager = supportFragmentManager
                val deleteDialog = ApproveFragment()
                val b = Bundle()
                b.putInt("dialog_title", R.string.delete_post)
                b.putInt("dialog_text", R.string.do_you_want_delete_post)
                b.putInt("dialog_positive", R.string.delete)
                deleteDialog.arguments = b
                deleteDialog.show(fm, "fragment_edit_name")
            }
        } else {
            fab_post.setOnClickListener {
                val fm: FragmentManager = supportFragmentManager
                val chatDialog = InsertTextInitiationFragment()
                val b = Bundle()
                b.putInt("dialog_title", R.string.message)
                b.putInt("dialog_positive", R.string.send)
                chatDialog.arguments = b
                chatDialog.show(fm, "fragment_edit_name")
            }
        }
        // Show fab
        fab_post.show()

        setDistaceView(post.id)
        setUserDataView(post.user)

        // Back button
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Insert data of the post in the View
     */
    private fun setPostDataView(
        title: String,
        description: String,
        datePosted: String,
        image: String
    ) {
        setTitle(title)
        textView_description.text = description
        textView_datePosted.text = datePosted
        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.loading_animation)
            .transform(CenterCrop())
            .into(imageView_post)
    }

    /**
     * Insert data of the user in the View
     */
    @SuppressLint("SetTextI18n")
    private fun setUserDataView(user: String) {
        db.collection(getString(R.string.db_user_details)).document(user).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val item = task.result
                    if (item != null) {
                        val name = item.get(getString(R.string.db__name)) as String
                        val surname = item.get(getString(R.string.db__surname)) as String
                        textView_user.text = "$name $surname"
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
     * Calculate the distance between your location and the place where it was posted and enter it in the View
     */
    @SuppressLint("SetTextI18n")
    private fun setDistaceView(itemId: String) {
        db.collection(getString(R.string.db_post)).document(itemId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val item = task.result
                    if (item != null) {
                        val coordinates = item.get("Coordinates") as ArrayList<Double>
                        val postLocation = Location("")
                        postLocation.latitude = coordinates[0]
                        postLocation.longitude = coordinates[1]
                        // Calculates the distance from the item
                        val fusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(this)
                        fusedLocationClient.lastLocation.addOnSuccessListener { currentLocation: Location? ->
                            // Got last known location
                            if (currentLocation != null) {
                                textView_distance.text =
                                    "" + (currentLocation.distanceTo(postLocation) / 1000).roundToInt() + " km"
                            }
                        }
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

    // If the dialogue has a postponed response then delete the post.
    override fun actionApproveDialog(value: Boolean) {
        if (value) {
            db.collection("available_items").document(post.id).delete()
                .addOnSuccessListener {
                    onBackPressed()
                    Toast.makeText(this, "Eliminato", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // If the dialogue has a delayed response then send the message.
    override fun sendTextDialog(text: String) {
        // TODO: send the message
    }
}
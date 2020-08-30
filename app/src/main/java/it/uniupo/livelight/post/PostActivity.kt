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
import java.text.SimpleDateFormat
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
                deleteDialog.show(fm, "fragment_delete")
            }
        } else {
            fab_post.setOnClickListener {
                // Check if the chat already exists
                var existsChat = false
                db.collection(getString(R.string.db_chats))
                    .whereEqualTo(getString(R.string.db__postId), post.id).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loop@ for (item in task.result!!.documents) {
                                val users =
                                    item.get(getString(R.string.db__usersId)) as ArrayList<*>
                                if (users.isNotEmpty())
                                    if (users.contains(post.user) && users.contains(auth.uid))
                                        existsChat = true
                            }
                            if (!existsChat) {
                                val fm: FragmentManager = supportFragmentManager
                                val chatDialog = InsertTextInitiationFragment()
                                val b = Bundle()
                                b.putInt("dialog_title", R.string.message)
                                b.putInt("dialog_positive", R.string.send)
                                chatDialog.arguments = b
                                chatDialog.show(fm, "fragment_chat")
                            } else {
                                Toast.makeText(
                                    baseContext, R.string.there_is_already_chat,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
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
            db.collection(getString(R.string.db_post)).document(post.id).delete()
                .addOnSuccessListener {
                    onBackPressed()
                    Toast.makeText(this, getString(R.string.post_deleted), Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    // If the dialogue has a delayed response then send the message.
    override fun sendTextDialog(text: String) {
        db.collection(getString(R.string.db_chats))
            .whereEqualTo(getString(R.string.db__postId), post.id).get()
            .addOnCompleteListener { task ->
                // Chat data
                if (task.isSuccessful) {
                    val data = hashMapOf(
                        getString(R.string.db__postId) to post.id,
                        getString(R.string.db__imageUrl) to post.image,
                        getString(R.string.db__title) to post.title,
                        getString(R.string.db__usersId) to arrayListOf(
                            auth.currentUser?.uid.toString(),
                            post.user
                        ),
                        getString(R.string.db__ownerId) to post.user
                    )
                    // Enter the chat in the database and send the message
                    val id = UUID.randomUUID().toString()
                    db.collection(getString(R.string.db_chats)).document(id)
                        .set(data as Map<String, Any>)
                        .addOnSuccessListener {
                            val bundle = Bundle()
                            bundle.putString(getString(R.string.db__chatId), id)
                            bundle.putStringArrayList(
                                getString(R.string.db__usersId),
                                arrayListOf<String>(
                                    auth.currentUser?.uid.toString(),
                                    post.user
                                )
                            )
                            bundle.putString(getString(R.string.db__ownerId), post.user)
                            // Send the message
                            sendMessage(text, auth.currentUser?.uid.toString(), post.user, id)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                exception.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }


    /**
     * Send the message
     */
    @SuppressLint("SimpleDateFormat")
    private fun sendMessage(text: String, sender: String, receiver: String, chatID: String) {
        // Message data
        val data = hashMapOf(
            getString(R.string.db__text) to text,
            getString(R.string.db__receiverId) to receiver,
            getString(R.string.db__senderId) to sender,
            getString(R.string.db__dateTime) to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
        )
        // Send the message
        db.collection(getString(R.string.db_chats)).document(chatID)
            .collection(getString(R.string.messages))
            .document(UUID.randomUUID().toString())
            .set(data as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    R.string.message_sent,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
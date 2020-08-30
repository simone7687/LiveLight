package it.uniupo.livelight.chats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import it.uniupo.livelight.R
import it.uniupo.livelight.dialog.ApproveFragment
import it.uniupo.livelight.dialog.ReviewFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity to view the Chat
 */
class ChatActivity : AppCompatActivity(), ReviewFragment.ReviewDialogListener,
    ApproveFragment.ApproveDialogListener {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var chat: ChatModel
    private var lend = false
    lateinit var receiver: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Collects data
        chat = ChatModel(intent.getStringExtra("chat_id").toString())
        chat.title = intent.getStringExtra("chat_title").toString()
        receiver = intent.getStringExtra("chat_receiver_user").toString()

        // Title
        db.collection(getString(R.string.db_user_details)).document(receiver).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    if (task.result != null)
                        title = task.result!!.get(getString(R.string.db__name)) as String
            }

        // Lend button
        val buttonLend = findViewById<Button>(R.id.button_startLend)
        // Check the loan status
        db.collection(getString(R.string.db_lend))
            .document(auth.currentUser?.uid.toString() + receiver).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.get(getString(R.string.db__dateEnd)) == "") {
                        buttonLend?.text = getString(R.string.end_lend)
                        lend = true
                    } else {
                        buttonLend?.text = getString(R.string.start_lend)
                        lend = false
                    }
                }
            }
        // Lend button Settings
        if (auth.currentUser?.uid.toString() == receiver) {
            buttonLend?.visibility = View.VISIBLE
            buttonLend?.setOnClickListener {
                if (!lend) {
                    val fm: FragmentManager = supportFragmentManager
                    val deleteDialog = ApproveFragment()
                    val b = Bundle()
                    b.putInt("dialog_title", R.string.lend_article)
                    b.putInt("dialog_text", R.string.are_sure_lend_your_article)
                    deleteDialog.arguments = b
                    deleteDialog.show(fm, "fragment_lend")
                } else {
                    actionApproveDialog(true)
                }
            }
        }

        // Back button
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Adds the bar_chat_menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar_chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.item_review -> {
                val fm: FragmentManager = supportFragmentManager
                val reviewDialog = ReviewFragment()
                val b = Bundle()
                b.putInt("dialog_title", R.string.user_rating)
                b.putInt("dialog_positive", R.string.currency)
                reviewDialog.arguments = b
                reviewDialog.show(fm, "fragment_review")
            }
            // Adds the bar_chat_menu
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    /**
     * Start Lend
     */
    @SuppressLint("SimpleDateFormat")
    private fun startLend(receiver: String, buttonLend: Button) {
        val data = hashMapOf(
            getString(R.string.db__ownerId) to auth.currentUser?.uid.toString(),
            getString(R.string.db__receiverId) to receiver,
            getString(R.string.db__dateStart) to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date()),
            getString(R.string.db__dateEnd) to ""
        )
        db.collection(getString(R.string.db_lend))
            .document(auth.currentUser?.uid.toString() + receiver)
            .set(data as Map<String, Any>)
        lend = true
        buttonLend.text = getString(R.string.end_lend)
    }

    /**
     * End Lend
     */
    private fun endLend(receiver: String, buttonLend: Button) {
        val data = hashMapOf(
            getString(R.string.end_lend) to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
        )

        db.collection(getString(R.string.db_lend))
            .document(auth.currentUser?.uid.toString() + receiver)
            .set(data, SetOptions.merge())
        lend = false
        buttonLend.text = getString(R.string.start_lend)
    }

    @SuppressLint("SimpleDateFormat")
    override fun sendReviewDialog(star: Float) {
        val data = hashMapOf(
            getString(R.string.db__postId) to chat.title,
            getString(R.string.db__reviewer) to auth.currentUser?.uid.toString(),
            getString(R.string.db__datePosted) to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date()),
            getString(R.string.db__stars) to star,
        )
        db.collection(getString(R.string.db_user_reviews)).document(receiver).collection(getString(R.string.db_reviewers))
            .document(UUID.randomUUID().toString())
            .set(data as Map<String, Any>)
    }

    override fun actionApproveDialog(value: Boolean) {
        if (value) {
            if (!lend)
                startLend(receiver, findViewById(R.id.button_startLend))
            else endLend(receiver, findViewById(R.id.button_startLend))
        }
    }
}
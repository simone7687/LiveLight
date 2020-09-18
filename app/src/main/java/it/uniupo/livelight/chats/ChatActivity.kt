package it.uniupo.livelight.chats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private lateinit var receiver: String

    private lateinit var listAdapter: MessagesListAdapter
    private lateinit var list: RecyclerView
    private lateinit var inputMessage: EditText

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

        // Update messages
        list = findViewById(R.id.ListView_message)
        val layoutMgr = LinearLayoutManager(this)
        layoutMgr.stackFromEnd = true
        list.layoutManager = layoutMgr

        listAdapter = MessagesListAdapter(this, mutableListOf())
        list.adapter = listAdapter

        updateMessages(chat.id, receiver)

        inputMessage = findViewById(R.id.txtMessage)

        //Handle send message button click
        findViewById<Button>(R.id.btnSend).setOnClickListener {
            if (inputMessage.text.toString().isNotEmpty()) {
                sendMessage(
                    inputMessage.text.toString(),
                    auth.currentUser?.uid.toString(),
                    receiver,
                    chat.id
                )
                inputMessage.text.clear()
                // TODO: list.scrollToPosition(listAdapter.itemCount)
            }
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
                db.collection(getString(R.string.db_lend))
                    .document(auth.currentUser?.uid.toString() + receiver).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val endDate = task.result?.getString(getString(R.string.end_lend))
                            if (!endDate.isNullOrEmpty()) {
                                val fm: FragmentManager = supportFragmentManager
                                val reviewDialog = ReviewFragment()
                                val b = Bundle()
                                b.putInt("dialog_title", R.string.user_rating)
                                b.putInt("dialog_positive", R.string.currency)
                                reviewDialog.arguments = b
                                reviewDialog.show(fm, "fragment_review")
                            } else {
                                Toast.makeText(
                                    this,
                                    R.string.there_is_already_chat,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
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
    @SuppressLint("SimpleDateFormat")
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
            .collection(getString(R.string.db_messages))
            .document(UUID.randomUUID().toString())
            .set(data as Map<String, Any>)
            .addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Update the messages list
     */
    private fun updateMessages(chat: String, receiver: String) {
        val collection = db.collection(getString(R.string.db_chats)).document(chat)
            .collection(getString(R.string.db_messages))
            .orderBy(getString(R.string.db__dateTime), Query.Direction.DESCENDING)

        collection.addSnapshotListener { snapshots, e ->
            if (snapshots != null && e == null) {
                for (dc in snapshots.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val message = MessageModel(dc.document.id)
                            message.message =
                                dc.document.getString(getString(R.string.db__text)) as String
                            message.dateTime =
                                dc.document.getString(getString(R.string.db__dateTime)) as String
                            message.isSender = (receiver == auth.currentUser?.uid.toString())

                            listAdapter.addMessage(message)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun sendReviewDialog(star: Float) {
        val data = hashMapOf(
            getString(R.string.db__postId) to chat.title,
            getString(R.string.db__reviewer) to auth.currentUser?.uid.toString(),
            getString(R.string.db__datePosted) to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(
                Date()
            ),
            getString(R.string.db__stars) to star,
        )
        db.collection(getString(R.string.db_user_reviews)).document(receiver)
            .collection(getString(R.string.db_reviewers))
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
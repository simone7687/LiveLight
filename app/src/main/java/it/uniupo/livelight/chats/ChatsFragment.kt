package it.uniupo.livelight.chats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R

class ChatsFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chats, container, false)

        getChatsList(root.findViewById(R.id.list_chats))
        /*
        val v = root.findViewById<ListView>(R.id.list_chats)
        if (root.findViewById<ListView>(R.id.list_chats).size == 0) {
            root.findViewById<TextView>(R.id.text_messages).visibility = View.VISIBLE
        }*/

        return root
    }


    /**
     * Gets all chats for the current users
     */
    private fun getChatsList(chatssList: ListView) {
        db.collection(getString(R.string.db_chats))
            .whereArrayContains(getString(R.string.db__usersId), auth.uid.toString()).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result!!.isEmpty)
                        return@addOnCompleteListener

                    val messages = arrayListOf<ChatModel>()
                    for (item in task.result!!.documents) {
                        val message = ChatModel(item.id)
                        message.title = item.getString(getString(R.string.db__title)) as String
                        message.image =
                            item.getString(getString(R.string.db__imageUrl)) as String
                        message.users =
                            item.get(getString(R.string.db__usersId)) as ArrayList<String>
                        message.owner = item.get(getString(R.string.db__ownerId)) as String
                        messages.add(message)
                    }

                    val conversationsAdapter = ChatsListAdapter(requireActivity(), messages)
                    chatssList.adapter = conversationsAdapter
                    chatssList.setOnItemClickListener { parent, view, position, id ->
                        conversationsAdapter.getItem(position)?.let { openChat(it) }
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    activity?.baseContext,
                    exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun openChat(chat: ChatModel) {
        val aa = ChatActivity()
        val intent = Intent(activity, aa::class.java)
        intent.putExtra("chat_id", chat.id)
        if (chat.users[0] == auth.currentUser?.uid.toString())
            intent.putExtra("chat_receiver_user", chat.users[1])
        else
            intent.putExtra("chat_receiver_user", chat.users[0])
        intent.putExtra("chat_title", chat.title)
        startActivity(intent)
    }
}
package it.uniupo.livelight.chats

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.uniupo.livelight.R

/**
 * Adapter of a list of messages
 */
class MessagesListAdapter(
    private val context: Activity,
    private var messages: MutableList<MessageModel>
) : RecyclerView.Adapter<MessagesListAdapter.ViewHolder>() {
    companion object {
        private const val SENT = 0
        private const val RECEIVED = 1
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            SENT -> {
                ViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.message_my, parent, false)
                )
            }
            else -> {
                ViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.message_other, parent, false)
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    // Binds each messages in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(messages[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSender) {
            SENT
        } else {
            RECEIVED
        }
    }

    fun addMessage(message: MessageModel) {
        this.messages.add(message)
        notifyItemInserted(this.messages.size - 1)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textView_message)
        private val dateText: TextView = itemView.findViewById(R.id.textView_time)

        fun setData(message: MessageModel) {
            messageText.text = message.message
            dateText.text = message.dateTime
        }
    }
}
package it.uniupo.livelight.chats

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import it.uniupo.livelight.R

/**
 * Adapter of a list of posts
 */
class MessagesListAdapter(
    private val context: Activity,
    private val message: ArrayList<MessageModel>
) : ArrayAdapter<MessageModel>(context, R.layout.message_my, message) {

    @SuppressLint("InflateParams")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView: View
        if (message[position].isSender) {
            rowView = inflater.inflate(R.layout.message_my, null, true)
        } else {
            rowView = inflater.inflate(R.layout.message_other, null, true)
        }

        val messageText = rowView.findViewById(R.id.textView_message) as TextView
        val timeText = rowView.findViewById(R.id.textView_time) as TextView

        messageText.text = message[position].message
        //timeText.text = message[position].dateTime
        return rowView
    }
}
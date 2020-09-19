package it.uniupo.livelight.chats

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import it.uniupo.livelight.R

/**
 * Adapter of a list of posts
 */
class ChatsListAdapter(
    private val context: Activity,
    private val chat: ArrayList<ChatModel>
) : ArrayAdapter<ChatModel>(context, R.layout.list_post, chat) {

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_post, null, true)

        val titleText = rowView.findViewById(R.id.title_list) as TextView
        val imageView = rowView.findViewById(R.id.icon_list) as ImageView
        val subtitleText = rowView.findViewById(R.id.description_list) as TextView

        titleText.text = chat[position].title
        //subtitleText.text = post[position].description
        val radius = 50
        Glide.with(this.context)
            .load(chat[position].image)
            .placeholder(R.drawable.loading_animation)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(imageView)

        return rowView
    }
}
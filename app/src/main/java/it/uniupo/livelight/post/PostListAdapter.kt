package it.uniupo.livelight.post

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import it.uniupo.livelight.R

/**
 * Adapter of a list of posts
 */
class PostListAdapter(
    private val context: Activity,
    private val title: ArrayList<String>,
    private val description: ArrayList<String>,
    private val image: ArrayList<String>
) : ArrayAdapter<String>(context, R.layout.list_post, title) {

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_post, null, true)

        val titleText = rowView.findViewById(R.id.title_list) as TextView
        val imageView = rowView.findViewById(R.id.icon_list) as ImageView
        val subtitleText = rowView.findViewById(R.id.description_list) as TextView

        titleText.text = title[position]
        subtitleText.text = description[position]
        Picasso.with(context).load(image[position]).placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_twotone_sync_problem_24)
            .transform(CircleTransform()).into(imageView)

        return rowView
    }
}
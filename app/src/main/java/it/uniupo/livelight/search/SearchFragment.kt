package it.uniupo.livelight.search

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import it.uniupo.livelight.R
import it.uniupo.livelight.post.PostListAdapter
import it.uniupo.livelight.post.PostModel
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()

    private var distanceSelected: Int = 0
    var lastLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        root.spinner_distance.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // p2: position
                distanceSelected = p2
                // Last position
                // TODO: Requesting permits
                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    // Got last known location
                    if (location != null) {
                        lastLocation = location
                    } else if (p2 != 0)
                        Toast.makeText(
                            activity?.baseContext, R.string.no_location,
                            Toast.LENGTH_SHORT
                        ).show()
                }
                // Update the list
                if (lastLocation != null)
                    updatePostList(
                        root.findViewById(R.id.list_post),
                        distanceSelected,
                        lastLocation
                    )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        updatePostList(root.findViewById(R.id.list_post), distanceSelected, lastLocation)

        return root
    }

    /**
     * Updates the list of posts according to the selected position
     */
    private fun updatePostList(postList: ListView, distanceSelected: Int, lastLocation: Location?) {
        db.collection(getString(R.string.db_post)).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // for PostListAdapter
                    val titlePost: ArrayList<String> = ArrayList()
                    val descriptionPost: ArrayList<String> = ArrayList()
                    val imagePost: ArrayList<String> = ArrayList()

                    loop@ for (item in task.result!!.documents) {
                        val model = PostModel(item.id)
                        model.user = item.get(getString(R.string.db__userId)) as String
                        model.title = item.get(getString(R.string.db__title)) as String
                        model.description = item.get(getString(R.string.db__description)) as String
                        model.image = item.get(getString(R.string.db__imageUrl)) as String

                        // Find only posts of the selected distance
                        if (distanceSelected != 0 && lastLocation != null) {
                            val itemLoc = Location("")
                            itemLoc.latitude = model.coordinates[0]
                            itemLoc.longitude = model.coordinates[1]

                            when (distanceSelected) {
                                //10km
                                1 -> {
                                    if (lastLocation.distanceTo(itemLoc) > 10000)
                                        continue@loop
                                }
                                //20km
                                2 -> {
                                    if (lastLocation.distanceTo(itemLoc) > 20000)
                                        continue@loop
                                }
                                //50km
                                3 -> {
                                    if (lastLocation.distanceTo(itemLoc) > 50000)
                                        continue@loop
                                }
                            }
                        }

                        // for PostListAdapter
                        titlePost.add(model.title)
                        descriptionPost.add(model.description)
                        imagePost.add(model.image)
                    }

                    // Update the list of posts
                    postList.adapter =
                        PostListAdapter(
                            this.requireActivity(),
                            titlePost,
                            descriptionPost,
                            imagePost
                        )
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    activity?.baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Updates the list of posts based on the selected location and Keywords
     */
    private fun updatePostList(
        postList: ListView,
        distanceSelected: Int,
        lastLocation: Location?,
        text: String
    ) {
        // If it is empty do not search with Keywords
        if (text.isEmpty()) {
            updatePostList(postList, distanceSelected, lastLocation)
            return
        }
        db.collection(getString(R.string.db_post)).whereArrayContains(
            "Keywords", text.toLowerCase(
                Locale.ROOT
            )
        ).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // for PostListAdapter
                    val titlePost: ArrayList<String> = ArrayList()
                    val descriptionPost: ArrayList<String> = ArrayList()
                    val imagePost: ArrayList<String> = ArrayList()

                    loop@ for (item in task.result!!.documents) {
                        val model = PostModel(item.id)
                        model.user = item.get(getString(R.string.db__userId)) as String
                        model.title = item.get(getString(R.string.db__title)) as String
                        model.description = item.get(getString(R.string.db__description)) as String
                        model.image = item.get(getString(R.string.db__imageUrl)) as String

                        // Find only posts of the selected distance
                        if (distanceSelected != 0 && lastLocation != null) {
                            val itemLoc = Location("")
                            itemLoc.latitude = model.coordinates[0]
                            itemLoc.longitude = model.coordinates[1]

                            when (distanceSelected) {
                                //10km
                                1 -> {
                                    if (lastLocation.distanceTo(itemLoc) > 10000)
                                        continue@loop
                                }
                                //20km
                                2 -> {
                                    if (lastLocation.distanceTo(itemLoc) > 20000)
                                        continue@loop
                                }
                                //50km
                                3 -> {
                                    if (lastLocation.distanceTo(itemLoc) > 50000)
                                        continue@loop
                                }
                            }
                        }

                        // for PostListAdapter
                        titlePost.add(model.title)
                        descriptionPost.add(model.description)
                        imagePost.add(model.image)
                    }

                    // Update the list of posts
                    postList.adapter =
                        PostListAdapter(
                            this.requireActivity(),
                            titlePost,
                            descriptionPost,
                            imagePost
                        )
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    activity?.baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
package it.uniupo.livelight.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import it.uniupo.livelight.R
import it.uniupo.livelight.post.PostActivity
import it.uniupo.livelight.post.PostListAdapter
import it.uniupo.livelight.post.PostModel
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * ProfileFragment is a snippet used to search for Post
 */
class SearchFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()

    private val REQUEST_CODE_LOCATION = 300

    private var distanceSelected: Int = 0
    private var lastLocation: Location? = null
    private lateinit var list: ListView
    private var search_text: String? = null
    private var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        list = root.findViewById(R.id.list_post)

        root.spinner_distance.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // p2: position
                distanceSelected = p2
                spinnerDistanceHandler()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                if (lastLocation == null)
                    spinnerDistanceHandler()
            }

        }

        updatePostList(root.findViewById(R.id.list_post), distanceSelected, lastLocation)

        // Lookup the swipe container view
        swipeContainer = root.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
        // Setup refresh listener which triggers new data loading
        swipeContainer!!.setOnRefreshListener {
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            updatePostList(
                list,
                distanceSelected,
                lastLocation,
                search_text
            )
        }
        // Configure the refreshing colors
        swipeContainer!!.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        return root
    }

    /**
     * spinnerDistanceHandler: update the list according to the location
     */
    fun spinnerDistanceHandler() {
        // Requesting permits
        if (distanceSelected != 0) {
            // Requests location permission
            if (checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                requestPermissions(permissions, REQUEST_CODE_LOCATION)
            }
            // Gets last position
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location
                if (location != null) {
                    lastLocation = location
                    updatePostList(
                        list,
                        distanceSelected,
                        lastLocation,
                        search_text
                    )
                } else if (distanceSelected != 0)
                    Toast.makeText(
                        activity?.baseContext, R.string.no_location,
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    /**
     * Open MapActivity
     */
    private fun openMap() {
        val intent = Intent(activity, MapActivity::class.java)
        startActivity(intent)
    }

    /**
     * Updates the list of posts according to the selected position
     */
    private fun updatePostList(
        postList: ListView,
        distanceSelected: Int,
        lastLocation: Location?
    ) {
        db.collection(getString(R.string.db_post)).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updatePostListDistance(
                        postList,
                        task.result,
                        distanceSelected,
                        lastLocation
                    )
                }
            }.addOnFailureListener { exception ->
                swipeContainer?.isRefreshing = false
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
        text: String?
    ) {
        // If it is empty do not search with Keywords
        if (text.isNullOrEmpty()) {
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
                    updatePostListDistance(
                        postList,
                        task.result,
                        distanceSelected,
                        lastLocation
                    )
                }
            }.addOnFailureListener { exception ->
                swipeContainer?.isRefreshing = false
                Toast.makeText(
                    activity?.baseContext, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Function used in updatePostList
     */
    private fun updatePostListDistance(
        postList: ListView,
        result: QuerySnapshot?,
        distanceSelected: Int,
        lastLocation: Location?
    ) {
        // for PostListAdapter
        val posts: ArrayList<PostModel> = ArrayList()

        loop@ for (item in result!!.documents) {
            val model = PostModel(item.id)
            model.user = item.get(getString(R.string.db__userId)) as String
            model.title = item.get(getString(R.string.db__title)) as String
            model.description = item.get(getString(R.string.db__description)) as String
            model.datePosted = item.get(getString(R.string.db__datePosted)) as String
            model.image = item.get(getString(R.string.db__imageUrl)) as String
            model.coordinates =
                item.get(getString(R.string.db__coordinates)) as ArrayList<Double>

            // Find only posts of the selected distance
            if (distanceSelected != 0 && lastLocation != null) {
                val itemLoc = Location("")
                itemLoc.latitude = model.coordinates[0]
                itemLoc.longitude = model.coordinates[1]

                when (distanceSelected) {
                    // 10km
                    1 -> {
                        if (lastLocation.distanceTo(itemLoc) > 10000)
                            continue@loop
                    }
                    // 30km
                    2 -> {
                        if (lastLocation.distanceTo(itemLoc) > 30000)
                            continue@loop
                    }
                    // 50km
                    3 -> {
                        if (lastLocation.distanceTo(itemLoc) > 50000)
                            continue@loop
                    }
                    // 100km
                    3 -> {
                        if (lastLocation.distanceTo(itemLoc) > 100000)
                            continue@loop
                    }
                }
            }

            // for PostListAdapter
            posts.add(model)
        }

        // Update the list of posts
        val postsAdapter = PostListAdapter(this.requireActivity(), posts)
        postList.adapter = postsAdapter
        postList.setOnItemClickListener { parent, view, position, id ->
            postsAdapter.getItem(position)?.let { viewPost(it) }
        }
        swipeContainer?.isRefreshing = false
    }

    fun viewPost(post: PostModel) {
        val aa = PostActivity()
        val intent = Intent(activity, aa::class.java)
        intent.putExtra("post_id", post.id)
        intent.putExtra("post_user", post.user)
        intent.putExtra("post_title", post.title)
        intent.putExtra("post_description", post.description)
        intent.putExtra("post_datePosted", post.datePosted)
        intent.putExtra("post_image", post.image)
        startActivity(intent)
    }
}
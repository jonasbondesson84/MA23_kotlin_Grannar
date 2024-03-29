package com.example.grannar

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class EventFragment : Fragment(), EventAdapter.MyAdapterListener, DistanceSliderListener,
    OnMapReadyCallback, AddEventDialogFragment.OnSaveListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var rvEvents: RecyclerView
    private lateinit var tabEvent: TabLayout
    private var eventList = mutableListOf<Event>()
    private var eventsInRecyclerView = mutableListOf<Event>()
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: EventAdapter
    private lateinit var etvFilterEvent: TextInputEditText
    private lateinit var distanceChip: Chip
    private lateinit var eventMap: MapView
    private var distanceSet: Float = 5f
    private lateinit var googleMap: GoogleMap
    private lateinit var circle: Circle
    private var currentZoomLevel = 10.75f
    private val STARTING_ZOOM = 10.75f
    private var savedEvents = mutableListOf<Event>()
    private lateinit var tvEmptyList: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event, container, false)
        db = Firebase.firestore
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        etvFilterEvent = view.findViewById(R.id.etvSearchEvent)
        distanceChip = view.findViewById(R.id.distanceEventChip)
        rvEvents = view.findViewById(R.id.rvEventsList)
        eventMap = view.findViewById(R.id.eventMapView)
        eventMap.visibility = View.INVISIBLE
        val fabAddEvent: FloatingActionButton = view.findViewById(R.id.fabAddEvent)
        tabEvent = view.findViewById(R.id.tabEvent)
        tvEmptyList = view.findViewById(R.id.tvEmptyEventList)
        eventMap.onCreate(savedInstanceState)
        eventMap.getMapAsync(this)

        getSavedEvents()

        tabEvent.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    when (tab.position) {
                        0 -> {
                            showEventList()
                        }

                        1 -> {
                            if (CurrentUser.userID != null) {
                                showMap()
                            } else {
                                openLogInFragment()
                            }
                        }

                        2 -> {
                            if (CurrentUser.userID != null) {
                                showMyEvents()
                            } else {
                                openLogInFragment()
                            }
                        }

                        else -> {
                        }
                    }

                }
            }

            private fun showMyEvents() {
                rvEvents.visibility = View.VISIBLE
                eventMap.visibility = View.INVISIBLE
                adapter = EventAdapter(view.context, savedEvents, this@EventFragment)
                rvEvents.adapter = adapter
                CurrentUser.tabEventItem = 2
                showEmptyMessage(savedEvents)
            }

            private fun showMap() {
                rvEvents.visibility = View.INVISIBLE
                eventMap.visibility = View.VISIBLE
                CurrentUser.tabEventItem = 1
                tvEmptyList.visibility = View.INVISIBLE
            }

            private fun showEventList() {
                rvEvents.visibility = View.VISIBLE
                eventMap.visibility = View.INVISIBLE
                adapter = EventAdapter(view.context, eventsInRecyclerView, this@EventFragment)
                rvEvents.adapter = adapter
                CurrentUser.tabEventItem = 0
                showEmptyMessage(eventsInRecyclerView)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }


        })


        rvEvents.layoutManager = LinearLayoutManager(view.context)
        rvEvents.addItemDecoration(
            DividerItemDecoration(
                requireContext(), LinearLayoutManager.VERTICAL
            )
        )
        adapter = EventAdapter(view.context, eventsInRecyclerView, this)
        rvEvents.adapter = adapter
        distanceChip.text = "Distance: ${distanceSet.toInt()} km"
        getEventsWithinDistance(distanceSet.toInt())
        addTextChangeListener()

        distanceChip.setOnClickListener {
            val dialogFragment = DistanceMapDialogFragment(distanceSet)
            dialogFragment.setDistanceSliderListener(this)
            dialogFragment.show(parentFragmentManager, "distanceDialogFragment")
        }


        fabAddEvent.setOnClickListener {
            if (CurrentUser.userID != null) {
                addEvent()
            } else {
                openLogInFragment()
            }
        }
        return view
    }


    private fun addEvent() {
        val dialogFragment = AddEventDialogFragment()
        dialogFragment.setOnSuccessListener(this)
        dialogFragment.show(parentFragmentManager, "AddEventDialogFragment")
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun getSavedEvents() {
        savedEvents.clear()
        for (eventID in CurrentUser.savedEvent) {
            db.collection("Events").document(eventID).get()
                .addOnSuccessListener { document ->
                    val newEvent = document.toObject<Event>()
                    if (newEvent != null) {
                        savedEvents.add(newEvent)
                        savedEvents.sortBy { it.startDateTime }

                    }
                }
        }
        db.collection("Events").whereEqualTo("createdByUID", CurrentUser.userID.toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val newEvent = document.toObject<Event>()
                    if (newEvent != null) {
                        savedEvents.add(newEvent)
                        savedEvents.sortBy { it.startDateTime }

                    }
                }
            }
    }

    private fun setMap(map: GoogleMap) {
        googleMap = map
        map.clear()

        val adapter = EventMapInfoAdapter(requireContext())
        map.setInfoWindowAdapter(adapter)
        val userLocation = LatLng(CurrentUser.locLat ?: 59.334591, CurrentUser.locLng ?: 18.063240)
        val circleOptions = CircleOptions()
            .center(userLocation)
            .radius((distanceSet * 1000).toDouble()) // Initial radius in meters (1 km)
            .strokeWidth(2f)
            .strokeColor(Color.RED)
            .fillColor(Color.parseColor("#30FF0000")) // Transparent red color
        circle = map.addCircle(circleOptions)

        for (event in eventList) {
            val lat = event.locLat
            val lng = event.locLng
            if (lat != null && lng != null) {
                val latLng = LatLng(lat, lng)
                val marker =
                    map.addMarker(MarkerOptions().position(latLng).title(event.name))
                marker?.tag = event
            }
        }
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, currentZoomLevel)
        map.moveCamera(cameraUpdate)
        map.setOnInfoWindowClickListener {
            val event = it.tag as? Pair<Event, ConstraintLayout>
            if (event != null) {
                goToEvent(event.first, event.second)
            }

        }

    }

    private fun updateCircleRadius(radiusInMeters: Double) {
        circle.radius = radiusInMeters
        setCameraZoom(radiusInMeters.toFloat())
    }

    private fun setCameraZoom(radiusInKM: Float) {
        // Set to dived by 13 because zoom looks good over Stockholm. Needs to change if we target areas closer to the equator.
        currentZoomLevel = (STARTING_ZOOM - (radiusInKM / 13))
    }

    private fun addEventsToRecycler(listToAdd: List<Event>) {
        eventsInRecyclerView.clear()
        eventsInRecyclerView.addAll(listToAdd)
        adapter.notifyDataSetChanged()
    }

    private fun filterList(textToSearchFor: String) {
        if (textToSearchFor.isNotBlank()) {
            val filteredList = eventList.filter { event ->
                event.name?.lowercase()?.contains(textToSearchFor) ?: false
            }
            addEventsToRecycler(filteredList)
        } else {
            addEventsToRecycler(eventList)
        }
    }


    private fun addTextChangeListener() {
        etvFilterEvent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not using this
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not using this
            }

            override fun afterTextChanged(s: Editable?) {
                val textInSearchField = s.toString().lowercase().trim()
                filterList(textInSearchField)
            }

        })
    }

    private fun openLogInFragment() {
        tabEvent.getTabAt(0)?.select()
        val dialogFragment = SignInDialogFragment()
        dialogFragment.show(parentFragmentManager, "SignInDialogFragment")
    }

    override fun goToEvent(event: Event, card: ConstraintLayout) {
        if (CurrentUser.userID != null) {
            val eventID = event.docID
            if (eventID != null) {
                val extra = FragmentNavigatorExtras(card to eventID)
                val action =
                    EventFragmentDirections.actionEventFragmentToEventInfoFragment(event.docID!!)
                findNavController().navigate(action, extra)
            }
        } else {
            openLogInFragment()
        }
    }

    override fun onDistanceSet(distance: Double) {
        distanceSet = distance.toFloat()
        getEventsWithinDistance(distance.toInt())
        distanceChip.text = "Distance: $distanceSet km"
        updateCircleRadius(distanceSet.toDouble())
    }

    private fun getEventsWithinDistance(distanceInKilometers: Int) {
        val lat = CurrentUser.locLat ?: 59.334591
        val lng = CurrentUser.locLng ?: 18.063240
        val center = GeoLocation(lat, lng)
        val radiusInM = distanceInKilometers * 1000
        val tasks = getListOfDbQueries(radiusInM, center)
        queryForEventsByGeoHash(tasks, center, radiusInM)
    }

    private fun getListOfDbQueries(
        radiusInM: Int,
        center: GeoLocation
    ): MutableList<Task<QuerySnapshot>> {

        // Get all surrounding geo hashes within radius
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM.toDouble())
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        // Query for all users with the same hash as in the list
        for (b in bounds) {
            val query = db.collection("Events")
                .orderBy("geoHash")
                .startAt(b.startHash)
                .endAt(b.endHash)
                .limit(30)
            tasks.add(query.get())
        }
        return tasks
    }

    private fun queryForEventsByGeoHash(
        tasks: MutableList<Task<QuerySnapshot>>,
        center: GeoLocation,
        radiusInM: Int
    ) {
        Tasks.whenAllComplete(tasks)
            .addOnSuccessListener {
                val matchingDocuments: MutableList<DocumentSnapshot> = ArrayList()
                for (task in tasks) {
                    val snapshot = task.result
                    for (doc in snapshot.documents) {
                        val docLat = doc.getDouble("locLat")
                        val docLng = doc.getDouble("locLng")
                        if (docLat != null && docLng != null) {
                            val docLoc = GeoLocation(docLat, docLng)
                            // Remove the false positive, that has the same hash but is still outside of the radius
                            val distanceToUserInM = GeoFireUtils.getDistanceBetween(docLoc, center)
                            if (distanceToUserInM <= radiusInM) {
                                matchingDocuments.add(doc)
                            } else {
                                Log.d("!!!", "False Positive Document!!!")
                            }
                        }
                    }
                }
                createEventsAndFilRecycler(matchingDocuments)
            }
    }

    private fun createEventsAndFilRecycler(matchingDocuments: MutableList<DocumentSnapshot>) {
        val nowTime = Calendar.getInstance().time
        eventList.clear()
        for (document in matchingDocuments) {
            val event = document.toObject<Event>()
            if (event?.startDateTime != null) {
                if (event.startDateTime?.compareTo(nowTime)!! > 0) {
                    eventList.add(event)
                }
            }
        }
        setListInRecyclerView(true)
    }

    private fun showEmptyMessage(eventList: MutableList<Event>) {
        if (eventList.size == 0) {
            tvEmptyList.visibility = View.VISIBLE
        } else {
            tvEmptyList.visibility = View.INVISIBLE
        }
    }

    private fun setListInRecyclerView(categoryFilterChanged: Boolean) {
        eventsInRecyclerView.clear()
        eventsInRecyclerView.addAll(eventList)
        showEmptyMessage(eventsInRecyclerView)
        eventMap.getMapAsync { googleMap ->
            setMap(googleMap)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        tabEvent.getTabAt(CurrentUser.tabEventItem)?.select()
        eventMap.onResume()

    }

    override fun onPause() {
        super.onPause()
        eventMap.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventMap.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        eventMap.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        eventMap.onLowMemory()
    }

    override fun onSuccessPass(success: Boolean) {
        if (success) {
            getEventsWithinDistance(distanceSet.toInt())
            getSavedEvents()
        }
    }

    override fun onDataPass(eventID: String) {

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}
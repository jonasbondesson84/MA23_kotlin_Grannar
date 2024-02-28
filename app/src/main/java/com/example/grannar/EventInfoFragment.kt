package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventInfoFragment : Fragment() {


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val args: EventInfoFragmentArgs by navArgs()
    private lateinit var tvName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvLocation: TextView
    private lateinit var imEventImage: ImageView
    private lateinit var topBar: MaterialToolbar
    private var selectedEvent: Event? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var locationMap: MapView
    private lateinit var googleMap: GoogleMap
    private var isSaved = false

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
        val view = inflater.inflate(R.layout.fragment_event_info, container, false)

        tvName = view.findViewById(R.id.tvEventInfoName)
        tvDate = view.findViewById(R.id.tvEventInfoDate)
        tvDesc = view.findViewById(R.id.tvEventInfoDesc)
        tvLocation = view.findViewById(R.id.tvEventInfoLocation)
        imEventImage = view.findViewById(R.id.imEventInfoImage)
        topBar = view.findViewById(R.id.topEventInfo)
        storage = Firebase.storage
        locationMap = view.findViewById(R.id.eventInfoMapView)

        locationMap.onCreate(savedInstanceState)



        topBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        topBar.setOnMenuItemClickListener {menuItem->
            when(menuItem.itemId) {
                R.id.deleteEvent-> {
                    showDeleteDialog()
                    true
                }
                R.id.saveEvent-> {
                    if(isSaved) {
                        removeEvent()
                    } else {
                        saveEvent()

                    }
                    true
                }
                else -> false
            }
        }

        getEventInfo(args.eventID.toString())


        return view
    }

    private fun removeEvent() {
        db.collection("users").document(CurrentUser.userID.toString())
            .update("savedEvents", FieldValue.arrayRemove(selectedEvent?.docID.toString()))
            .addOnSuccessListener {
//                CurrentUser.savedEvent.remove(selectedEvent?.docID.toString())
                isSaved = false
                updateIcon()
            }
    }

    private fun updateIcon()  {
        if(isSaved) {
            topBar.menu.getItem(1).icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_favorite_24, null)
        } else {
            topBar.menu.getItem(1).icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_favorite_border_24, null)
        }
    }

    private fun saveEvent() {
        db.collection("users").document(CurrentUser.userID.toString())
            .update("savedEvents", FieldValue.arrayUnion(selectedEvent?.docID.toString()))
            .addOnSuccessListener {
//                CurrentUser.savedEvent.add(selectedEvent?.docID.toString())
                isSaved = true
                updateIcon()
                Log.d("!!!", "saved event")
            }
    }

    private fun showDeleteDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Warning")
                .setMessage("Do you want to delete this event?")
                .setNegativeButton("No") { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton("Yes") { dialog, which ->
                    // Respond to positive button press
                    deleteItem()
                }
                .show()
        }
    }

    private fun deleteItem() {
        if(selectedEvent != null) {
            selectedEvent!!.docID?.let { db.collection("Events").document(it)
                .delete()
                .addOnCompleteListener {task->
                    if(task.isSuccessful) {
                        deleteImage()
                    }

            } }

        }
    }

    private fun deleteImage() {
        val image = selectedEvent?.imageURL?.let { storage.getReferenceFromUrl(it) }
        if (image != null) {
            image.delete().addOnSuccessListener {
                findNavController().navigateUp()
            }
        } else {
            findNavController().navigateUp()
        }
    }

    private fun getEventInfo(eventID: String) {
        db.collection("Events").document(eventID).get()
            .addOnSuccessListener {document->
                if(document != null) {
                    val event = document.toObject<Event>()
                    if (event != null) {
                        selectedEvent = event
                        showEventData(event)
                    }
                }

            }
    }

    private fun showEventData(event: Event) {
        tvName.text = event.name
        tvDesc.text = event.description
        val formattedDate =
            event.startDateTime?.let {
                SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(
                    it
                )
            }
        tvDate.text = formattedDate
        tvLocation.text = event.geoHash
        Glide
            .with(requireContext())
            .load(event.imageURL)
            .centerCrop()
            .placeholder(R.drawable.img_album)
            .error(R.drawable.img_album)
            .into(imEventImage)

        if(event.createdByUID != CurrentUser.userID) {
            topBar.menu.getItem(0).isVisible = false
        }
        if(event.docID in CurrentUser.savedEvent) {
            isSaved = true
            topBar.menu.getItem(1).icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_favorite_24, null)
        }
        locationMap.getMapAsync { googleMap ->
            setMap(googleMap, event)
        }

    }

    private fun setMap(googleMap: GoogleMap, event: Event) {
        this.googleMap = googleMap
        val lat = event.locLat ?: 0.0
        val lng = event.locLng ?: 0.0
        val latLng = LatLng(lat, lng)

        val cameraUpdate = latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
        if (cameraUpdate != null) {
            googleMap.moveCamera(cameraUpdate)
            googleMap.addMarker(MarkerOptions().position(latLng))
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
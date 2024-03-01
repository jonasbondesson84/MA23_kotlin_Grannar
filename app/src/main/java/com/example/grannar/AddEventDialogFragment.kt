package com.example.grannar

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventDialogFragment: DialogFragment(), OnMapReadyCallback {
   // private var onSaveEventListener: OnSavedEventListener? = null
    interface OnSaveListener {
        fun onDataPass(eventID: String)
    }

    private var setLocation: LatLng? = null
    private var userLocation: LatLng? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var etvDate: TextInputEditText
    private lateinit var tilDate: TextInputLayout
    private lateinit var etvName: TextInputEditText
    private lateinit var etvDesc: TextInputEditText
    private lateinit var imAddImage: ImageView
    private var dateTime: Date? = null
    private var location = mutableMapOf<String, Any>()
    private var lat: Double? = null
    private var lng: Double? = null
    private var geohash: String? = null
    private var imageUri: Uri? = null
    private var editMode = false
    private var imageChanged = false
    private lateinit var mapView: MapView
    private lateinit var btnAddEvent: Button
    private var dataPassListener: OnSaveListener? = null




    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_fragment_add_event, null)
        db = Firebase.firestore
        storage = Firebase.storage
        etvDate = view.findViewById(R.id.etvEventDate)
        tilDate = view.findViewById(R.id.tilEventDate)
        etvName = view.findViewById(R.id.etvEventName)
        etvDesc = view.findViewById(R.id.etvEventDescription)
        imAddImage = view.findViewById(R.id.imEventAddImage)
        btnAddEvent = view.findViewById(R.id.eventAddButton)

        if(arguments != null) {
            editMode = true
            setData(requireArguments())
        }
        etvDate.apply {
            setOnClickListener {
                showDatePickerDialog()
            }
            isFocusable = false
            isFocusableInTouchMode = false
        }

        imAddImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*" //För image/png bara
                startActivityForResult(it, 0)
            }
        }
//        val userLocationLat = arguments?.getDouble("lat")
//        val userLocationLng = arguments?.getDouble("lng")
//
//        userLocation = userLocationLat?.let { userLocationLng?.let { it1 -> LatLng(it, it1) } }
//        Log.d("!!!",userLocation.toString())
//




        view.findViewById<Button>(R.id.eventCancelButton).setOnClickListener {
            dismiss()
        }

        btnAddEvent.setOnClickListener {
//            if(setLocation != null) {
//                onDataPassListener?.onDataPassed(setLocation!!)
            if(editMode) {
                if(imageChanged) {
                    savePhoto()
                } else {
                    val imageURL = arguments?.getString("imageURL")
                    saveEvent(imageURL)
                }
            } else {
                if (!etvName.text.isNullOrBlank() && !etvDesc.text.isNullOrBlank() && lat != null && dateTime != null) {
                    savePhoto()
                }
                //dismiss()

//            }
            }



        }


        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        mapView = view.findViewById(R.id.dialogEventMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->

            val latLng = com.google.android.gms.maps.model.LatLng(
                59.334591,
                18.063240
            )
            val cameraUpdate = userLocation?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) } ?: CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            googleMap.moveCamera(cameraUpdate)
            var marker: Marker? = null
            if(editMode) {
                lat = arguments?.getDouble("lat")?.toDouble()
                lng = arguments?.getDouble("lng")?.toDouble()
                val savedLatLng = lat?.let {
                    lng?.let { it1 -> LatLng(it, it1) }
                }

                marker = savedLatLng?.let { MarkerOptions().position(it) }
                    ?.let { googleMap.addMarker(it) }
            }

            googleMap.setOnMapClickListener { latLng ->
                marker?.remove()
                marker = googleMap.addMarker(MarkerOptions().position(latLng))
//                setLocation = latLng
                 lat = latLng.latitude
                 lng = latLng.longitude
                 geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(latLng.latitude, latLng.longitude))
//
//                location = mutableMapOf(
//                    "geohash" to hash,
//                    "lat" to lat,
//                    "lng" to lng
//                )

            }
        }

        return builder.create()


    }

    private fun setData(arguments: Bundle) {
        btnAddEvent.text = "Save"
        etvName.setText(arguments.getString("name"))
        etvDesc.setText(arguments.getString("desc"))
        dateTime = arguments.getSerializable("startDateTime") as Date?
        val formattedDate =
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(dateTime)
        etvDate.setText(formattedDate)
        val imageURL = arguments.getString("imageURL")
        Glide
            .with(requireContext())
            .load(imageURL)
            .error(R.drawable.baseline_add_photo_alternate_24)
            .into(imAddImage)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 0) {

            val uri = data?.data
            if (uri != null) {

                imAddImage.setImageURI(uri)

                imageUri = uri // spara för uppladdning
                imageChanged = true

            }
        } else {
            Log.d("&&&", "OnActivityResult(), imageUri is null")
        }
    }

    private fun savePhoto() {

            if (imageUri != null) { //If you have selected an image, if first saves the image to firebase.storage, then saves the post in firebase
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val filePath = imageUri
                val storageRef = storage.reference.child("images/events").child(fileName)

                filePath?.let { storageRef.putFile(it) }?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            saveEvent(downloadUrl)

                        }
                    } else {
                        // Image upload failed
                        Toast.makeText(requireContext(), "Error uploading file", Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            } else { //if you haven't selected an image you just saves the place to firebase
                saveEvent(null)
            }


    }


    private fun saveEvent(imageURL: String?) {

        val event = Event(
            name = etvName.text.toString(),
            description = etvDesc.text.toString(),
            location = location,
            geoHash = geohash,
            locLng = lng,
            locLat = lat,
            startDateTime = dateTime,
            createdByUID = CurrentUser.userID.toString(),
            imageURL = imageURL
        )
        if(editMode) {

            val docID = arguments?.getString("docID")
            Log.d("!!!", "docID: $docID")
            if (docID != null) {
                db.collection("Events").document(docID).update(
                    "name", event.name,
                "description", event.description,
                    "locLng", lng,
                    "locLat", lat,
                    "startDateTime", dateTime,
                    "geoHash", geohash,
                    "imageURL", imageURL)
                    .addOnSuccessListener {
                        Log.d("!!!", "event updated")
                        //update infofragment
                        dataPassListener?.onDataPass(docID)
                        dismiss()
                    }
            }
        }else {
            db.collection("Events").add(event).addOnSuccessListener {
                // onSaveEventListener?.onDataPassed(event)
                dismiss()

            }
        }
    }
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()


        val defaultDateTimestamp = calendar.timeInMillis

        val calenderConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())


        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Date of event")
                .setCalendarConstraints(calenderConstraints.build())
                .setSelection(defaultDateTimestamp)
                .build()

        datePicker.show(parentFragmentManager, "datePicker");
        datePicker.addOnPositiveButtonClickListener {
            if (datePicker.selection != null) {
                calendar.time = Date(datePicker.selection as Long)
                showTimePickerDialog(calendar)

            }
        }

    }

    private fun showTimePickerDialog(calendar: Calendar) {
        val isSystem24Hour = is24HourFormat(requireContext())
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(12)
            .setMinute(10)
            .setTitleText("Event start time")
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()

        timePicker.show(parentFragmentManager, "showTimePickerDialog")

        timePicker.addOnPositiveButtonClickListener {


            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            dateTime = calendar.time

            val formattedDate =
                SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(calendar.time)
            etvDate.setText(formattedDate)
        }


    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnSaveListener) {
            dataPassListener = context
        } else {
            Log.d("!!!", "nope")
        }
    }
    fun setOnDataPassListener(listener: OnSaveListener) {
        dataPassListener = listener
    }
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnSavedEventListener) {
//            onSaveEventListener = context
//        } else {
//            throw RuntimeException("$context must implement OnDataPassListener")
//        }
//    }
//    override fun onDetach() {
//        super.onDetach()
//        onSaveEventListener = null // Avregistrera lyssnare för att undvika minnesläckor
//    }

}
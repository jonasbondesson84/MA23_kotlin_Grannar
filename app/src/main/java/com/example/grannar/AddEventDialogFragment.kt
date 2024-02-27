package com.example.grannar

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
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
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
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

class AddEventDialogFragment: DialogFragment() {
//    private var onDataPassListener: OnDataPassListener? = null

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

        view.findViewById<Button>(R.id.eventAddButton).setOnClickListener {
//            if(setLocation != null) {
//                onDataPassListener?.onDataPassed(setLocation!!)
                if( !etvName.text.isNullOrBlank() && !etvDesc.text.isNullOrBlank() && lat != null && dateTime != null) {
                   savePhoto()
               }
                //dismiss()

//            }




        }


        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        val map = view.findViewById<MapView>(R.id.dialogEventMapView)
        map.onCreate(savedInstanceState)
        map.getMapAsync { googleMap ->

            val latLng = com.google.android.gms.maps.model.LatLng(
                59.334591,
                18.063240
            )
            val cameraUpdate = userLocation?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) } ?: CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            googleMap.moveCamera(cameraUpdate)
            var marker: Marker? = null

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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 0) {

            val uri = data?.data
            if (uri != null) {

                imAddImage.setImageURI(uri)

                imageUri = uri // spara för uppladdning

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
                        Toast.makeText(requireContext(),"Error uploading file", Toast.LENGTH_SHORT).show()

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
        db.collection("Events").add(event).addOnSuccessListener {
            dismiss()

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
}
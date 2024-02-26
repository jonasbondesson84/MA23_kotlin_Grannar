package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventDialogFragment: DialogFragment() {
//    private var onDataPassListener: OnDataPassListener? = null

    private var setLocation: LatLng? = null
    private var userLocation: LatLng? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var etvDate: TextInputEditText
    private lateinit var tilDate: TextInputLayout
    private lateinit var etvName: TextInputEditText
    private lateinit var etvDesc: TextInputEditText
    private var dateTime: Date? = null
    private var location = mutableMapOf<String, Any>()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_fragment_add_event, null)
        db = Firebase.firestore
        etvDate = view.findViewById(R.id.etvEventDate)
        tilDate = view.findViewById(R.id.tilEventDate)
        etvName = view.findViewById(R.id.etvEventName)
        etvDesc = view.findViewById(R.id.etvEventDescription)
        etvDate.apply {
            setOnClickListener {
                showDatePickerDialog()
            }
            isFocusable = false
            isFocusableInTouchMode = false
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
                if( !etvName.text.isNullOrBlank() && !etvDesc.text.isNullOrBlank() && location.isNotEmpty() && dateTime != null) {

                    saveEvent()
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
                val lat = latLng.latitude
                val lng = latLng.longitude
                val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng))

                location = mutableMapOf(
                    "geohash" to hash,
                    "lat" to lat,
                    "lng" to lng
                )

            }
        }

        return builder.create()


    }


    private fun saveEvent() {
        val event = Event(
            name = etvName.text.toString(),
            description = etvDesc.text.toString(),
            location = location,
            startDateTime = dateTime,
            createdByUID = CurrentUser.userID.toString()
        )
        db.collection("Events").add(event).addOnSuccessListener {
            dismiss()
        }
    }
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()


        val defaultDateTimestamp = calendar.timeInMillis

        val calenderConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())


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
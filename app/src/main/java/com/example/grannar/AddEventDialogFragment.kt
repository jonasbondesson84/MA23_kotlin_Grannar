package com.example.grannar

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
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

interface OnGetLocationPassListener {
    fun onLocationPassed(data: LatLng)
}

class AddEventDialogFragment : DialogFragment(), OnGetLocationPassListener {
    interface OnEditListener {
        fun onDataPass(eventID: String)
    }

    interface OnSaveListener : OnEditListener {
        fun onSuccessPass(success: Boolean)

    }

    private var dateTime: Date? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private var geohash: String? = null
    private var imageUri: Uri? = null
    private var editMode = false
    private var imageChanged = false

    private var dataPassListener: OnEditListener? = null
    private var successPassListener: OnSaveListener? = null
    private lateinit var db: FirebaseFirestore

    private lateinit var storage: FirebaseStorage
    private lateinit var etvDate: TextInputEditText
    private lateinit var tilDate: TextInputLayout
    private lateinit var etvName: TextInputEditText
    private lateinit var etvDesc: TextInputEditText
    private lateinit var imAddImage: ImageView
    private lateinit var btnAddEvent: Button
    private lateinit var btnCancel: Button
    private lateinit var btnLocation: ImageButton
    private lateinit var imNameCheck: ImageView
    private lateinit var imDescCheck: ImageView
    private lateinit var imDateCheck: ImageView
    private lateinit var imLocCheck: ImageView

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            val name = etvName.text.toString()
            val desc = etvDesc.text.toString()
            val date = etvDate.text.toString()
            if (name.isNotEmpty()) {
                imNameCheck.visibility = View.VISIBLE
            } else {
                imNameCheck.visibility = View.INVISIBLE
            }
            if (desc.isNotEmpty()) {
                imDescCheck.visibility = View.VISIBLE
            } else {
                imDescCheck.visibility = View.INVISIBLE
            }
            if (date.isNotEmpty()) {
                imDateCheck.visibility = View.VISIBLE
            } else {
                imDateCheck.visibility = View.INVISIBLE
            }
            btnAddEvent.isEnabled = checkIfAllDataIsCorrect()
        }

        override fun afterTextChanged(s: Editable?) {}

    }

    private fun checkIfAllDataIsCorrect(): Boolean {
        return (!etvName.text.isNullOrBlank() &&
                !etvDesc.text.isNullOrBlank() &&
                dateTime != null &&
                lat != null &&
                lng != null
                )
    }

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
        btnLocation = view.findViewById(R.id.addEventLocationButton)
        imNameCheck = view.findViewById(R.id.imNameCheck)
        imDescCheck = view.findViewById(R.id.imDescCheck)
        imDateCheck = view.findViewById(R.id.imDateCheck)
        imLocCheck = view.findViewById(R.id.imLocCheck)
        btnCancel = view.findViewById(R.id.eventCancelButton)

        etvName.addTextChangedListener(textWatcher)
        etvDate.addTextChangedListener(textWatcher)
        etvDesc.addTextChangedListener(textWatcher)

        btnLocation.setOnClickListener {
            showMapDialogFragment()
        }

        if (arguments != null) {
            editMode = true
            setData(requireArguments())
        } else {
            editMode = false
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

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnAddEvent.setOnClickListener {
            addEvent()

        }
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)

        return builder.create()
    }

    private fun addEvent() {
        if (editMode) {
            if (imageChanged) {
                savePhoto()
            } else {
                val imageURL = arguments?.getString("imageURL")
                saveEvent(imageURL)
            }
        } else {
            if (!etvName.text.isNullOrBlank() &&
                !etvDesc.text.isNullOrBlank() &&
                lat != null &&
                dateTime != null
            ) {
                savePhoto()
            }
        }
    }

    private fun showMapDialogFragment() {
        val dialogFragment = MapDialogFragment()
        val args = Bundle()
        CurrentUser.locLat?.let { args.putDouble("lat", it) }
        CurrentUser.locLng?.let { args.putDouble("lng", it) }
        args.putBoolean("editMode", true)
        dialogFragment.arguments = args
        dialogFragment.setOnLocationPassListener(this)
        dialogFragment.show(childFragmentManager, "MapDialogFragment")
    }

    private fun setData(arguments: Bundle) {
        btnAddEvent.text = "Save"
        etvName.setText(arguments.getString("name"))
        etvDesc.setText(arguments.getString("desc"))
        dateTime = arguments.getSerializable("startDateTime") as Date?
        val formattedDate =
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(dateTime)
        etvDate.setText(formattedDate)
        lat = arguments.getDouble("lat")
        lng = arguments.getDouble("lng")
        if (lat != null) {
            imLocCheck.visibility = View.VISIBLE
        }
        btnAddEvent.isEnabled = checkIfAllDataIsCorrect()
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
            geoHash = geohash,
            locLng = lng,
            locLat = lat,
            startDateTime = dateTime,
            createdByUID = CurrentUser.userID.toString(),
            imageURL = imageURL
        )
        if (editMode) {
            val docID = arguments?.getString("docID")
            if (docID != null) {
                db.collection("Events").document(docID).update(
                    "name", event.name,
                    "description", event.description,
                    "locLng", lng,
                    "locLat", lat,
                    "startDateTime", dateTime,
                    "geoHash", geohash,
                    "imageURL", imageURL
                )
                    .addOnSuccessListener {
                        dataPassListener?.onDataPass(docID)
                        dismiss()
                    }
            }
        } else {
            db.collection("Events").add(event).addOnSuccessListener {
                successPassListener?.onSuccessPass(true)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnEditListener) {
            dataPassListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        dataPassListener = null
        successPassListener = null
    }

    fun setOnDataPassListener(listener: OnEditListener) {
        dataPassListener = listener
    }

    fun setOnSuccessListener(listener: OnSaveListener) {
        successPassListener = listener
    }

    override fun onLocationPassed(data: LatLng) {
        lat = data.latitude
        lng = data.longitude
        btnAddEvent.isEnabled = checkIfAllDataIsCorrect()
        imLocCheck.visibility = View.VISIBLE
    }

}
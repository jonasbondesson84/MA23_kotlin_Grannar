package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
interface OnDataEditPassListener{
    fun onDataPassed(data: LatLng)
}
class EditProfileDialogFragment : DialogFragment(), OnDataEditPassListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    lateinit var firstNameEditText: TextView
    lateinit var surNameEditText: EditText
    lateinit var genderRadioGroup: RadioGroup
    lateinit var birthdayEditText: EditText
    var birthDate: Date? = null
    private var location: LatLng? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null
    lateinit var locationImageButton: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView: View =
            inflater.inflate(R.layout.dialog_fragment_edit_profile, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        genderRadioGroup = rootView.findViewById(R.id.genderRadioGroup)
        firstNameEditText = rootView.findViewById(R.id.firstNameEditText)
        surNameEditText = rootView.findViewById(R.id.surnameEditText)
        locationImageButton = rootView.findViewById<ImageButton>(R.id.locationImageButton)
        birthdayEditText = rootView.findViewById(R.id.birthDateEditText)
        birthdayEditText.apply {
            setOnClickListener {
                showDatePickerDialog()
            }
            isFocusable = false
            isFocusableInTouchMode = false
        }

        rootView.findViewById<ImageButton>(R.id.locationImageButton).setOnClickListener {
            Log.d("!!!", "Button clicked")
            showMapDialogFragment()

        }

        rootView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }


        rootView.findViewById<Button>(R.id.saveButton).setOnClickListener {
            Log.d("!!!", "Savebutton clicked")
            updateUserInformation()
        }



        return rootView

    }

    private fun updateUserInformation() {
        Log.d("!!!", "fun updateUserInfo")

        val userID = CurrentUser.userID
        if (userID != null) {

        val db = Firebase.firestore
        val userRef = db.collection("users").document(userID)

        val firstName = firstNameEditText.text.toString()
        val surname = surNameEditText.text.toString()
        val age = birthdayEditText.text?.toString() ?: ""
        val genderRadioGroup = genderRadioGroup
        val checkedRadioButtonId = genderRadioGroup.checkedRadioButtonId
        val gender = when (checkedRadioButtonId) {
            R.id.radioButtonMale -> "Male"
            R.id.radioButtonFemale -> "Female"
            R.id.radioButtonNonBinary -> "Non-Binary"
            else -> null
        }
            val userUpdates = mutableMapOf<String, Any>()
            if (firstName.isNotBlank()) userUpdates["firstName"] = firstName
            if (surname.isNotBlank()) userUpdates["surname"] = surname
            if (age.isNotBlank()) userUpdates["age"] = age
            if (gender != null) userUpdates["gender"] = gender
            if (lat != null) userUpdates["locLat"] = lat!!
            if (lng != null) userUpdates["locLng"] = lng!!

            if (userUpdates.isNotEmpty()) {
                userRef.update(userUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dismiss()
                    } else {
                        Toast.makeText(context, "Failed to update user information", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("!!!", "No new information provided")

            }
        }
    }
    private fun showMapDialogFragment() {
        Log.d("!!!", "the")
        val dialogFragment = MapDialogFragment()
        val args = Bundle()
        CurrentUser.locLat?.let { args.putDouble("lat", it) }
        CurrentUser.locLng?.let { args.putDouble("lng", it) }
        args.putBoolean("editMode", true)
        dialogFragment.arguments = args
        dialogFragment.setOnDataEditPassListener(this)
        dialogFragment.show(childFragmentManager, "MapDialogFragment")
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        if (birthDate == null) {

        }
        val defaultDateTimestamp = birthDate?.time ?: calendar.timeInMillis

        val calenderConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())


        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Birthdate")
                .setCalendarConstraints(calenderConstraints.build())
                .setSelection(defaultDateTimestamp)
                .build()

        datePicker.show(childFragmentManager, "datePicker");
        datePicker.addOnPositiveButtonClickListener {
            if (datePicker.selection != null) {
                val selectedDate = Date(datePicker.selection as Long)
                val formattedDate =
                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDate)
                birthdayEditText.setText(formattedDate)
                birthDate = selectedDate
            }
        }

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfileDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDataPassed(data: LatLng) {
        lat = data.latitude
        lng = data.longitude
    }
}
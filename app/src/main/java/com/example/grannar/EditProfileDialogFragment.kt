package com.example.grannar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
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
 */
class EditProfileDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    lateinit var firstNameEditText: EditText
    lateinit var surNameEditText: EditText
    lateinit var genderRadioGroup: RadioGroup
    lateinit var birthdayEditText: EditText
    var birthDate: Date? = null
    private var location: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null


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

        val rootView: View = inflater.inflate(R.layout.dialog_fragment_edit_profile, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        genderRadioGroup = rootView.findViewById(R.id.genderRadioGroup)


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
//            mapFragment?.let {
//                it.showMapForLocationSelection()
//            }
            //open mapdialog
            //check for lastLocation()
            getPermission()


        }

        rootView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            onDismiss()
        }


        rootView.findViewById<Button>(R.id.saveButton).setOnClickListener {
            updateUserInformation()
        }



        return rootView

    }


    private fun updateUserInformation() {
        val userID = "${CurrentUser.userID}"

        val user = User(
            userID = userID,
            firstName = firstNameEditText.text.toString(),
            surname = surNameEditText.text.toString(),
            age = calculateAge(birthDate),
            gender = getGender(),
            location = location,
        )

        val db = Firebase.firestore
        val userRef = db.collection("users").document(userID)

        userRef.set(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dismiss()
            } else {
                Toast.makeText(context, "Failed to update user information", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }



    private fun onDismiss() {
        dismiss()
    }

    private fun calculateAge(birthDate: Date?): String? {

        showDatePickerDialog()

        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd",Locale.getDefault())
        val birthDateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(birthDate)
        val localDate = LocalDate.parse(birthDateStr, formatter)
        val today = LocalDate.now()
        val age = Period.between(localDate, today).years
        return age.toString()
    }

        private fun getPermission() {
            Log.d("!!!", "getPermisson()")
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("!!!", "${requireContext()},")
                getLastLocation()
                Log.d("!!!", "${getLastLocation()},")
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1
                )
                Log.d("!!!", "${requireActivity()},")
            }
        }


            override fun onRequestPermissionsResult(
                requestCode: Int,
                permissions: Array<out String>,
                grantResults: IntArray
            ) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (requestCode == 1) {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getLastLocation()
                    }
                }
            }

            @SuppressLint("MissingPermission")
            private fun getLastLocation() {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    userLocation = LatLng(location.latitude, location.longitude)
                    openMapFragment()

                }
            }

            private fun openMapFragment() {
                val dialogFragment = MapDialogFragment()
                val args = Bundle()
                userLocation?.let { args.putDouble("lat", it.latitude) }
                userLocation?.let { args.putDouble("lng", it.longitude) }
                dialogFragment.arguments = args

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


            private fun getGender(): String {
                val pickedRadioButtonID = genderRadioGroup.checkedRadioButtonId
                return view?.findViewById<RadioButton>(pickedRadioButtonID)?.text.toString()
            }

     fun onDataPassed(data: LatLng) {
        location = data
        Log.d("!!!","Data från dialogfragment ${location}")
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
}
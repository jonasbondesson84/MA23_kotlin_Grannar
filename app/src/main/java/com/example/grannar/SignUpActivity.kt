package com.example.grannar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
interface OnDataPassListener{
    fun onDataPassed(data: LatLng)
}
class SignUpActivity : AppCompatActivity(), OnDataPassListener {
    lateinit var firstNameEditText: EditText
    lateinit var surNameEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var confirmPasswordEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var genderRadioGroup: RadioGroup
    lateinit var birthdayEditText: EditText
    var birthDate: Date? = null
    //var mapFragment: MapFragment? = null
    private var location: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        firstNameEditText = findViewById(R.id.firstNameEditText)
        surNameEditText = findViewById(R.id.surnameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        emailEditText = findViewById(R.id.emailEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)

        //mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as MapFragment?


        birthdayEditText = findViewById(R.id.birthDateEditText)
        birthdayEditText.apply {
            setOnClickListener {
                showDatePickerDialog()
            }
            isFocusable = false
            isFocusableInTouchMode = false
        }

        findViewById<ImageButton>(R.id.locationImageButton).setOnClickListener{
            Log.d("!!!", "Button clicked")
//            mapFragment?.let {
//                it.showMapForLocationSelection()
//            }
            //open mapdialog
            //check for lastLocation()
            getPermission()


        }

        findViewById<Button>(R.id.cancelSignUpButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmationPassword = confirmPasswordEditText.text.toString()

            if (checkInformation()) {
                if (checkPassword(password, confirmationPassword)) {
                    signUp()
                } else {
                    Snackbar.make(
                        confirmPasswordEditText,
                        "Passwords don't match",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    confirmPasswordEditText,
                    "Please fill out all fields",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {location->
            userLocation = LatLng(location.latitude, location.longitude)
            showMapDialogFragment()
            
        }
    }

    private fun showMapDialogFragment() {

        val dialogFragment = MapDialogFragment()
        val args = Bundle()
        userLocation?.let { args.putDouble("lat", it.latitude) }
        userLocation?.let { args.putDouble("lng", it.longitude) }
        dialogFragment.arguments = args

        dialogFragment.show(supportFragmentManager, "MapDialogFragment")
    }




    private fun signUp() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val password = passwordEditText.text.toString()
        val email = emailEditText.text.toString()
        val gender = getGender()
        val birthDateString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(birthDate)
        val lat = location?.latitude
        val lng = location?.longitude
        var geoHash: String? =  null
        if (lat != null && lng != null){
            geoHash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng))
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { signUp ->
            if (signUp.isSuccessful) {
                val newUser = User(
                    docID = "",
                    userID = auth.currentUser?.uid,
                    //location = location,
                    locLat = location?.latitude,
                    locLng = location?.longitude,
                    geoHash = geoHash,
                    firstName = firstNameEditText.text.toString(),
                    surname = surNameEditText.text.toString(),
                    email = email,
                    gender = gender,
                    age = birthDateString
                )
                CurrentUser.setUser(newUser)
                newUser.userID?.let { uid ->
                    db.collection("users").document(uid).set(newUser).addOnSuccessListener {

                        val resultIntent = Intent()
                        resultIntent.putExtra("signed_up", true)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        }
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

        datePicker.show(supportFragmentManager, "datePicker");
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
        return findViewById<RadioButton>(pickedRadioButtonID).text.toString()
    }

    private fun checkInformation(): Boolean {
        if (
            firstNameEditText.text.isEmpty() ||
            surNameEditText.text.isEmpty() ||
            passwordEditText.text.isEmpty() ||
            confirmPasswordEditText.text.isEmpty() ||
            emailEditText.text.isEmpty() ||
            genderRadioGroup.checkedRadioButtonId == -1 ||
            birthDate == null ||
            location == null

        ) {
            return false
        }
        return true
    }

    private fun checkPassword(password: String, confirmationPassword: String): Boolean {
        return password == confirmationPassword
    }

    override fun onDataPassed(data: LatLng) {
        location = data
        Log.d("!!!","Data från dialogfragment ${location}")
    }


}
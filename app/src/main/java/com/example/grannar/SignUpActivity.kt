package com.example.grannar

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var emailTextLayout: TextInputLayout
    private lateinit var confPWTextLayout: TextInputLayout
    private lateinit var passwordTextLayout: TextInputLayout
    private lateinit var imFirstNameCheck: ImageView
    private lateinit var imSurNameCheck: ImageView
    private lateinit var imLocationCheck: ImageView
    private lateinit var imBirthdayCheck: ImageView
    private lateinit var imGenderCheck: ImageView
    private lateinit var imEmailCheck: ImageView
    private lateinit var imPasswordCheck: ImageView
    private lateinit var imConfirmPWCheck: ImageView
    private lateinit var btnSignUp: Button

    var birthDate: Date? = null
    //var mapFragment: MapFragment? = null
    private var location: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            val firstName = firstNameEditText.text.toString()
            val surName = surNameEditText.text.toString()

            if(firstName.length >2) {
                imFirstNameCheck.visibility = View.VISIBLE
            } else {
                imFirstNameCheck.visibility = View.INVISIBLE
            }
            if(surName.length >2) {
                imSurNameCheck.visibility = View.VISIBLE
            } else {
                imSurNameCheck.visibility = View.INVISIBLE
            }


            btnSignUp.isEnabled = checkIfAllDataIsCorrect()

        }

        override fun afterTextChanged(s: Editable?) {}

    }

    private fun checkIfAllDataIsCorrect(): Boolean {

        return (firstNameEditText.text.length > 2 &&
                surNameEditText.text.length > 2 &&
                location != null &&
                birthDate != null &&
                genderRadioGroup.checkedRadioButtonId != -1 &&
                emailTextLayout.error == null &&
                emailEditText.text.isNotEmpty() &&
                passwordTextLayout.error == null &&
                passwordEditText.text.isNotEmpty() &&
                confPWTextLayout.error == null &&
                confirmPasswordEditText.text.isNotEmpty()

                )
    }

    private val textWatcherEmail = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val email = emailEditText.text.toString()
            if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailTextLayout.error = null
                imEmailCheck.visibility = View.VISIBLE
            } else {
                emailTextLayout.error = "No email-address"
                imEmailCheck.visibility = View.INVISIBLE
            }

            btnSignUp.isEnabled = checkIfAllDataIsCorrect()
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }
    private val textWatcherPassword = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val password = passwordEditText.text.toString()
            val confirmationPassword = confirmPasswordEditText.text.toString()
            if(password.length > 7) {
                passwordTextLayout.error = null
                imPasswordCheck.visibility = View.VISIBLE
            } else {
                passwordTextLayout.error = "Password must be at least 8 characters long"
                imPasswordCheck.visibility = View.INVISIBLE
            }
            if(password == confirmationPassword && confirmationPassword.length > 7) {
                confPWTextLayout.error = null
                imConfirmPWCheck.visibility = View.VISIBLE
            } else {
                confPWTextLayout.error = "Password doesnt match"
                imConfirmPWCheck.visibility = View.INVISIBLE
            }

            btnSignUp.isEnabled = checkIfAllDataIsCorrect()
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }




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
        birthdayEditText = findViewById(R.id.birthDateEditText)

        emailTextLayout = findViewById(R.id.emailTextInputLayout)
        confPWTextLayout = findViewById(R.id.confirmPasswordTextInputLayout)
        passwordTextLayout = findViewById(R.id.passwordTextInputLayout)

        imFirstNameCheck = findViewById(R.id.imFirstNameCheck)
        imSurNameCheck = findViewById(R.id.imSurNameCheck)
        imLocationCheck = findViewById(R.id.imLocationCheck)
        imBirthdayCheck = findViewById(R.id.imBirthdayCheck)
        imGenderCheck = findViewById(R.id.imGenderCheck)
        imEmailCheck = findViewById(R.id.imEmailCheck)
        imPasswordCheck = findViewById(R.id.imPasswordCheck)
        imConfirmPWCheck = findViewById(R.id.imConfirmPasswordCheck)

        firstNameEditText.addTextChangedListener(textWatcher)
        surNameEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcherPassword)
        confirmPasswordEditText.addTextChangedListener(textWatcherPassword)
        emailEditText.addTextChangedListener(textWatcherEmail)
        birthdayEditText.addTextChangedListener(textWatcher)

        btnSignUp = findViewById(R.id.signUpButton)
        //mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as MapFragment?



        genderRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("!!!", "what")
            imGenderCheck.visibility = View.VISIBLE
            btnSignUp.isEnabled = checkIfAllDataIsCorrect()
        }

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
            signUp()
//            val password = passwordEditText.text.toString()
//            val confirmationPassword = confirmPasswordEditText.text.toString()
//
//            if (checkInformation()) {
//                if (checkPassword(password, confirmationPassword)) {
//                    signUp()
//                } else {
//                    Snackbar.make(
//                        confirmPasswordEditText,
//                        "Passwords don't match",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                }
//            } else {
//                Snackbar.make(
//                    confirmPasswordEditText,
//                    "Please fill out all fields",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//            }
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
            }else {
                userLocation = LatLng(
                    59.334591,
                    18.063240
                )
                showMapDialogFragment()
            }
        }
    }


    private fun getLastLocation() {
    if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            userLocation = LatLng(location.latitude, location.longitude)
            showMapDialogFragment()

        }
    }
    }

    private fun showMapDialogFragment() {
Log.d("!!!", "the")
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
                imBirthdayCheck.visibility = View.VISIBLE
                btnSignUp.isEnabled = checkIfAllDataIsCorrect()
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
        imLocationCheck.visibility = View.VISIBLE
        btnSignUp.isEnabled = checkIfAllDataIsCorrect()
        Log.d("!!!","Data från dialogfragment ${location}")
    }


}
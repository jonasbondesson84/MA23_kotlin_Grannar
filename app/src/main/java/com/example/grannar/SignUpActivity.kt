package com.example.grannar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    lateinit var firstNameEditText: EditText
    lateinit var surNameEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var confirmPasswordEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var genderRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firstNameEditText = findViewById(R.id.firstNameEditText)
        surNameEditText = findViewById(R.id.surnameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        emailEditText = findViewById(R.id.emailEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        findViewById<Button>(R.id.cancelSignUpButton).setOnClickListener{
            finish()
        }

        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmationPassword = confirmPasswordEditText.text.toString()

            if (checkInformation()){
                if (checkPassword(password, confirmationPassword)){
                    signUp()
                }
                else{
                    Snackbar.make(confirmPasswordEditText, "Passwords don't match", Snackbar.LENGTH_SHORT).show()
                }

            }else{
                Snackbar.make(confirmPasswordEditText, "Please fill out all fields", Snackbar.LENGTH_SHORT).show()

            }
        }

    }

    private fun signUp(){
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val password = passwordEditText.text.toString()
        val email = emailEditText.text.toString()
        val gender = getGender()

        auth.createUserWithEmailAndPassword(email, password). addOnCompleteListener {signUp ->
            if (signUp.isSuccessful){
                val newUser = User(
                    userID = auth.currentUser?.uid,
                    firstName = firstNameEditText.text.toString(),
                    surname = surNameEditText.text.toString(),
                    email = email,
                    gender = gender
                )
                newUser.userID?.let {uid ->
                    db.collection("users").document(uid).set(newUser).addOnSuccessListener {
                        finish()
                    }
                }
            }
        }
    }

    private fun getGender(): String {
        val pickedRadioButtonID = genderRadioGroup.checkedRadioButtonId
        return findViewById<RadioButton>(pickedRadioButtonID).text.toString()
    }

    private fun checkInformation(): Boolean{
        if (firstNameEditText.text.isEmpty() || surNameEditText.text.isEmpty() ||
            passwordEditText.text.isEmpty() || confirmPasswordEditText.text.isEmpty() ||
            emailEditText.text.isEmpty() || genderRadioGroup.checkedRadioButtonId == -1
            ){
            return false
        }
        return true
    }

    private fun checkPassword(password: String, confirmationPassword: String ): Boolean{
        return password == confirmationPassword
    }


}
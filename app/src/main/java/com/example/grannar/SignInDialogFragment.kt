package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class SignInDialogFragment() : DialogFragment() {

    private lateinit var emailEditText: EditText
    private lateinit var emailTextLayout: TextInputLayout
    private lateinit var passwordEditText: EditText
    private lateinit var signInResultListener: SignInResultListener
    private lateinit var signInButton: Button
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signInButton.isEnabled =
                Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isNotEmpty()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailTextLayout.error = null
            } else {
                emailTextLayout.error = "No email-address"
            }
        }

        override fun afterTextChanged(s: Editable?) {}

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        signInResultListener = requireActivity() as SignInResultListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.sign_in_dialog_fragment, null)
        emailEditText = view.findViewById(R.id.signInEmailEditText)
        passwordEditText = view.findViewById(R.id.signInPasswordEditText)
        signInButton = view.findViewById(R.id.signInButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelSignInButton)
        val signUpTextView = view.findViewById<TextView>(R.id.signUpTextView)
        emailTextLayout = view.findViewById(R.id.emailInputLayout)
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)

        cancelButton.setOnClickListener {
            signInResultListener.onSignInFailure()
            dismiss()
        }

        signInButton.setOnClickListener {
            signIn()

        }

        signUpTextView.setOnClickListener {
            signInResultListener.onSignUpPress()
            dismiss()
        }


        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        return builder.create()

    }

    private fun signIn() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()


        val auth = FirebaseAuth.getInstance()
        if (!email.isEmpty() && !password.isEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    //Gets userInfo from database, needs to be done here to set with signInResultListener in addOnSuccessListener
                    val db = Firebase.firestore
                    db.collection("users").document(auth.uid.toString()).get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val currentUser = document.toObject<User>()
                                if (currentUser != null) {
                                    CurrentUser.setUser(currentUser)
                                }
                            }
                            signInResultListener.onSignInSuccess()
                            dismiss()

                        }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Wrong Password or Username.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("!!!", "Failure logging in!")
                }
        }
    }

}
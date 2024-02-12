package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth

class SignInDialogFragment() : DialogFragment() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.sign_in_dialog_fragment, null)
        emailEditText = view.findViewById(R.id.signInEmailEditText)
        passwordEditText = view.findViewById(R.id.signInPasswordEditText)
        val singInButton = view.findViewById<Button>(R.id.signInButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelSignInButton)
        val signUpTextView = view.findViewById<TextView>(R.id.signUpTextView)

        cancelButton.setOnClickListener {
            dismiss()
        }

        singInButton.setOnClickListener {
            signIn()
        }

        signUpTextView.setOnClickListener {
            val intent = Intent(activity, SignUpActivity::class.java)
            startActivity(intent)
        }


        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        return builder.create()

    }

    private fun signIn() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()


        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d("!!!", "Logged in")
                dismiss()
            }
            .addOnFailureListener {
                Log.d("!!!", "Failure logging in!")
            }


    }


}
package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth

class SignInDialogFragment() : DialogFragment() {

    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.sign_in_dialog_fragment, null)
        emailEditText = view.findViewById(R.id.signInEmailEditText)
        passwordEditText = view.findViewById(R.id.signInPasswordEditText)
        val singInButton = view.findViewById<Button>(R.id.signInButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelSignInButton)

        cancelButton.setOnClickListener {
            dismiss()
        }

        singInButton.setOnClickListener {
            signIn()
        }



        val builder = AlertDialog.Builder(requireActivity())


        builder.setView(view)

        return builder.create()

    }

    private fun signIn() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()


        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                Log.d("!!!", "Logged in")
                dismiss()
        }
            .addOnFailureListener {
                Log.d("!!!", "Failure logging in!")
            }


    }




}
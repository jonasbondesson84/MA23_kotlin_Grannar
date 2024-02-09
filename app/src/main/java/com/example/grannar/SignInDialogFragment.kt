package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class SignInDialogFragment() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.sign_in_dialog_fragment, null)
        val emailEditText = view.findViewById<EditText>(R.id.signInEmailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.signInPasswordEditText)



        val builder = AlertDialog.Builder(requireActivity())


        builder.setView(view)

        return builder.create()

    }

}
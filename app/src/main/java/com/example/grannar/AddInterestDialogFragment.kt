package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddInterestDialogFragment(): DialogFragment() {

    lateinit var tvInterestName: TextView
    lateinit var autoCompleteTextView: AutoCompleteTextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_fragment_add_interest, null)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, CategoryManager.getCategoryNames())
        tvInterestName = view.findViewById(R.id.tvInterestName)
        autoCompleteTextView = view.findViewById(R.id.dropDown)
        autoCompleteTextView.setAdapter(adapter)

        view.findViewById<Button>(R.id.interestCancelButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.interestAddButton).setOnClickListener {
            saveInterest()
        }


        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        return builder.create()


    }

    private fun saveInterest() {
        val category = CategoryManager.getCategoryFromName(autoCompleteTextView.text.toString())
        val interestName = tvInterestName.text.toString()
        if (category != null) {
            val interest = Interest(category, interestName)
            if (CurrentUser.interests == null){
                CurrentUser.interests = mutableListOf()
            }
            CurrentUser.interests?.add(interest)

            val uid = CurrentUser.userID
            if (uid != null) {
                CurrentUser.saveInterests(uid)
            }


//            val db = FirebaseFirestore.getInstance()
//            val auth = FirebaseAuth.getInstance()
//            val userID = auth.currentUser?.uid
//                if (userID != null) {
//                    db.collection("users").document(userID).
//                }
//
//        }
//
//        val interest = CategoryManager.getCategoryFromName(autoCompleteTextView.text.toString())
//            ?.let { Interest(it, tvInterestName.text.toString(), ) }
            Log.d("!!!", "${interest?.name} ${interest?.category?.name}")
        }
    }

}
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
import com.google.firebase.firestore.firestore
import javax.security.auth.callback.Callback

interface AddedInterestCallback{
    fun interestAdded()
}
class AddInterestDialogFragment(): DialogFragment() {

    lateinit var tvInterestName: TextView
    lateinit var autoCompleteTextView: AutoCompleteTextView
    private var interestCallback: AddedInterestCallback? = null
//    private      val categories = listOf(
//         Category("Sport", R.color.sportCategory),
//         Category("Nature", R.color.natureCategory),
//         Category("Animals", R.color.animalsCategory),
//         Category("Music", R.color.musicCategory),
//         Category("Literature", R.color.literatureCategory),
//         Category("Travel", R.color.travelCategory),
//         Category("Games", R.color.gamesCategory),
//         Category("Exercise", R.color.exerciseCategory),
//         Category("Other", R.color.otherCategory),
//    )

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

//    fun getCategoryNames():List<String>{
//        val categoryNames = mutableListOf<String>()
//        for (category in categories){
//            categoryNames.add(category.name.toString())
//        }
//        return categoryNames
//    }
//    fun getCategoryFromName(name: String): Category? {
//        val category = categories.find { it.name == name }
//        return category
//    }

    private fun saveInterest() {
        Log.d("!!!", "${autoCompleteTextView.text.toString()}")
        val categoryName =autoCompleteTextView.text.toString()
        //val categoryName = CategoryManager.getCategoryNames(autoCompleteTextView.text.toString())

        val interestName = tvInterestName.text.toString()
        if (interestName.isNotEmpty()) {
            val interest = Interest(categoryName, interestName)
            if (CurrentUser.interests == null){
                CurrentUser.interests = mutableListOf()
            }
            CurrentUser.interests?.add(interest)
            val db = Firebase.firestore
            val uid = CurrentUser.userID
            if (uid != null){
                val docRef = db.collection("users").document(uid)
                val updates = mapOf(
                    "interests" to CurrentUser.interests
                )
                docRef.update(updates).addOnSuccessListener {
                    Log.d("!!!", "Updated")
                    //CurrentUser.loadUserInfo(uid)
                    interestCallback?.interestAdded()
                    dismiss()
                }
            }


        }
    }
    fun setAddedInterestCallback(callback: AddedInterestCallback){
        interestCallback = callback
    }

}
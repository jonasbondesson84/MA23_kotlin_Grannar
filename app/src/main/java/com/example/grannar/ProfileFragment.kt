package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private lateinit var userProfile: User
val db = Firebase.firestore



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        val showName = view.findViewById<TextView>(R.id.nameTextView)
        val showGender = view.findViewById<TextView>(R.id.genderTextView)
        val showAge = view.findViewById<TextView>(R.id.ageTextView)
        val showLocation = view.findViewById<TextView>(R.id.locationTextView)



        getUserInfo { user ->
            if (user != null) {
                showName.text = user?.firstName
                showGender.text = user?.gender
                showAge.text = user?.age
                showLocation.text = user?.location?.toString() ?: "N/A"
            }else {
                showName.text="N/A"
                showGender.text="N/A"
                showAge.text="N/A"
                showLocation.text="N/A"
            }
        }


        return view
    }


    //funktioner


    fun getUserInfo(callback: (User?) -> Unit) {
        val docRef= db.collection("users").document("K2clKql2GHhX3ZKyErAiG3axf6r2")
        //documentPath kommer behöva ändras sedan till den anv som är inloggad.

        docRef.addSnapshotListener { snapshot, e ->

            if (e != null) {
                callback(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                callback(user)
            } else {
                callback(null)
            }
            //val user = documentSnapshot.toObject<User>()
        }
    }








    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
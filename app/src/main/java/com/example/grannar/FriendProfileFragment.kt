package com.example.grannar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val args: FriendProfileFragmentArgs by navArgs()
    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvAboutMe: TextView

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
        val view = inflater.inflate(R.layout.fragment_friend_profile, container, false)

        tvName = view.findViewById(R.id.friendProfileNameTextView)
        tvGender = view.findViewById(R.id.friendProfileGenderTextView)
        tvAge = view.findViewById(R.id.friendProfileAgeTextView)
        tvLocation = view.findViewById(R.id.friendProfileLocationTextView)
        tvAboutMe = view.findViewById(R.id.friendProfileAbout_meTextView)

        val appBar = view.findViewById<MaterialToolbar>(R.id.topFriendProfile)

        appBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val friendUid = args.userID
        if (friendUid != null) {
            getUserInfo(friendUid)
        }

        return view
    }

    private fun getUserInfo(friendUid: String) {
        val db = Firebase.firestore
        db.collection("users").document(friendUid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val selectedUser = document.toObject<User>()
                    if (selectedUser != null) {
                        showInfo(selectedUser)
                    }
                }
            }
    }

    private fun showInfo(selectedUser: User) {
        tvName.text = selectedUser.firstName
        tvGender.text = selectedUser.gender
        tvAge.text = selectedUser.getAgeSpan()
        tvLocation.text = selectedUser.location.toString()
        tvAboutMe.text = selectedUser.aboutMe

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
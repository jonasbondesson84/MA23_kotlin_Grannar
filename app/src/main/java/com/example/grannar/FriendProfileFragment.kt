package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

//import android.net.Uri
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.contract.ActivityResultContracts

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
    private var selectedUser: User? = null
    private var interestsTextViewList = mutableListOf<TextView>()
    private lateinit var appBar: MaterialToolbar



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

        interestsTextViewList.add(view.findViewById(R.id.friendsInterest1TextView))
        interestsTextViewList.add(view.findViewById(R.id.friendsInterest2TextView))
        interestsTextViewList.add(view.findViewById(R.id.friendsInterest3TextView))
        interestsTextViewList.add(view.findViewById(R.id.friendsInterest4TextView))
        interestsTextViewList.add(view.findViewById(R.id.friendsInterest5TextView))
        interestsTextViewList.add(view.findViewById(R.id.friendsInterest6TextView))




         appBar = view.findViewById(R.id.topFriendProfile)

        appBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        appBar.setOnMenuItemClickListener {menuItem->
            when(menuItem.itemId) {
                R.id.removeFriendMenu-> {
                    showDeleteDialog(view)
                    true
                }
                R.id.addFriendMenu -> {
                    addFriend()
                    true
                }
                R.id.sendMessageMenu -> {
                    sendMessage()
                    true
                }
                else -> false
            }
        }

        val friendUid = args.userID
        if (friendUid != null) {
            getUserInfo(friendUid)
        }

        return view
    }

    private fun sendMessage() {
        val uid = selectedUser?.userID
        if(uid != null) {
            val action =
                FriendProfileFragmentDirections.actionFriendProfileFragmentToChatFragment(uid)
            findNavController().navigate(action)
        }
    }

    private fun addFriend() {
        val userMap = mapOf(
            "userID" to selectedUser?.userID,
            "firstName" to selectedUser?.firstName,
            "surname" to selectedUser?.surname,
            "age" to selectedUser?.age,
            "location" to selectedUser?.location,
            "email" to selectedUser?.email,
            "gender" to selectedUser?.gender,
            "profileImageURL" to selectedUser?.profileImageURL,
            "interests" to selectedUser?.interests,
            "aboutMe" to selectedUser?.aboutMe,
            "imageURLs" to selectedUser?.imageURLs,
            "friendsList" to selectedUser?.friendsList

        )
        db.collection("users").document(CurrentUser.userID.toString()).update(
            "friendsList",
            FieldValue.arrayUnion(userMap)
        ).addOnCompleteListener {
            Log.d("!!!", "saved friend")
        }
    }

    private fun removeFriend() {


        val userMap = mapOf(
            "userID" to (selectedUser?.userID),
            "firstName" to (selectedUser?.firstName ),
            "surname" to (selectedUser?.surname),
            "age" to (selectedUser?.age),
            "location" to (selectedUser?.location ),
            "email" to (selectedUser?.email ),
            "gender" to selectedUser?.gender,
            "profileImageURL" to selectedUser?.profileImageURL,
            "interests" to selectedUser?.interests,
            "aboutMe" to selectedUser?.aboutMe,
            "imageURLs" to selectedUser?.imageURLs,
            "friendsList" to selectedUser?.friendsList

        )
        db.collection("users").document(CurrentUser.userID.toString()).update(
            "friendsList",
            FieldValue.arrayRemove(userMap)
        )

        CurrentUser.friendsList?.remove(selectedUser)

    }

    private fun getUserInfo(friendUid: String) {
        val db = Firebase.firestore
        db.collection("users").document(friendUid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    selectedUser = document.toObject<User>()
                    selectedUser?.let { showInfo(it) }

                }
            }
    }

    private fun setIcons(selectedUser: User) {
        if(CurrentUser.friendsList?.contains(selectedUser) == true) {
            Log.d("!!!", "Contains: true")
            appBar.menu.getItem(0).isVisible = false
        } else {
            Log.d("!!!", "Contains: false")
            appBar.menu.getItem(1).isVisible = false
        }
    }

    private fun showInfo(selectedUser: User) {
        tvName.text = selectedUser.firstName
        tvGender.text = selectedUser.gender
        tvAge.text = selectedUser.getAgeSpan()
        tvLocation.text = selectedUser.location.toString()
        tvAboutMe.text = selectedUser.aboutMe
        showInterests(selectedUser.interests)

       // setIcons(selectedUser)

    }


    private fun showInterests(interests: List<Interest>?){
        interests?.forEachIndexed{ i, interest ->
            val categoryColorID = interest.category?.let { CategoryManager.getCategoryColorId(it) }
            interestsTextViewList[i].text = interest.name
            if (categoryColorID != null) {
                interestsTextViewList[i].setBackgroundColor(resources.getColor(categoryColorID))
            }

        }
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

    private fun showDeleteDialog(view: View) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Warning")
                .setMessage("Do you want to remove ${selectedUser?.firstName} from your friendslist?")
                .setNegativeButton("No") { dialog, which ->
                    // Respond to negative button press

                }
                .setPositiveButton("Yes") { dialog, which ->
                    // Respond to positive button press
                    removeFriend()

                }
                .show()
        }
    }
}
package com.example.grannar

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
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
    val db = com.google.firebase.ktx.Firebase.firestore

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val args: FriendProfileFragmentArgs by navArgs()
    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvAboutMe: TextView
    private var interestChips = mutableListOf<Chip>()
    private var selectedUser: User? = null
    private lateinit var appBar: MaterialToolbar
    private lateinit var layout: ConstraintLayout
    private lateinit var personalImageView: ImageView
    private lateinit var ivFriendProfile: ImageView


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
        personalImageView = view.findViewById(R.id.personalImageView)

        interestChips.clear() // Need clear to remove old items when user uses back button
        interestChips.add(view.findViewById(R.id.friendsInterest1Chip))
        interestChips.add(view.findViewById(R.id.friendsInterest2Chip))
        interestChips.add(view.findViewById(R.id.friendsInterest3Chip))
        interestChips.add(view.findViewById(R.id.friendsInterest4Chip))
        interestChips.add(view.findViewById(R.id.friendsInterest5Chip))
        interestChips.add(view.findViewById(R.id.friendsInterest6Chip))

        layout = view.findViewById(R.id.linearLayoutFriend)
        val friendUid = args.userID
        if (friendUid != null) {
            getUserInfo(friendUid)
            layout.transitionName = friendUid
        }

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 500
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.md_theme_background))

        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 500
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.md_theme_background))
        }

        ivFriendProfile = view.findViewById(R.id.friendProfileImageView)

        appBar = view.findViewById(R.id.topFriendProfile)

        appBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        appBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.removeFriendMenu -> {
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

        return view
    }

    private fun sendMessage() {
        val uid = selectedUser?.userID
        if (uid != null) {
            val action =
                FriendProfileFragmentDirections.actionFriendProfileFragmentToChatFragment(uid)
            findNavController().navigate(action)
        }
    }

    private fun addFriend() {

        db.collection("users").document(CurrentUser.userID.toString()).update(
            "friendsUIDList",
            FieldValue.arrayUnion(selectedUser?.userID)
        ).addOnCompleteListener {

            CurrentUser.friendsUIDList.add(selectedUser?.userID.toString())
            selectedUser?.let { it1 -> setIcons(it1) }
        }
    }

    private fun removeFriend() {

        db.collection("users").document(CurrentUser.userID.toString()).update(
            "friendsUIDList",
            FieldValue.arrayRemove(selectedUser?.userID)
        ).addOnSuccessListener {

            CurrentUser.friendsUIDList.remove(selectedUser?.userID.toString())
            selectedUser?.let { it1 -> setIcons(it1) }
        }

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
        if (CurrentUser.friendsUIDList.contains(selectedUser.userID.toString())) {

            appBar.menu.getItem(0).isVisible = false
            appBar.menu.getItem(1).isVisible = true
        } else {

            appBar.menu.getItem(1).isVisible = false
            appBar.menu.getItem(0).isVisible = true
        }
    }

    private fun showInfo(selectedUser: User) {
        tvName.text = selectedUser.firstName
        tvGender.text = selectedUser.gender
        tvAge.text = selectedUser.getAgeSpan()
        tvLocation.text = selectedUser.showDistanceSpan()
        tvAboutMe.text = selectedUser.aboutMe
        showInterests(selectedUser.interests)


        selectedUser.personalImageUrl?.let { imageUrl ->

            Glide.with(this)
                .load(selectedUser.personalImageUrl)
                .placeholder(R.drawable.img_album)
                .error(R.drawable.img_album)
                .into(personalImageView)
        }

        setIcons(selectedUser)
        showProfileImage(selectedUser)

    }

    private fun showProfileImage(selectedUser: User) {

        if (selectedUser.profileImageURL != null) {
            Glide.with(this)
                .load(selectedUser.profileImageURL)
                .placeholder(R.drawable.img_album)
                .error(R.drawable.img_album)
                .into(ivFriendProfile)
        }
    }

    private fun showInterests(interests: List<Interest>?) {
        interests?.forEachIndexed { index, interest ->

            interestChips[index].text = interest.name
            val backgroundColor = ContextCompat.getColor(
                requireContext(),
                CategoryManager.getCategoryColorId(interest.category)
            )
            interestChips[index].chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
            interestChips[index].isVisible = true
            interestChips[index].setTextColor(
                AppCompatResources.getColorStateList(
                    requireContext(),
                    CategoryManager.getCategoryTextColorID(interest.category)
                )
            )

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
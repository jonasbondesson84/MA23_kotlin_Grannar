package com.example.grannar

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID


private lateinit var userProfile: User



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), AddedInterestCallback {
    // TODO: Rename and change types of parameters
    val db = Firebase.firestore

    private var param1: String? = null
    private var param2: String? = null

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var personalImageView: ImageView? = null
    private var profileImageView: ImageView? = null
    private var interestChips = mutableListOf<Chip>()
    private val MAX_INTERESTS = 6

    private lateinit var selectedImageUri: Uri
    private lateinit var getContent: ActivityResultLauncher<String>


    private var pendingDestination: NavDestination? = null
    private var pendingBundle: Bundle? = null
    private lateinit var signUpLauncher: ActivityResultLauncher<Intent>




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                val firebasePath = "userImage/${CurrentUser.userID}/${UUID.randomUUID()}.jpg"
                uploadPersonalImageToFirebase(it)
            }
        }
    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val profileBtn = view.findViewById<ImageButton>(R.id.profileImageButton)
        profileBtn.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*" //För image/png bara
                startActivityForResult(it, 0)
            }
        }
        val profileImageView: CircleImageView = view.findViewById(R.id.profileImageView)

        val showName = view.findViewById<TextView>(R.id.profileNameTextView)
        val showGender = view.findViewById<TextView>(R.id.profileGenderTextView)
        val showAge = view.findViewById<TextView>(R.id.profileAgeTextView)
        val showLocation = view.findViewById<TextView>(R.id.profileLocationTextView)
        val personalImageView = view.findViewById<ImageView>(R.id.personalImageView)
        val aboutMeEditText = view.findViewById<EditText>(R.id.profileAbout_meEditText)
        val signoutButton = view.findViewById<ImageButton>(R.id.signoutButton)


        Log.d("!!!", "Nr of interests:  ${CurrentUser.interests?.size}")
        Log.d("!!!", "Nr of interests:  ${CurrentUser.firstName}")

        interestChips.add(view.findViewById(R.id.profileInterest1Chip))
        interestChips.add(view.findViewById(R.id.profileInterest2Chip))
        interestChips.add(view.findViewById(R.id.profileInterest3Chip))
        interestChips.add(view.findViewById(R.id.profileInterest4Chip))
        interestChips.add(view.findViewById(R.id.profileInterest5Chip))
        interestChips.add(view.findViewById(R.id.profileInterest6Chip))

        Log.d("!!!", "${CurrentUser.interests?.size}")

        val button: ImageButton = view.findViewById(R.id.chooseImageButton)
        button.setOnClickListener {
            openGallery()
        }

        val editProfileTextBtn: ImageButton = view.findViewById(R.id.editImageButton)
        editProfileTextBtn.setOnClickListener{
            val dialogFragment = EditProfileDialogFragment()
            dialogFragment.show(childFragmentManager, "editprofileDialog")
        }


        signoutButton.setOnClickListener {
            if (isLoggedIn()) {
                signOut()
                Log.d("!!!", "User is signing out")
            } else {
                Log.d("!!!", "user is not loged in")
            }
        }



        getUserInfo { user ->
            if (isAdded) {
            if (user != null) {
                val profileImageURL = user.profileImageURL
                showName.text = user.firstName
                showGender.text = user.gender
                showAge.text = user.age
                showLocation.text = user.location?.toString() ?: "none location to show"
                if (!profileImageURL.isNullOrEmpty()) {
                    Glide.with(requireActivity())
                        .load(user.profileImageURL)
                        .into(profileImageView!!)
                }
                aboutMeEditText.setText(user.aboutMe)
                aboutMeEditText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        Log.d("!!!", "savebutton")
                        saveAboutMe(aboutMeEditText.text.toString())
                        return@setOnEditorActionListener true
                    }
                    false
                }
            } else {
                showName.text = " "
                showGender.text = " "
                showAge.text = " "
                showLocation.text = " "
                profileImageView.setImageResource(R.drawable.avatar)
            }
            }
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        personalImageView = view.findViewById(R.id.personalImageView)
        loadPersonalImageFromPreferences()

    }

    private fun loadPersonalImageFromPreferences(){
       // val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Activity.MODE_PRIVATE)
       // val imageUrl = sharedPreferences.getString("personalImageUrl", null)
        if(CurrentUser.personalImageUrl != null) {
            Glide.with(requireContext())
                .load(CurrentUser.personalImageUrl)
                .into(personalImageView!!)
        }
    }

    override fun onResume() {
        super.onResume()
        showInterestsWithColor(CurrentUser.interests)
    }

    //funktioner




    private fun isLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            return true
        }
        else return false
    }

    private fun signOut() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        CurrentUser.clearUser()
        Toast.makeText(requireContext(), "You have Logged Out", Toast.LENGTH_SHORT)
            .show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            val uri = data?.data
            uri?.let {
                personalImageView?.setImageURI(uri)
                imageUri = uri // save for upload
                uploadImageToFirebase()
            }
        } else {

        }
    }


    private fun uploadImageToFirebase() {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${CurrentUser.userID}/profileImage.jpg")

            imageRef.putFile(imageUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        db.collection("users").document(CurrentUser.userID!!)
                            .update("profileImageURL", downloadUri.toString())
                    } else {

                    }
                }
                .addOnSuccessListener {
                    profileImageView?.setImageURI(imageUri)

                    Toast.makeText(
                        requireContext(),
                        "Image upploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }


    private fun getUserInfo(callback: (User?) -> Unit) {
        val docRef = db.collection("users").document(CurrentUser.userID!!)
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
        }
    }

    private fun showInterestsWithColor(interests: MutableList<Interest>?) {
        if (interests != null) {
            interests?.forEachIndexed { index, interest ->

                interestChips[index].text = interest.name
                val backgroundColor = ContextCompat.getColor(requireContext(), CategoryManager.getCategoryColorId(interest.category))
                interestChips[index].chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
                interestChips[index].isVisible = true
                interestChips[index].setTextColor(
                    AppCompatResources.getColorStateList(requireContext(), CategoryManager.getCategoryTextColorID(interest.category)))
                interestChips[index].setChipIconResource(R.drawable.baseline_close_24)
                interestChips[index].setOnClickListener {
                    deleteInterest(index)
                }
            }
            interests?.size?.let { hideInterestChips(it) }

        } else {
            hideInterestChips(0)
        }
    }

    private fun hideInterestChips(numberToBeVisible: Int) {
        interestChips.forEachIndexed() { i, chip ->
            if (i >= numberToBeVisible) {
                if (i == MAX_INTERESTS - 1) {
                    chip.text = "Add Interest"
                    val backgroundColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary)
                    chip.chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
                    chip.setTextColor(Color.WHITE)
                    chip.isVisible = true
                    chip.setChipIconResource(R.drawable.baseline_add_24)
                    chip.setOnClickListener {
                        startAddInterestDialog()
                    }
//                    }

                } else {
                    chip.visibility = View.INVISIBLE

                }
            }
        }

    }

    private fun deleteInterest(index: Int) {
        val db = com.google.firebase.Firebase.firestore
        val uid = CurrentUser.userID
        CurrentUser.interests?.removeAt(index)
        if (uid != null) {
            val docRef = db.collection("users").document(uid)
            val updates = mapOf(
                "interests" to CurrentUser.interests
            )
            docRef.update(updates).addOnSuccessListener {
                showInterestsWithColor(CurrentUser.interests)

            }
        }

    }

    private fun startAddInterestDialog() {
        val dialogFragment = AddInterestDialogFragment()
        dialogFragment.setAddedInterestCallback(this)
        dialogFragment.show(parentFragmentManager, "AddInterestFragment")
    }

    override fun interestAdded() {
        showInterestsWithColor(CurrentUser.interests)
    }


    fun openGallery() {
        getContent.launch("image/*")
    }
//private fun uploadImageToFirebaseStorage(imageUri: Uri, firebasePath: String) {
   // val storageRef = FirebaseStorage.getInstance().getReference(firebasePath)
  //  storageRef.putFile(imageUri)
       // .addOnSuccessListener{
          //  storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
              //  Log.d("ProfileFragment", "Bild uppladdad: $downloadUri")
             //   Glide.with(this@ProfileFragment)
                 //   .load(downloadUri.toString())
                  //  .into(personalImageView!!)
         //   }
       // }
     //   .addOnFailureListener {
          //  Toast.makeText(context, "Uppladdning misslyckades: ${it.message}", Toast.LENGTH_SHORT).show()
      //  }
//}

    private fun uploadPersonalImageToFirebase(imageUri: Uri) {
        val firebasePath = "images/${CurrentUser.userID}/personalImages/${UUID.randomUUID()}.jpg"
        val storageRef = FirebaseStorage.getInstance().getReference(firebasePath)

        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updatePersonalImageUrlInFirestore(downloadUri.toString())
                    Log.d("Upload", "Personal Image Uploaded: $downloadUri")
                    Glide.with(requireContext())
                        .load(downloadUri)
                        .into(personalImageView!!)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to upload personal image: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Upload", "Failed to upload personal image", e)
            }
    }

    private fun updatePersonalImageUrlInFirestore(imageUrl: String){
        val userRef = db.collection("users").document(CurrentUser.userID!!)
        userRef.update("personalImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("Firestore", "Personal image URL updated ")


            }
            .addOnFailureListener{ e ->
               Log.e("Firestore", "Error updating personal image URL", e)
           }
    }

   // private fun saveImageUrlToPreferences(imageUrl: String) {
       // val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Activity.MODE_PRIVATE)
    //    sharedPreferences.edit().putString("personalImageUrl", imageUrl).apply()
  // }


    private fun saveAboutMe(newAboutMe: String) {
        val userRef = db.collection("users").document(CurrentUser.userID!!)
        userRef.update("aboutMe", newAboutMe)
            .addOnSuccessListener {
                Log.d("!!!", "success about me ${db}")
                Toast.makeText(
                    requireContext(),
                    "About me updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Log.d("!!!", "Error updating About Me ${db}")
                Toast.makeText(requireContext(), "Failed to update About Me", Toast.LENGTH_SHORT)
                    .show()
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
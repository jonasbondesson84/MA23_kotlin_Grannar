package com.example.grannar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage



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
class ProfileFragment : Fragment(), AddedInterestCallback{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var personalImageView: ImageView? = null
    private var interestTextViewList = mutableListOf<TextView>()
    private var interestConstraintList = mutableListOf<ConstraintLayout>()
    private lateinit var lastInterestImageView: ImageView
    val MAX_INTERESTS = 6


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

        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        val showName = view.findViewById<TextView>(R.id.profileNameTextView)
        val showGender = view.findViewById<TextView>(R.id.profileGenderTextView)
        val showAge = view.findViewById<TextView>(R.id.profileAgeTextView)
        val showLocation = view.findViewById<TextView>(R.id.profileLocationTextView)

        val aboutMeEditText = view.findViewById<EditText>(R.id.profileAbout_meEditText)
        //val saveAboutMeButton =view.findViewById<Button>(R.id.saveAboutMeButton)

        //Anv ska kunna ladda upp en övrig bild
        val chooseImageButton = view.findViewById<ImageButton>(R.id.chooseImageButton)
        val personalImageView = view.findViewById<ImageView>(R.id.personalImageView)
        Log.d("!!!", "Nr of interests:  ${CurrentUser.interests?.size}")
        Log.d("!!!", "Nr of interests:  ${CurrentUser.firstName}")

        interestTextViewList.add(view.findViewById(R.id.interest1TextView))
        interestTextViewList.add(view.findViewById(R.id.interest2TextView))
        interestTextViewList.add(view.findViewById(R.id.interest3TextView))
        interestTextViewList.add(view.findViewById(R.id.interest4TextView))
        interestTextViewList.add(view.findViewById(R.id.interest5TextView))
        interestTextViewList.add(view.findViewById(R.id.interest6TextView))

        interestConstraintList.add(view.findViewById(R.id.interest1Constraint))
        interestConstraintList.add(view.findViewById(R.id.interest2Constraint))
        interestConstraintList.add(view.findViewById(R.id.interest3Constraint))
        interestConstraintList.add(view.findViewById(R.id.interest4Constraint))
        interestConstraintList.add(view.findViewById(R.id.interest5Constraint))
        interestConstraintList.add(view.findViewById(R.id.interest6Constraint))

        lastInterestImageView = view.findViewById(R.id.deleteInterest6ImageView)

        Log.d("!!!", "${CurrentUser.interests?.size}")



        chooseImageButton.setOnClickListener {
            openImageChooser()
        }


        getUserInfo { user ->
            if (user != null) {
                showName.text = user?.firstName
                showGender.text = user?.gender
                showAge.text = user?.age
                showLocation.text = user?.location?.toString() ?: "none location to show"

              //  showInterest(user.interests)
                aboutMeEditText.setText(user.aboutMe)

                aboutMeEditText.setOnEditorActionListener{ _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        Log.d("!!!", "savebutton")
                        saveAboutMe(aboutMeEditText.text.toString())
                        return@setOnEditorActionListener true
                    }
                    false
                    }
                } else {
                showName.text=" "
                showGender.text=" "
                showAge.text=" "
                showLocation.text=" "
                //showInterest(null)
            }
        }


        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        showInterestsWithColor(CurrentUser.interests)
    }

    //funktioner



    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            personalImageView?.setImageURI(imageUri)

            uploadImageToFirebase()
        }
    }

    private fun uploadImageToFirebase(){
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${userProfile.userID}/profileImage.jpg")

            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                }
                .addOnFailureListener {e ->

                }
        }
    }







    private fun getUserInfo(callback: (User?) -> Unit) {
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

    private fun showInterest(interests: MutableList<Interest>?) {

        val interest1TextView = view?.findViewById<TextView>(R.id.interest1TextView)
        val interest2TextView = view?.findViewById<TextView>(R.id.interest2TextView)
        val interest3TextView = view?.findViewById<TextView>(R.id.interest3TextView)
        val interest4TextView = view?.findViewById<TextView>(R.id.interest4TextView)
        val interest5TextView = view?.findViewById<TextView>(R.id.interest5TextView)
        val interest6TextView = view?.findViewById<TextView>(R.id.interest6TextView)


        if (interests != null && interests.size >= 6){
            interest1TextView?.text = interests[0].name
            interest2TextView?.text = interests[1].name
            interest3TextView?.text = interests[2].name
            interest4TextView?.text = interests[3].name
            interest5TextView?.text = interests[4].name
            interest6TextView?.text = interests[5].name
        } else{
            interest1TextView?.text = " "
            interest2TextView?.text = " "
            interest3TextView?.text = " "
            interest4TextView?.text = " "
            interest5TextView?.text = " "
            interest6TextView?.text = " "
        }
    }

    private fun showInterestsWithColor(interests: MutableList<Interest>?){
        if (interests != null){
            interests?.forEachIndexed { i, interest ->
                interestTextViewList[i].text = interest.name
                interest.category?.colorID?.let { interestTextViewList[i].setBackgroundColor(resources.getColor(it)) }
                interestConstraintList[i].visibility = View.VISIBLE
                interestConstraintList[i].setOnClickListener {
                    deleteInterest(i)
                }
                if (i == MAX_INTERESTS -1){
                    lastInterestImageView.setImageResource(R.drawable.baseline_close_24)
                    lastInterestImageView.setBackgroundColor(resources.getColor(R.color.md_theme_error))

                }
            }
            interests?.size?.let { hideConstraints(it) }

        }else{
            hideConstraints(0)
        }
    }

    private fun hideConstraints(numberToBeVisible: Int){
        interestConstraintList.forEachIndexed() { i, constraintLayout ->
            if (i >= numberToBeVisible){
                if (i == MAX_INTERESTS -1){
                    interestTextViewList[i].text = "Add Interest"
                    constraintLayout.visibility = View.VISIBLE
                    interestTextViewList[i].setTextColor(resources.getColor(R.color.md_theme_primary))
                    lastInterestImageView.setImageResource(R.drawable.baseline_add_24)
                    lastInterestImageView.setBackgroundColor(resources.getColor(R.color.md_theme_primary))
                    interestTextViewList[i].setBackgroundColor(0)
                    constraintLayout.setOnClickListener {
                        startAddInterestDialog()
                    }

                } else {
                    constraintLayout.visibility = View.INVISIBLE

                }
            }
        }

    }

    private fun deleteInterest(index: Int){
        val db = com.google.firebase.Firebase.firestore
        val uid = CurrentUser.userID
        CurrentUser.interests?.removeAt(index)
        if (uid != null){
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

private fun saveAboutMe(newAboutMe: String) {

    val userId = "K2clKql2GHhX3ZKyErAiG3axf6r2"
    val userRef = db.collection("users").document(userId)
    userRef.update("aboutMe", newAboutMe)
        .addOnSuccessListener {
            Log.d("!!!", "success about me ${db}")
            Toast.makeText(requireContext(), "About me updated successfully", Toast.LENGTH_SHORT).show()
        } .addOnFailureListener{
            Log.d("!!!", "Error updating About Me ${db}")
            Toast.makeText(requireContext(), "Failed to update About Me", Toast.LENGTH_SHORT).show()
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
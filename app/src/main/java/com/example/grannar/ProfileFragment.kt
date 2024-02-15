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
import androidx.fragment.app.Fragment
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
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var personalImageView: ImageView? = null


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


        val showName = view.findViewById<TextView>(R.id.friendProfileNameTextView)
        val showGender = view.findViewById<TextView>(R.id.friendProfileGenderTextView)
        val showAge = view.findViewById<TextView>(R.id.friendProfileAgeTextView)
        val showLocation = view.findViewById<TextView>(R.id.friendProfileLocationTextView)

        val aboutMeEditText = view.findViewById<EditText>(R.id.about_meEditText)
        //val saveAboutMeButton =view.findViewById<Button>(R.id.saveAboutMeButton)

        //Anv ska kunna ladda upp en övrig bild
        val chooseImageButton = view.findViewById<ImageButton>(R.id.chooseImageButton)
        val personalImageView = view.findViewById<ImageView>(R.id.personalImageView)

        chooseImageButton.setOnClickListener {
            openImageChooser()
        }


        getUserInfo { user ->
            if (user != null) {
                showName.text = user?.firstName
                showGender.text = user?.gender
                showAge.text = user?.age
                showLocation.text = user?.location?.toString() ?: "none location to show"

                showInterest(user.interests)
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
                showInterest(null)
            }
        }


        return view
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

    private fun showInterest(interests: MutableList<Interests>?) {

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
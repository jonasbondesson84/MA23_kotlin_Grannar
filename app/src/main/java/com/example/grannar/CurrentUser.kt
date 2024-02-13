package com.example.grannar

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.type.LatLng

object CurrentUser {

    var userID: String? = null
    var firstName: String? = null
    var surname: String? = null
    var age: String? = null
    var location: LatLng? = null
    var email: String? = null
    var gender: String? = null
    var profileImageURL: String? = null
    var interests: MutableList<Interests>? = mutableListOf()
    var aboutMe: String? = null
    var imageURLs: MutableList<String>? = mutableListOf()
    var friendsList: MutableList<User>? = mutableListOf()


    fun setUser(user: User) {
        clearUser()
        this.userID = user.userID
        this.firstName = user.firstName
        this.surname = user.surname
        this.age = user.age
        this.location = user.location
        this.email = user.email
        this.gender = user.gender
        this.profileImageURL = user.profileImageURL
        this.interests = user.interests
        this.aboutMe = user.aboutMe
        this.imageURLs = user.imageURLs
        this.friendsList = user.friendsList

    }

    fun clearUser() {
        this.userID = null
        this.firstName = null
        this.surname = null
        this.age = null
        this.location = null
        this.email = null
        this.gender = null
        this.profileImageURL = null
        this.interests = null
        this.aboutMe = null
        this.imageURLs = null
        this.friendsList?.clear()
    }

    fun loadUserInfo(uid: String) {
        val db = Firebase.firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener {document ->
                if (document != null) {
                    val currentUser = document.toObject<User>()
                    if (currentUser != null) {
                        setUser(currentUser)
                    }
                }

            }
    }

}
package com.example.grannar

import com.google.firebase.firestore.DocumentId
import com.google.type.LatLng
import java.util.Date

class User(
   // @DocumentId val docID: String? = null,
    var userID: String? = null,
    var firstName: String? = null,
    var surname: String? = null,
    var age: Date? = null,
    var location: LatLng? = null,
    var email: String? = null,
    var gender: String? = null,
    var profileImageURL: String? = null,
    var interests: MutableList<Interests>? = null,
    var aboutMe: String? = null,
    var imageURLs: MutableList<String>? = null,
    var friendsList: MutableList<String>? = null

    ) {

}
package com.example.grannar

import com.google.firebase.firestore.Exclude
import com.google.type.LatLng
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class User(

    var userID: String? = null,
    var firstName: String? = null,
    var surname: String? = null,
    var age: String? = null,
    var location: LatLng? = null,
    var email: String? = null,
    var gender: String? = null,
    var profileImageURL: String? = null,
    var interests: MutableList<Interests>? = null,
    var aboutMe: String? = null,
    var imageURLs: MutableList<String>? = null,
    var friendsList: MutableList<String>? = null

    ) {

    @Exclude
    fun getBirthDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return LocalDate.parse(age, formatter)
    }
}
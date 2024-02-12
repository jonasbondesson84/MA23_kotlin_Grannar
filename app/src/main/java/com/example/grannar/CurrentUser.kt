package com.example.grannar

import com.google.type.LatLng
import java.time.LocalDate

object CurrentUser {

    var userID: String? = null
    var firstName: String? = null
    var surname: String? = null
    var age: LocalDate? = null
    var location: LatLng? = null
    var email: String? = null
    var gender: String? = null
    var profileImageURL: String? = null
    var interests = mutableListOf<Interests>()
    var aboutMe: String? = null
    var imageURLs = mutableListOf<String>()
    var friendsList = mutableListOf<User>()

}
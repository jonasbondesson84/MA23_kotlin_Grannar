package com.example.grannar

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
    var interests = mutableListOf<Interests>()
    var aboutMe: String? = null
    var imageURLs = mutableListOf<String>()
    var friendsList = mutableListOf<User>()


    fun setUser() {
        this.userID = "bJZDtEbTLAN0iFOYwr8Hsds41V62"
        this.firstName = "Jonas"
        this.surname = "Bondesson"
        this.age = "1990/12/12"
    }
}
package com.example.grannar

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.type.LatLng
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


data class User(
    @DocumentId var docID: String? = null,

    var userID: String? = null,
    var firstName: String? = null,
    var surname: String? = null,
    var age: String? = null,
    var location: LatLng? = null,
    var email: String? = null,
    var gender: String? = null,
    var profileImageURL: String? = null,
    var interests: MutableList<Interest>? = null,
    var aboutMe: String? = null,
    var imageURLs: MutableList<String>? = null,
    var friendsList: MutableList<User>? = mutableListOf<User>()

    ) {
   // constructor() : this("", null, null, null, null, null, null, null, null, null, null, null, null)


    @Exclude
    fun getBirthDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return LocalDate.parse(age, formatter)
    }

    fun getAgeSpan(): String {
        val userAge = getBirthDate()
        val currentDate = LocalDate.now()
        val ageSpan = Period.between(userAge, currentDate)
        when (ageSpan.years) {
            in 2..10 -> {
                return "1 - 10"
            }
            in 11 .. 20 -> {
                return "11 - 20"
            }
            in 21 .. 30 -> {
                return "21 - 30"
            }
            in 31 .. 40 -> {
                return "31 - 40"
            }
            in 41 .. 50 -> {
                return "41 - 50"
            }
            in 51 .. 60 -> {
                return "51 - 60"
            }
            in 61 .. 70 -> {
                return "61 - 70"
            }
            else -> return "70+"
        }

    }
}
package com.example.grannar

import android.util.Log
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt


data class User(
    @DocumentId var docID: String? = null,

    var userID: String? = null,
    var firstName: String? = null,
    var surname: String? = null,
    var age: String? = null,
    var location: com.google.android.gms.maps.model.LatLng? = null,
    var locLat: Double? = null,
    var locLng: Double? = null,
    var geoHash: String? = null,
    var email: String? = null,
    var gender: String? = null,
    var profileImageURL: String? = null,
    var personalImageUrl: String? = null,
    var interests: MutableList<Interest>? = mutableListOf(),
    var aboutMe: String? = null,
    var imageURLs: MutableList<String>? = null,
    var friendsList: MutableList<User>? = mutableListOf<User>(),
    var friendsUIDList: MutableList<String> = mutableListOf(),
    var unreadMessages: HashMap<String, Int> = hashMapOf(),
    var savedEvents: MutableList<String> = mutableListOf()
   //

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

    fun showDistanceSpan():String {
        val distance = round(calculateDistance())
        Log.d("!!!", distance.toString())
        when(distance) {
            in 0.0 .. 9.9 -> {
                return "0 - 10 km"
            }
            in 10.0 .. 19.9 -> {
                return "10 - 20 km"
            }
            in 20.0 .. 29.9 -> {
                return "20 - 30 km"
            }
            in 30.0 .. 39.9 -> {
                return "30 - 40 km"
            }
            in 40.0 .. 49.9 -> {
                return "40 - 50 km"
            }
            in 50.0 .. 59.9 -> {
                return "50 - 60 km"
            }
            in 60.0 .. 69.9 -> {
                return "60 - 70 km"
            }
            in 70.0 .. 79.9 -> {
                return "70 - 80 km"
            }
            in 80.0 .. 89.9 -> {
                return "80 - 90 km"
            }
            in 90.0 .. 99.9 -> {
                return "90 - 100 km"
            }
            else -> {
                return "100+ km"
            }
        }
    }

    fun calculateDistance(): Double {
        val R = 6371 //earth radius

        val lat1 = CurrentUser.locLat ?: 0.0
        val lon1 = CurrentUser.locLng ?: 0.0

        val lat2 = this.locLat ?: 0.0
        val lon2 = this.locLng ?: 0.0


        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c  //distance in km


        return distance
    }





}
package com.example.grannar


import com.google.firebase.firestore.DocumentId
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class Event(
    @DocumentId var docID: String? = null,
    var name: String? = null,
    var location: Map<String, Any>? = null,
    var startDateTime: Date? = null,
    var description: String? = null,
    var imageURL: String? = null,
    val createdByUID: String? = null,
    val geoHash: String? = null,
    val locLat: Double? = null,
    val locLng: Double? = null

) {
    fun showDistanceSpan(): String {
        val distance = round(calculateDistance())
        when (distance) {
            in 0.0..9.9 -> {
                return "0 - 10 km"
            }

            in 10.0..19.9 -> {
                return "10 - 20 km"
            }

            in 20.0..29.9 -> {
                return "20 - 30 km"
            }

            in 30.0..39.9 -> {
                return "30 - 40 km"
            }

            in 40.0..49.9 -> {
                return "40 - 50 km"
            }

            in 50.0..59.9 -> {
                return "50 - 60 km"
            }

            in 60.0..69.9 -> {
                return "60 - 70 km"
            }

            in 70.0..79.9 -> {
                return "70 - 80 km"
            }

            in 80.0..89.9 -> {
                return "80 - 90 km"
            }

            in 90.0..99.9 -> {
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
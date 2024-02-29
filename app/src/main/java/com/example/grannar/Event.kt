package com.example.grannar

import com.google.firebase.firestore.DocumentId
import java.util.Date

class Event(
    @DocumentId var docID: String? = null,
    var name: String? = null,
//    var locationLat: Double? = null,
//    var locationLng: Double? = null,
    var location: Map<String, Any>? = null,
    var startDateTime: Date? = null,
    var description: String? = null,
    var imageURL: String? = null,
    val createdByUID: String? = null,
    val geoHash: String? = null,
    val locLat: Double? = null,
    val locLng: Double? = null

) {
}
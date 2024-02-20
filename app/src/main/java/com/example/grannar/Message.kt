package com.example.grannar

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class Message(
    @DocumentId val docID: String? = null,
    @ServerTimestamp val timeStamp: Date? = null,
//    val timeStamp: LocalDateTime? = null,
    val fromID: String? = null,
    val toID: String? = null,
    val text: String? = null,
    var unread: Boolean = true

    ) {

}
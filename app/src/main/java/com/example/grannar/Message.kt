package com.example.grannar

import com.google.firebase.firestore.DocumentId
import java.util.Date

class Message(
    @DocumentId val docID: String? = null,
    val timeStamp: Date? = null,
    val fromID: String? = null,
    val toID: String? = null,
    val message: String? = null,
    val unread: Boolean = true

    ) {
}
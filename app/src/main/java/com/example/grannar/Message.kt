package com.example.grannar

import com.google.firebase.firestore.DocumentId

class Message(
    @DocumentId val docID: String? = null,
    //val timestamp: ServerTimestamp,
    val fromID: String,
    val toID: String,
    val message: String,
    val unread: Boolean = true

    ) {
}
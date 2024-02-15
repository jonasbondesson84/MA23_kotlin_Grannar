package com.example.grannar

import com.google.firebase.firestore.DocumentId

class Message(
    @DocumentId val docID: String?,
    //val timestamp: ServerTimestamp,
    val fromUser: User,
    val toUser: User,
    val message: String

    ) {
}
package com.example.grannar

import com.google.firebase.firestore.DocumentId

class Chats(
    @DocumentId val docID: String? = null,
    val fromUser: User,
    var lastMessage: Message
)

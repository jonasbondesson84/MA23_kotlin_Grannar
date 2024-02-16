package com.example.grannar

import com.google.firebase.firestore.DocumentId

class Chats(
    @DocumentId val docID: String? = null,
    var participants: MutableList<String>? = null,
    var messages: MutableList<Message>? = null
) {
}
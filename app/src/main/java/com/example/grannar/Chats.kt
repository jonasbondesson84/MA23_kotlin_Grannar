package com.example.grannar

import com.google.firebase.firestore.DocumentId

class Chats(
    @DocumentId val docID: String? = null,
   // var participants: MutableList<String>? = null,
//    var message: MutableList<HashMap<String, Any>>? = null
    val fromUser: User,
//    var message: MutableList<Message>? = null
    var lastMessage: Message
) {
}
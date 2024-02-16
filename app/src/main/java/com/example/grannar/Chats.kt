package com.example.grannar

import com.google.firebase.firestore.DocumentId

class Chats(
    @DocumentId val docID: String,
    var participants: MutableList<User>,
    var unread: Boolean,
    var messages: MutableList<Message>
) {
}
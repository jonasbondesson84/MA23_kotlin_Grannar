package com.example.grannar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

object CurrentUser {

    var userID: String? = null
    var firstName: String? = null
    var surname: String? = null
    var age: String? = null
    var location: com.google.android.gms.maps.model.LatLng? = null
    var locLat: Double? = null
    var locLng: Double? = null
    var geoHash: String? = null
    var email: String? = null
    var gender: String? = null
    var profileImageURL: String? = null
    var interests: MutableList<Interest>? = mutableListOf()
    var aboutMe: String? = null
    var imageURLs: MutableList<String>? = mutableListOf()
    var friendsList: MutableList<User>? = mutableListOf()
    var friendsUIDList: MutableList<String> = mutableListOf()
    var unreadMessageIDs: HashMap<String, Int> = hashMapOf()
    private val _unreadMessagesNumber = MutableLiveData<Int>( 0)
    val unreadMessageNumber: LiveData<Int> = _unreadMessagesNumber
    var savedEvent: MutableList<String> = mutableListOf()

    var tabFriendItem = 0
    var tabEventItem = 0





    fun setUser(user: User) {
        clearUser()
        this.userID = user.userID
        this.firstName = user.firstName
        this.surname = user.surname
        this.age = user.age
        this.location = user.location
        this.locLat= user.locLat
        this.locLng= user.locLng
        this.geoHash = user.geoHash
        this.email = user.email
        this.gender = user.gender
        this.profileImageURL = user.profileImageURL
        this.interests = user.interests
        this.aboutMe = user.aboutMe
        this.imageURLs = user.imageURLs
        this.friendsList = user.friendsList
        this.friendsUIDList = user.friendsUIDList
        this.unreadMessageIDs = user.unreadMessages
        this.savedEvent = user.savedEvents
        getUnreadMessages(user)
        getFriendList(user)

    }

    private fun getUnreadMessages(user: User) {
        this._unreadMessagesNumber.value = user.unreadMessages.size
        Log.d("!!!", user.unreadMessages.size.toString())
    }

    fun clearUser() {
        this.userID = null
        this.firstName = null
        this.surname = null
        this.age = null
        this.location = null
        this.locLat = null
        this.locLng = null
        this.email = null
        this.gender = null
        this.profileImageURL = null
        this.interests = null
        this.aboutMe = null
        this.imageURLs = null
        this.friendsList?.clear()
        this.friendsUIDList.clear()
        this.unreadMessageIDs.clear()
        this._unreadMessagesNumber.value = 0
        this.savedEvent.clear()
    }

    fun loadUserInfo(uid: String) {
        val db = Firebase.firestore
        db.collection("users").document(uid).addSnapshotListener { user, error ->
            if(user != null) {
                val currentUser = user.toObject<User>()
                if(currentUser != null) {
                    setUser(currentUser)

                }
            }
        }
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener {document ->
//                if (document != null) {
//                    val currentUser = document.toObject<User>()
//                    if (currentUser != null) {
//                        setUser(currentUser)
//                    }
//                }
//
//            }
    }
    fun getFriendList(user: User) {
        this.friendsList?.clear()
        for(userID in user.friendsUIDList) {
            db.collection("users").document(userID).get()
                .addOnSuccessListener {document->
                    if(document != null) {
                        val friend = document.toObject<User>()
                        if(friend != null) {
                            this.friendsList?.add(friend)
                        }
                    }

                }
        }
    }

}
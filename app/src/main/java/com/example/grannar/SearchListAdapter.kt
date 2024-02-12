
package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class SearchListAdapter(context: Context, private val searchList: MutableList<User>) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private val db = Firebase.firestore


    var onUserClick: ((User) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.friend_name)
        val tvAge: TextView = itemView.findViewById(R.id.age)
        val addFriend: LinearLayout = itemView.findViewById(R.id.addFriend_linearLayout)
        val btnAddFriend: ImageButton = itemView.findViewById(R.id.friend_request_icon)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchListAdapter.ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_friend, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchListAdapter.ViewHolder, position: Int) {
        val selectedUser = searchList[position]
        holder.tvName.text = selectedUser.firstName
        holder.tvAge.text = "Age: ${selectedUser.getAgeSpan() }"

        //If a user is in CurrentUsers friendslist, the add friend button gets removed
        if(selectedUser in CurrentUser.friendsList) {
            holder.addFriend.visibility = View.INVISIBLE

        }

        holder.itemView.setOnClickListener {
            onUserClick?.invoke(selectedUser)
        }
        holder.btnAddFriend.setOnClickListener {
            saveFriend(selectedUser, position)
        }
        holder.addFriend.setOnClickListener {

            saveFriend(selectedUser, position)

        }

    }



    fun removeFriend( position: Int) {
        val selectedUser = searchList[position]
        val userMap = mapOf(
            "userID" to selectedUser.userID,
            "firstName" to selectedUser.firstName,
            "surname" to selectedUser.surname,
            "age" to selectedUser.age,
            "location" to selectedUser.location,
            "email" to selectedUser.email,
            "gender" to selectedUser.gender,
            "profileImageURL" to selectedUser.profileImageURL,
            "interests" to selectedUser.interests,
            "aboutMe" to selectedUser.aboutMe,
            "imageURLs" to selectedUser.imageURLs,
            "friendsList" to selectedUser.friendsList

        )
        db.collection("users").document(CurrentUser.userID.toString()).update(
            "friendsList",
            FieldValue.arrayRemove(userMap)
        )

        CurrentUser.friendsList.remove(selectedUser)
        notifyItemRemoved(position)
    }

    private fun saveFriend(friend: User, position: Int) {
        CurrentUser.friendsList.add(friend)
        CurrentUser.userID="bJZDtEbTLAN0iFOYwr8Hsds41V62"
        var i = 0

        val userMap = mapOf(
            "userID" to friend.userID,
            "firstName" to friend.firstName,
            "surname" to friend.surname,
            "age" to friend.age,
            "location" to friend.location,
                "email" to friend.email,
                "gender" to friend.gender,
                "profileImageURL" to friend.profileImageURL,
                "interests" to friend.interests,
                "aboutMe" to friend.aboutMe,
                "imageURLs" to friend.imageURLs,
                "friendsList" to friend.friendsList

        )
        db.collection("users").document(CurrentUser.userID.toString()).update(
                "friendsList",
                FieldValue.arrayUnion(userMap)
            )

        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    private fun createFriendsListJSON() {

    }

    private fun collectFriendsListFromJSON() {

    }
}

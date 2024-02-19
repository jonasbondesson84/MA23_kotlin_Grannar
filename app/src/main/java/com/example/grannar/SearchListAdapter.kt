
package com.example.grannar

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class SearchListAdapter(val context: Context, private val searchList: MutableList<User>,private val listener: MyAdapterListener) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private val db = Firebase.firestore



    interface MyAdapterListener {
        fun onAddFriendsListener(user: User)
        fun onSendMessageListener(user: User)
    }

    var onUserClick: ((User) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.friend_name)
        val tvAge: TextView = itemView.findViewById(R.id.age)
        val addFriend: LinearLayout = itemView.findViewById(R.id.addFriend_linearLayout)
        val btnAddFriend: ImageButton = itemView.findViewById(R.id.friend_request_icon)
        val sendMessage: LinearLayout = itemView.findViewById(R.id.sendMessage)
        val btnSendMessage: ImageButton = itemView.findViewById(R.id.message_icon)

        val interestsTextViewList = mutableListOf<TextView>(
            itemView.findViewById(R.id.interest1ListItemTextView),
            itemView.findViewById(R.id.interest2ListItemTextView),
            itemView.findViewById(R.id.interest3ListItemTextView),
            itemView.findViewById(R.id.interest4ListItemTextView),
            itemView.findViewById(R.id.interest5ListItemTextView),
            itemView.findViewById(R.id.interest6ListItemTextView),
        )



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
        clearInterestsTextViews(holder.interestsTextViewList)
        holder.tvName.text = selectedUser.firstName
        holder.tvAge.text = "Age: ${selectedUser.getAgeSpan() }"

        //If a user is in CurrentUsers friendslist, the add friend button gets removed
        if(CurrentUser.friendsList?.any{it.userID == selectedUser.userID} == true) {
            holder.addFriend.visibility = View.INVISIBLE
        }

        holder.btnSendMessage.setOnClickListener {
            if(CurrentUser.userID != null) {
                listener.onSendMessageListener(selectedUser)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }
        holder.sendMessage.setOnClickListener {
            if(CurrentUser.userID != null) {
                listener.onSendMessageListener(selectedUser)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }

        holder.itemView.setOnClickListener {
            if(CurrentUser.userID != null) {
                onUserClick?.invoke(selectedUser)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }
        holder.btnAddFriend.setOnClickListener {
            if(CurrentUser.userID != null) {
            saveFriend(selectedUser, position)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }
        holder.addFriend.setOnClickListener {
            if(CurrentUser.userID != null) {
                saveFriend(selectedUser, position)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }

        val interests = selectedUser.interests
        Log.d("!!!", "Interests ${interests?.size}")
        if (interests != null){
            addInterests(interests, holder.interestsTextViewList)
        }



    }

    private fun addInterests(interests: List<Interest>, interestTextViewList: List<TextView> ){
        interests.forEachIndexed{index, interest ->
            interestTextViewList[index].text = interest.name
            interest.category?.colorID?.let { interestTextViewList[index].setBackgroundColor(context.resources.getColor(it)) }
        }
    }

    private fun clearInterestsTextViews(interestTextViewList: List<TextView>){
        interestTextViewList.forEach{textView ->
            textView.text = ""
            textView.setBackgroundColor(0)
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

        CurrentUser.friendsList?.remove(selectedUser)
        notifyItemRemoved(position)
    }

    private fun saveFriend(friend: User, position: Int) {
        CurrentUser.friendsList?.add(friend)
        Log.d("!!!", CurrentUser.userID.toString())
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
            ).addOnCompleteListener {
                Log.d("!!!", "saved friend")
        }

        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

}

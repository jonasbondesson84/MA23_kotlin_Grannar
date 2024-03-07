package com.example.grannar

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class SearchListAdapter(
    val context: Context,
    private val searchList: MutableList<User>,
    private val listener: MyAdapterListener
) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {


    private val layoutInflater = LayoutInflater.from(context)
    private val db = Firebase.firestore


    interface MyAdapterListener {
        fun onAddFriendsListener(user: User)
        fun onSendMessageListener(user: User)
        fun goToUser(user: User, card: ConstraintLayout)
    }

    var onUserClick: ((User) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.friend_name)
        val tvAge: TextView = itemView.findViewById(R.id.age)
        val addFriend: LinearLayout = itemView.findViewById(R.id.addFriend_linearLayout)
        val btnAddFriend: ImageButton = itemView.findViewById(R.id.friend_request_icon)
        val sendMessage: LinearLayout = itemView.findViewById(R.id.sendMessage)
        val btnSendMessage: ImageButton = itemView.findViewById(R.id.message_icon)
        val linearLayoutFriend: ConstraintLayout = itemView.findViewById(R.id.linearLayoutFriend)
        val tvGender: TextView = itemView.findViewById(R.id.friend_gender)
        val profileImageView: ImageView = itemView.findViewById(R.id.friend_image)
        val tvDistance: TextView = itemView.findViewById(R.id.distance)

        val interestsChips = mutableListOf<Chip>(
            itemView.findViewById(R.id.interest1Chip),
            itemView.findViewById(R.id.interest2Chip),
            itemView.findViewById(R.id.interest3Chip),
            itemView.findViewById(R.id.interest4Chip),
            itemView.findViewById(R.id.interest5Chip),
            itemView.findViewById(R.id.interest6Chip),
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
        clearInterestsChips(holder.interestsChips)
        holder.tvName.text = selectedUser.firstName
        holder.tvAge.text = "Age: ${selectedUser.getAgeSpan()}"
        holder.tvDistance.text = "${selectedUser.showDistanceSpan()} away"
        holder.linearLayoutFriend.transitionName = selectedUser.userID
        holder.tvGender.text = selectedUser.gender

        //If a user is in CurrentUsers friendslist, the add friend button gets removed
        if (CurrentUser.friendsUIDList.contains(selectedUser.userID.toString())) {
            holder.addFriend.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            if (CurrentUser.userID != null) {

                listener.goToUser(selectedUser, holder.linearLayoutFriend)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }
        holder.btnSendMessage.setOnClickListener {
            if (CurrentUser.userID != null) {
                listener.onSendMessageListener(selectedUser)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }
        holder.sendMessage.setOnClickListener {
            if (CurrentUser.userID != null) {
                listener.onSendMessageListener(selectedUser)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }

        holder.btnAddFriend.setOnClickListener {
            if (CurrentUser.userID != null) {
                saveFriend(selectedUser, position)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }
        holder.addFriend.setOnClickListener {
            if (CurrentUser.userID != null) {
                saveFriend(selectedUser, position)
            } else {
                listener.onAddFriendsListener(selectedUser)
            }
        }

        val interests = selectedUser.interests

        if (interests != null) {
            addInterests(interests, holder.interestsChips)
        }


        if (selectedUser.profileImageURL != null) {
            Glide.with(context)
                .load(selectedUser.profileImageURL)
                .placeholder(R.drawable.img_album)
                .error(R.drawable.img_album)
                .centerCrop()
                .into(holder.profileImageView)
        } else {
            holder.profileImageView.setImageResource(R.drawable.avatar)
        }
    }

    private fun addInterests(interests: List<Interest>, interestChips: List<Chip>) {
        interests.forEachIndexed { index, interest ->
            interestChips[index].text = interest.name

            val backgroundColor = ContextCompat.getColor(
                context,
                CategoryManager.getCategoryColorId(interest.category)
            )
            interestChips[index].chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
            interestChips[index].isVisible = true
            interestChips[index].setTextColor(
                getColorStateList(
                    context,
                    CategoryManager.getCategoryTextColorID(interest.category)
                )
            )

        }
    }

    private fun clearInterestsChips(interestChips: List<Chip>) {
        interestChips.forEach { chip ->
            chip.text = ""

            chip.chipBackgroundColor = ColorStateList.valueOf(0)
            chip.isVisible = false
        }
    }


    fun removeFriend(position: Int) {
        val selectedUser = searchList[position]
        db.collection("users").document(CurrentUser.userID.toString()).update(
            "friendsUIDList",
            FieldValue.arrayRemove(selectedUser.userID.toString())
        )

        CurrentUser.friendsUIDList?.remove(selectedUser.userID.toString())
        notifyItemRemoved(position)
    }

    private fun saveFriend(friend: User, position: Int) {
        CurrentUser.friendsUIDList.add(friend.userID.toString())
        db.collection("users").document(CurrentUser.userID.toString()).update(
            "friendsUIDList",
            FieldValue.arrayUnion(friend.userID)
        ).addOnCompleteListener {

        }

        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

}

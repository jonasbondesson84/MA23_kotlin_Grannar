
package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.util.Date

class SearchListAdapter(context: Context, private val searchList: MutableList<User>) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)


    var onUserClick: ((User) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.friend_name)
        val btnAddFriend: ImageView = itemView.findViewById(R.id.friend_request_icon)
        val tvAge: TextView = itemView.findViewById(R.id.age)
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
        //holder.tvAge.text = "Age: ${selectedUser.age?.let { calculateAge(it)} }"

//        //If a user is in CurrentUsers friendslist, the add friend button gets removed
//        if(selectedUser in CurrentUser.friendsList) {
//            holder.btnAddFriend.visibility = View.INVISIBLE
//        }

        holder.itemView.setOnClickListener {
            onUserClick?.invoke(selectedUser)
        }
        holder.btnAddFriend.setOnClickListener {

            saveFriend(selectedUser, position)

        }

    }

    private fun calculateAge(age: Date): String {
        val year = age.year
        val currentYear = LocalDateTime.now().year
        when (currentYear - year) {
            in 2..10 -> {
                return "1 - 10"
            }
            in 11 .. 20 -> {
                return "11 - 20"
            }
            in 21 .. 30 -> {
                return "21 - 30"
            }
            in 31 .. 40 -> {
                return "31 - 40"
            }
            in 41 .. 50 -> {
                return "41 - 50"
            }
            in 51 .. 60 -> {
                return "51 - 60"
            }
            in 61 .. 70 -> {
                return "61 - 70"
            }
            else -> return "70+"
        }

    }

    fun removeFriend( position: Int) {
        val selectedUser = searchList[position]
        CurrentUser.friendsList.remove(selectedUser)
        notifyItemRemoved(position)
    }

    private fun saveFriend(friend: User, position: Int) {
        CurrentUser.friendsList.add(friend)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }
}

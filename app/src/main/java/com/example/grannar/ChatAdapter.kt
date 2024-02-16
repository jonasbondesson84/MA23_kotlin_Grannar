package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(context: Context, val messages: MutableList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    inner class ViewHolderFromCurrentUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessageFrom: TextView = itemView.findViewById(R.id.tvChatFromMessage)

    }
    inner class ViewHolderToCurrentUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessageTo: TextView = itemView.findViewById(R.id.tvChatToMessage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 1) {
            ViewHolderFromCurrentUser(layoutInflater.inflate(R.layout.item_message_from_currentuser, parent, false))
        }else {
            ViewHolderToCurrentUser(layoutInflater.inflate(R.layout.item_message_to_currentuser, parent, false))
    }

    }
    override fun getItemViewType(position: Int): Int {
        return if(messages[position].fromID == CurrentUser.userID) {
            0
        } else 1

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position) == 1) {
            val fromHolder = holder as ViewHolderFromCurrentUser
            fromHolder.tvMessageFrom.text = messages[position].message
        } else {
            val toHolder = holder as ViewHolderToCurrentUser
            toHolder.tvMessageTo.text = messages[position].message
        }


    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
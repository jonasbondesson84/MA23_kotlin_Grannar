package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(context: Context, private val chatList: MutableList<Chats>,private val listener: MessageAdapter.MyAdapterListener): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    interface MyAdapterListener {
        fun goToMessage(user: User)

    }
    var onUserClick: ((User) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMessageName)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessageChatMessage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_messages,parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        val message = chatList[position].lastMessage.text
        val fromUser = chatList[position].fromUser
        holder.tvName.text = fromUser.firstName
        holder.tvMessage.text = message

        holder.itemView.setOnClickListener {
            listener.goToMessage(fromUser)
        }

    }

    override fun getItemCount(): Int {
       return chatList.size
    }
}
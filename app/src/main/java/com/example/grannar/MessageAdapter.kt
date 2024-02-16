package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(context: Context, val messages: MutableList<Chats>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMessageName)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessageChatMessage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_messages,parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        val message = messages[position].messages?.last()
        val fromUser = message?.fromID
        holder.tvName.text = fromUser
        holder.tvMessage.text = message?.message

    }

    override fun getItemCount(): Int {
       return messages.size
    }
}
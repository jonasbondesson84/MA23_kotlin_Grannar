package com.example.grannar

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class MessageAdapter(context: Context, private val chatList: MutableList<Chats>,private val listener: MessageAdapter.MyAdapterListener): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    interface MyAdapterListener {
        fun goToMessage(user: User)

    }
    var onUserClick: ((User) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMessageName)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessageChatMessage)
        val tvTimeStamp : TextView = itemView.findViewById(R.id.tvMessageTimeStamp)
        val imProfileImage: ImageView = itemView.findViewById(R.id.imMesssageImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_messages,parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        val message = chatList[position].lastMessage
        val fromUser = chatList[position].fromUser
        holder.tvName.text = fromUser.firstName

        if(message.fromID.toString() == CurrentUser.userID.toString()) {
            holder.tvMessage.text = "You: ${message.text} "
        } else {
            holder.tvMessage.text = "${fromUser.firstName}: ${message.text}"

        }
        holder.tvTimeStamp.text = setTimeText(message.timeStamp)
        if(message.unread && message.toID == CurrentUser.userID.toString()) {
            showAsUnread(holder)
        }
        Glide
            .with(holder.itemView.context)
            .load(fromUser.profileImageURL)
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .into(holder.imProfileImage)

        holder.itemView.setOnClickListener {
            listener.goToMessage(fromUser)
        }

    }

    private fun showAsUnread(holder: ViewHolder) {
        holder.tvMessage.setTypeface(null, Typeface.BOLD)
        holder.tvName.setTypeface(null, Typeface.BOLD)
        holder.tvTimeStamp.setTypeface(null, Typeface.BOLD)
    }


    private fun setTimeText(timeStamp: Date?): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val sendTimeStamp = formatter.format(timeStamp)

        val sendDateTime = formatter.parse(sendTimeStamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val currentDateTime = LocalDateTime.now()

        val timeDifference = Duration.between(sendDateTime, currentDateTime)
        when(timeDifference.toMinutes()) {
            in 0 .. 1 -> {
                return "now"
            }
            in 1 .. 60 -> {
                return "${timeDifference.toMinutes()} minutes ago"
            }
            in 60 .. 1440 -> {
                return "${timeDifference.toHours()} hours ago"
            }
            else -> {
                val formatterDays = SimpleDateFormat("EEE, dd MMM, yyyy", Locale.getDefault())

                return formatterDays.format(timeStamp)
            }
        }

        return timeDifference.seconds.toString()

    }

    override fun getItemCount(): Int {
       return chatList.size
    }
}
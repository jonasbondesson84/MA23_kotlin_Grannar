package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class ChatAdapter(context: Context, val messages: MutableList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    inner class ViewHolderFromCurrentUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessageFrom: TextView = itemView.findViewById(R.id.tvChatFromMessage)
        val tvTimeStampFrom: TextView = itemView.findViewById(R.id.tvChatFromTimeStamp)

    }
    inner class ViewHolderToCurrentUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessageTo: TextView = itemView.findViewById(R.id.tvChatToMessage)
        val tvTimeStampTo: TextView = itemView.findViewById(R.id.tvChatToTimeStamp)

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
            fromHolder.tvMessageFrom.text = messages[position].text

            val timeStamp = messages[position].timeStamp
            //setTimeText(timeStamp)
            var timeText = ""
            if(timeStamp != null) {
                 timeText = setTimeText(timeStamp)
            } else {
                timeText = "null"
            }
            fromHolder.tvTimeStampFrom.text = timeText
//            fromHolder.tvTimeStampFrom.text = DateFormat.getDateTimeInstance().format(timeStamp).toString()
        } else {
            val toHolder = holder as ViewHolderToCurrentUser
            toHolder.tvMessageTo.text = messages[position].text
            val timeStamp = messages[position].timeStamp
            var timeText = ""
            if(timeStamp != null) {
                timeText = setTimeText(timeStamp)
            } else {
                timeText = "null"
            }

            toHolder.tvTimeStampTo.text = timeText
        }


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
        return messages.size
    }
}
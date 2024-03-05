package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.Locale


class EventAdapter(val context: Context, private val eventList: MutableList<Event>, private val listener: EventAdapter.MyAdapterListener): RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    interface MyAdapterListener {
        fun goToEvent(event: Event, card: ConstraintLayout)
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvDate: TextView = itemView.findViewById(R.id.tvEventDate)
        val imEvent: ImageView = itemView.findViewById(R.id.imEventImage)
        val constEvent: ConstraintLayout = itemView.findViewById(R.id.constraintEvent)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_event,parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventList[position]

        holder.tvName.text = event.name
        val formattedDate =
            event.startDateTime?.let {
                SimpleDateFormat("EEE dd MMM ''yy HH:mm", Locale.getDefault()).format(
                    it
                )
            }
        holder.tvDate.text = formattedDate
        holder.constEvent.transitionName = event.docID
        holder.itemView.setOnClickListener {
            listener.goToEvent(event, holder.constEvent )
        }
        Glide
            .with(holder.itemView.context)
            .load(event.imageURL)
            .centerCrop()

            .placeholder(R.drawable.img_album)
            .error(R.drawable.img_album)
            .into(holder.imEvent)
            .apply {
                RequestOptions().dontTransform()
            }

    }


    override fun getItemCount(): Int {
        return eventList.size
    }
}
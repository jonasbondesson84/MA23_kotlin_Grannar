package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(val context: Context, private val eventList: MutableList<Event>, private val listener: EventAdapter.MyAdapterListener): RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    interface MyAdapterListener {
        fun goToEvent(event: Event)

    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvDate: TextView = itemView.findViewById(R.id.tvEventDate)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_event,parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventList[position]

        holder.tvName.text = event.name
        holder.tvDate.text = event.startDateTime.toString()
        holder.itemView.setOnClickListener {
            listener.goToEvent(event)
        }

    }


    override fun getItemCount(): Int {
        return eventList.size
    }
}
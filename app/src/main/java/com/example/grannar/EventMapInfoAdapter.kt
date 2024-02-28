package com.example.grannar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.text.SimpleDateFormat
import java.util.Locale

class EventMapInfoAdapter(val context: Context): GoogleMap.InfoWindowAdapter {

    private val layoutInflater = LayoutInflater.from(context)


    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    override fun getInfoWindow(p0: Marker): View? {
        val infoWindow = layoutInflater.inflate(R.layout.item_event_map, null)
        val tvTitle = infoWindow.findViewById<TextView>(R.id.tvWindowTitle)
        val tvDesc = infoWindow.findViewById<TextView>(R.id.tvWindowDesc)

        val event = p0.tag as? Event

        tvTitle.text = event?.name
        val formattedDate =
            event?.startDateTime?.let {
                SimpleDateFormat("EEE dd MMM ''yy HH:mm", Locale.getDefault()).format(
                    it
                )
            }
        tvDesc.text = formattedDate


        return infoWindow
    }


}
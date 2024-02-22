package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapDialogFragment: DialogFragment() {
    private var onDataPassListener: OnDataPassListener? = null

    private var setLocation: LatLng? = null
    private var userLocation: LatLng? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_fragment_map, null)

        val userLocationLat = arguments?.getDouble("lat")
        val userLocationLng = arguments?.getDouble("lng")

        userLocation = userLocationLat?.let { userLocationLng?.let { it1 -> LatLng(it, it1) } }
        Log.d("!!!",userLocation.toString())





        view.findViewById<Button>(R.id.mapCancelButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.mapAddButton).setOnClickListener {
            if(setLocation != null) {
                onDataPassListener?.onDataPassed(setLocation!!)
                dismiss()

            }

        }


        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        val map = view.findViewById<MapView>(R.id.dialogMapView)
        map.onCreate(savedInstanceState)
        map.getMapAsync { googleMap ->

            val latLng = com.google.android.gms.maps.model.LatLng(
                59.334591,
                18.063240
            )
                val cameraUpdate = userLocation?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) } ?: CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            googleMap.moveCamera(cameraUpdate)
            var marker: Marker? = null

            googleMap.setOnMapClickListener { latLng ->
                marker?.remove()
                marker = googleMap.addMarker(MarkerOptions().position(latLng))
                setLocation = latLng

            }
        }

        return builder.create()


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDataPassListener) {
            onDataPassListener = context
        } else {
            throw RuntimeException("$context must implement OnDataPassListener")
        }
    }
    override fun onDetach() {
        super.onDetach()
        onDataPassListener = null // Avregistrera lyssnare för att undvika minnesläckor
    }

    private fun getUserLocation() {
//
    }
}
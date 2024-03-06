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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapDialogFragment: DialogFragment(), OnMapReadyCallback {
    private var onDataPassListener: OnDataPassListener? = null
    private var onDataEditPassListener: OnDataEditPassListener? = null

    private var setLocation: LatLng? = null
    private var userLocation: LatLng? = null
    private lateinit var map : MapView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_fragment_map, null)

        val userLocationLat = arguments?.getDouble("lat")
        val userLocationLng = arguments?.getDouble("lng")
        val editMode = arguments?.getBoolean("editMode", false)

        userLocation = userLocationLat?.let { userLocationLng?.let { it1 -> LatLng(it, it1) } }
        Log.d("!!!",userLocation.toString())





        view.findViewById<Button>(R.id.mapCancelButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.mapAddButton).setOnClickListener {
            if(setLocation != null) {
                if(editMode == true) {
                    onDataEditPassListener?.onDataPassed(setLocation!!)
                } else {
                    onDataPassListener?.onDataPassed(setLocation!!)
                }
                dismiss()

            }

        }


        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        map = view.findViewById(R.id.dialogMapView)
        map.onCreate(savedInstanceState)
        map.getMapAsync { googleMap ->

            val latLng = com.google.android.gms.maps.model.LatLng(
                59.334591,
                18.063240
            )
                val cameraUpdate = userLocation?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) } ?: CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            googleMap.moveCamera(cameraUpdate)
            var marker: Marker? = null
            if(editMode == true) {
                marker = googleMap.addMarker(MarkerOptions().position(userLocation ?: latLng))
            }
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
        } else if (context is OnDataEditPassListener){
            onDataEditPassListener = context }
        else {
            //throw RuntimeException("$context must implement OnDataPassListener")
        }
    }
    override fun onDetach() {
        super.onDetach()
        onDataEditPassListener = null
        onDataPassListener = null // Avregistrera lyssnare för att undvika minnesläckor
    }
    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {

    }

    fun setOnDataPassListener(listener: OnDataPassListener) {
        onDataPassListener = listener
    }

    fun setOnDataEditPassListener(listener: OnDataEditPassListener) {
        onDataEditPassListener = listener
    }

    private fun getUserLocation() {
//
    }
}
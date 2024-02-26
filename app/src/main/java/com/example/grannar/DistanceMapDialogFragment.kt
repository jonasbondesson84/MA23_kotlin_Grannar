package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.graphics.Color
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.material.slider.Slider
import kotlin.math.ln

interface DistanceSliderListener{
    fun onDistanceSet(distance: Double)
}

class DistanceMapDialogFragment(val distance: Float): DialogFragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var circle: Circle
    private lateinit var map: GoogleMap
    private var distanceSliderListener: DistanceSliderListener? = null
    private var currentZoomLevel = 10.75f
    private val STARTING_ZOOM = 10.75f
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_fragment_map_distance, null)

        mapView = view.findViewById(R.id.distanceDialogMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val distanceSlider = view.findViewById<Slider>(R.id.mapDistanceSlider)

        distanceSlider.value = distance ?: 5.0f
        setCameraZoom(distance ?: 5.0f)

        distanceSlider.addOnChangeListener { slider, value, fromUser ->
            updateCircleRadius(value.toDouble()*1000)
            setCameraZoom(value)
            updateCamera()
        }

        view.findViewById<Button>(R.id.distanceMapCancelButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.distanceMapSetButton).setOnClickListener{
            distanceSliderListener?.onDistanceSet(distanceSlider.value.toDouble())
            dismiss()
        }


        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        return builder.create()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }
            map = googleMap
           // googleMap.isMyLocationEnabled = true



            val initialLatLng = LatLng(CurrentUser.locLat ?:59.334591 , CurrentUser.locLng ?: 18.063240) // Initial coordinates
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, currentZoomLevel))


            val circleOptions = CircleOptions()
                .center(initialLatLng)
                .radius((distance * 1000).toDouble()) // Initial radius in meters (1 km)
                .strokeWidth(2f)
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#30FF0000")) // Transparent red color
            circle = googleMap.addCircle(circleOptions)


    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun updateCircleRadius(radiusInMeters: Double){
        circle.radius = radiusInMeters
        setCameraZoom(radiusInMeters.toFloat())

    }

    fun setDistanceSliderListener(listener: DistanceSliderListener){
        distanceSliderListener = listener
    }

    private fun setCameraZoom(radiusInMeters: Float) {
         currentZoomLevel = (STARTING_ZOOM - (radiusInMeters / 20)).toFloat()
    }

    private fun updateCamera(){
        val cameraUpdate = CameraUpdateFactory.zoomTo(currentZoomLevel)
        map.animateCamera(cameraUpdate)
    }
}

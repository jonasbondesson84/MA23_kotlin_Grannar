package com.example.grannar

import android.app.AlertDialog
import android.app.Dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.slider.Slider


interface DistanceSliderListener{
    fun onDistanceSet(distance: Double)
}

class DistanceMapDialogFragment(val distance: Float): DialogFragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var circle: Circle
    private lateinit var map: GoogleMap
    private lateinit var tvDistance: TextView
    private var distanceSliderListener: DistanceSliderListener? = null
    private var currentZoomLevel = 10.75f
    private val STARTING_ZOOM = 10.75f
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_fragment_map_distance, null)

        mapView = view.findViewById(R.id.distanceDialogMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        tvDistance = view.findViewById(R.id.tvDistance)


        val distanceSlider = view.findViewById<Slider>(R.id.mapDistanceSlider)

        distanceSlider.value = distance
        tvDistance.text = "${distance.toInt()} km"
        setCameraZoom(distance)

        distanceSlider.addOnChangeListener { slider, value, fromUser ->
            tvDistance.text = "${value.toInt()} km"
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

        // KODEN LIGGER KVAR för att om vi ska använda användarens plats senare. Måst fixa en callback eller hantera om användaren svara på permissions


//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                1
//            )
//            return
//        }
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

    private fun setCameraZoom(radiusInKM: Float) {
        // Set to dived by 13 because zoom looks good over Stockholm. Needs to change if we target areas closer to the equator.
         currentZoomLevel = (STARTING_ZOOM - (radiusInKM / 13))


    }

    private fun updateCamera(){
        val cameraUpdate = CameraUpdateFactory.zoomTo(currentZoomLevel)
        map.animateCamera(cameraUpdate)
    }
}


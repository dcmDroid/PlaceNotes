package com.utbmt.placenotes.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.utbmt.placenotes.R
import com.utbmt.placenotes.models.PlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mPlaceDetails: PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                    as PlaceModel
        }

        if(mPlaceDetails != null){

            val supportMapFragment:
                    SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map)
            as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val position = LatLng(mPlaceDetails!!.latitude, mPlaceDetails!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(mPlaceDetails!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 10f)
        googleMap.animateCamera(newLatLngZoom)
    }

}
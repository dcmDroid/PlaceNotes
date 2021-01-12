package com.utbmt.placenotes.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.utbmt.placenotes.R
import com.utbmt.placenotes.database.DatabaseHandler
import com.utbmt.placenotes.models.PlaceModel
import com.utbmt.placenotes.utils.GetAddressFromLatLng
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener{

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    var mPlaceDetails : PlaceModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val titleFieldPopulate = findViewById<AppCompatEditText>(R.id.et_title)
        val descriptionFieldPopulate = findViewById<AppCompatEditText>(R.id.et_description)
        val locationFieldPopulate = findViewById<AppCompatEditText>(R.id.et_location)
        val locationBtn = findViewById<TextView>(R.id.tv_select_current_location)
        val dateFieldPopulate = findViewById<AppCompatEditText>(R.id.et_date)

        val backBtn = findViewById<Button>(R.id.btn_back)
        backBtn.setOnClickListener{
            val backIntent = Intent(this, MainActivity::class.java)
            startActivity(backIntent)
            finish()
        }

        val saveBtn = findViewById<Button>(R.id.btn_save)
        saveBtn.setOnClickListener(this)

        if(!Places.isInitialized()){
            Places.initialize(this@AddPlaceActivity, resources.getString(R.string.google_maps_api_key))

        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as PlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        val datePick = findViewById<AppCompatEditText>(R.id.et_date)
        datePick.setOnClickListener(this)

        if(mPlaceDetails != null) {

            titleFieldPopulate.setText(mPlaceDetails!!.title)
            descriptionFieldPopulate.setText(mPlaceDetails!!.description)
            dateFieldPopulate.setText(mPlaceDetails!!.date)
            locationFieldPopulate.setText(mPlaceDetails!!.location)
            mLatitude = mPlaceDetails!!.latitude
            mLongitude = mPlaceDetails!!.longitude

            saveBtn.text = "Uložit Změny"

        }

        locationFieldPopulate.setOnClickListener(this)
        locationBtn.setOnClickListener(this)

    }
    private fun isLocationEnabled() : Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult!!.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Aktuální šířka", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Aktuální délka", "$mLongitude")

            val locationFieldPopulate = findViewById<AppCompatEditText>(R.id.et_location)
            val addressTask =
                GetAddressFromLatLng(this@AddPlaceActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Adresa ::", "" + address)
                    locationFieldPopulate.setText(address)
                }

                override fun onError() {
                    Log.e("Získat adresu::", "Něco se pokazilo :(")
                }
            })

            addressTask.getAddress()
        }
    }

    override fun onClick(v: View?) {
        val titleField = findViewById<AppCompatEditText>(R.id.et_title)
        val descriptionField = findViewById<AppCompatEditText>(R.id.et_description)
        val locationField = findViewById<AppCompatEditText>(R.id.et_location)
        val dateField = findViewById<AppCompatEditText>(R.id.et_date)


        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@AddPlaceActivity, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            R.id.btn_save ->{

                if(titleField.text.isNullOrEmpty()){
                    Toast.makeText(this, "Pojmenujte místo", Toast.LENGTH_SHORT).show()
                }else if(locationField.text.isNullOrEmpty()){
                    Toast.makeText(this, "Zvolte polohu místa", Toast.LENGTH_SHORT).show()
                }else{
                        val placeModel = PlaceModel(
                            if(mPlaceDetails == null) 0 else mPlaceDetails!!.id,
                            titleField.text.toString(),
                            descriptionField.text.toString(),
                            dateField.text.toString(),
                            locationField.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        if(mPlaceDetails == null){
                            val addPlace = dbHandler.addPlace(placeModel)
                            if(addPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                        }

                        }else {
                            val updatePlace = dbHandler.updatePlace(placeModel)
                            if (updatePlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }

                        }
                    }

            }

            R.id.et_location ->{
                try{
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }catch(e:Exception){
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {

                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Aktivujte poskytovatele polohy",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {

                                    requestNewLocationData()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogForPermissions()
                            }
                        }).onSameThread()
                        .check()
                }
            }

        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val locationField = findViewById<AppCompatEditText>(R.id.et_location)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                locationField.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }


        }
        }

    private fun updateDateInView(){
        val myDateFormat = "dd. MMMM yyyy"
        val sdf = SimpleDateFormat(myDateFormat, Locale.getDefault())
        val datePopulate = findViewById<AppCompatEditText>(R.id.et_date)
        datePopulate.setText(sdf.format(cal.time).toString())
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Vapadá to, že oprávnění přístupu k poloze nebylo uděleno. Přístup lze udělit v nastavení aplikace")
            .setPositiveButton(
                "Přejít do nastavení"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("balíček", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    companion object {
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3

    }

}
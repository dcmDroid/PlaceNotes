package com.utbmt.placenotes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.utbmt.placenotes.R
import com.utbmt.placenotes.models.PlaceModel

class PlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        val detailTitleField = findViewById<TextView>(R.id.tv_title)
        val detailDescriptionField = findViewById<TextView>(R.id.tv_description)
        val detailLocationField = findViewById<TextView>(R.id.tv_location)

        var placeDetailModel : PlaceModel? = null

        val mapBtn = findViewById<Button>(R.id.btn_view_on_map)

        val backBtn = findViewById<Button>(R.id.btn_back)
        backBtn.setOnClickListener{
            val backIntent = Intent(this, MainActivity::class.java)
            startActivity(backIntent)
            finish()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            placeDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as PlaceModel
        }
            if (placeDetailModel != null){

                detailTitleField.text = placeDetailModel?.title
                detailDescriptionField.text = placeDetailModel?.description
                detailLocationField.text = placeDetailModel?.location

                mapBtn.setOnClickListener{
                    val intent = Intent(this, MapActivity::class.java)
                    intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, placeDetailModel)
                    startActivity(intent)
                }

        }


        
    }
}
package com.utbmt.placenotes.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.utbmt.placenotes.R
import com.utbmt.placenotes.adapters.PlacesAdapter
import com.utbmt.placenotes.database.DatabaseHandler
import com.utbmt.placenotes.models.PlaceModel
import com.utbmt.placenotes.utils.SwipeToDeleteCallback
import com.utbmt.placenotes.utils.SwipeToEditCallback


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val placeAdder = findViewById<FloatingActionButton>(R.id.AddNewPlace)
        placeAdder.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getPlacesListFromDatabase()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getPlacesListFromDatabase()
            }
        }
    }

    private fun setupPlacesRecyclerView(placeList: ArrayList<PlaceModel>){
        val listOfPlaces = findViewById<RecyclerView>(R.id.rv_places_list)
        listOfPlaces.layoutManager = LinearLayoutManager(this)
        listOfPlaces.setHasFixedSize(true)
       val placesAdapter = PlacesAdapter(this, placeList)
        listOfPlaces.adapter = placesAdapter

        placesAdapter.setOnClickListener(object: PlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: PlaceModel){
                val intent = Intent(this@MainActivity, PlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
               val adapter = listOfPlaces.adapter as PlacesAdapter
                adapter.notifyEditItem(this@MainActivity,
                        viewHolder.adapterPosition,
                        ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(listOfPlaces)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = listOfPlaces.adapter as PlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getPlacesListFromDatabase()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(listOfPlaces)

    }

    private fun getPlacesListFromDatabase() {
        val listOfPlaces = findViewById<RecyclerView>(R.id.rv_places_list)
        val noItemInRecyclerView = findViewById<TextView>(R.id.tv_no_records_available)
        val dbHandler = DatabaseHandler(this)
        val getPlaceList: ArrayList<PlaceModel> = dbHandler.getPlaceNotesLists()

        if(getPlaceList.size > 0) {
            listOfPlaces.visibility = View.VISIBLE
            noItemInRecyclerView.visibility = View.GONE
            setupPlacesRecyclerView(getPlaceList)
            }else{
            listOfPlaces.visibility = View.GONE
            noItemInRecyclerView.visibility = View.VISIBLE

        }

        }
        companion object {
            var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
            var EXTRA_PLACE_DETAILS = "extra_place_details"
        }

    }

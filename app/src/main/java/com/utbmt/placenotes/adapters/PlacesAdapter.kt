package com.utbmt.placenotes.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utbmt.placenotes.R
import com.utbmt.placenotes.activities.AddPlaceActivity
import com.utbmt.placenotes.activities.MainActivity
import com.utbmt.placenotes.database.DatabaseHandler
import com.utbmt.placenotes.models.PlaceModel

open class PlacesAdapter(
    private val context: Context,
    private var list: ArrayList<PlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    private class MyPlacesViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyPlacesViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_place,
                parent,
                false
            )
        )
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyPlacesViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.tvTitle).text = model.title
            holder.itemView.findViewById<TextView>(R.id.tvDescription).text = model.description
            holder.itemView.findViewById<TextView>(R.id.tvLocation).text = model.location

            holder.itemView.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }


        }

    }

    interface OnClickListener {
        fun onClick(position: Int, model: PlaceModel)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun removeAt(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deletePlace(list[position])
        if(isDeleted > 0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(
                intent,
                requestCode
        )
        notifyItemChanged(position)

    }

}
package com.utbmt.placenotes.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.utbmt.placenotes.models.PlaceModel

class DatabaseHandler (context: Context):
SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PlaceNotesDatabase"
        private const val TABLE_PLACE_NOTES = "PlaceNotesTable"
        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTablePlaceNotes = ("CREATE TABLE " + TABLE_PLACE_NOTES + " ("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(createTablePlaceNotes)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int){
       db!!.execSQL("DROP TABLE IF EXISTS $TABLE_PLACE_NOTES")
        onCreate(db)
    }

    fun addPlace(place: PlaceModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, place.title)
        contentValues.put(KEY_DESCRIPTION, place.description)
        contentValues.put(KEY_DATE, place.date)
        contentValues.put(KEY_LOCATION, place.location)
        contentValues.put(KEY_LATITUDE, place.latitude)
        contentValues.put(KEY_LONGITUDE, place.longitude)

        val result = db.insert(TABLE_PLACE_NOTES, null, contentValues)
        db.close()
        return result
    }

    fun updatePlace(place: PlaceModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, place.title)
        contentValues.put(KEY_DESCRIPTION, place.description)
        contentValues.put(KEY_DATE, place.date)
        contentValues.put(KEY_LOCATION, place.location)
        contentValues.put(KEY_LATITUDE, place.latitude)
        contentValues.put(KEY_LONGITUDE, place.longitude)

        val success = db.update(TABLE_PLACE_NOTES,
                contentValues, KEY_ID + "=" + place.id, null)
        db.close()
        return success
    }

    fun deletePlace(place: PlaceModel) : Int{
        val db = this.writableDatabase
        val success = db.delete(TABLE_PLACE_NOTES,
                KEY_ID + "=" + place.id, null)
        db.close()
        return success
    }

    fun getPlaceNotesLists():ArrayList<PlaceModel> {
        val placeNotesList: ArrayList<PlaceModel> = ArrayList<PlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_PLACE_NOTES"
        val db = this.readableDatabase

        try{
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){
                do {
                    val place = PlaceModel(
                            cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                            cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                            cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                            cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    placeNotesList.add(place)

                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }

        return placeNotesList
    }



}
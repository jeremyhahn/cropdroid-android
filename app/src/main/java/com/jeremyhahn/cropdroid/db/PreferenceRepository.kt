package com.jeremyhahn.cropdroid.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jeremyhahn.cropdroid.Constants.Companion.DATABASE_NAME
import com.jeremyhahn.cropdroid.model.Preference

private const val DATABASE_VERSION = 1
private const val TABLE_PREFERENCES = "prefs"
private const val KEY_ID = "id"
private const val KEY_CONTROLLER_ID = "controller_id"
private const val KEY_VALUE = "value"

class PreferenceRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    var context : Context? = null

    init {
        this.context = context
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createServiceTableSql =
            ("CREATE TABLE " + TABLE_PREFERENCES + "("
                    + KEY_ID + " INTEGER,"
                    + KEY_CONTROLLER_ID + " INTEGER,"
                    + KEY_VALUE + " TEXT,"
                    + "PRIMARY KEY (" + KEY_ID + ", " + KEY_CONTROLLER_ID + ")")
        db.execSQL(createServiceTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PREFERENCES")
        onCreate(db)
    }

    fun drop() {
        context!!.deleteDatabase(DATABASE_NAME)
    }

    fun getCount() : Int {
        val countQuery = "SELECT  * FROM $TABLE_PREFERENCES"
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.rawQuery(countQuery, null)
        cursor.close()
        var count = cursor.getCount()
        db.close()
        return count
    }

    fun add(preference: Preference) : Preference {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_ID, preference.id)
        values.put(KEY_CONTROLLER_ID, preference.controllerId)
        values.put(KEY_VALUE, preference.value)
        db.close()
        return preference
    }

    fun get(id: String, controllerId: Int): Preference {
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_PREFERENCES,
            arrayOf(
                KEY_ID,
                KEY_CONTROLLER_ID,
                KEY_VALUE
            ),
            "$KEY_ID=? AND $KEY_CONTROLLER_ID=$controllerId",
            arrayOf(id.toString(), controllerId.toString()),
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        var preference  = Preference(
            cursor.getString(0),
            cursor.getString(1).toInt(),
            cursor.getString(2)
        )
        db.close()
        return preference
    }

    val all: ArrayList<Preference>
        get() {
            var preferenceList: ArrayList<Preference> = ArrayList<Preference>()
            val selectQuery = "SELECT  * FROM $TABLE_PREFERENCES"
            val db: SQLiteDatabase = this.getWritableDatabase()
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    preferenceList.add(Preference(
                        cursor.getString(0),
                        cursor.getString(1).toInt(),
                        cursor.getString(2)
                    ))
                } while (cursor.moveToNext())
            }
            db.close()
            return preferenceList
        }

    fun update(preference: Preference): Int {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_ID, preference.id)
        values.put(KEY_CONTROLLER_ID, preference.controllerId)
        values.put(KEY_VALUE, preference.value)
        var response = db.update(
            TABLE_PREFERENCES,
            values,
            "$KEY_ID = ? AND $KEY_CONTROLLER_ID = ?",
            arrayOf<String>(preference.id, java.lang.String.valueOf(preference.controllerId))
        )
        db.close()
        return response
    }

    fun delete(preference: Preference) {
        val db: SQLiteDatabase = this.getWritableDatabase()
        db.delete(
            TABLE_PREFERENCES,
            "$KEY_ID = ? AND $KEY_CONTROLLER_ID = ?",
            arrayOf<String>(preference.id, java.lang.String.valueOf(preference.controllerId))
        )
        db.close()
    }
}
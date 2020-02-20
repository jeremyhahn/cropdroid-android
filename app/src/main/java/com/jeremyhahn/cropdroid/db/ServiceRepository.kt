package com.jeremyhahn.cropdroid.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jeremyhahn.cropdroid.DATABASE_NAME
import com.jeremyhahn.cropdroid.model.Service

private const val DATABASE_VERSION = 1
private const val TABLE_SERVICES = "services"
private const val KEY_ID = "id"
private const val KEY_CONTROLLER_ID = "service_id"
private const val KEY_TYPE = "type"

class ServiceRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    var context : Context? = null
    var serviceCount = 0

    init {
        this.context = context
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createServiceTableSql =
            ("CREATE TABLE " + TABLE_SERVICES + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_CONTROLLER_ID + " INTEGER,"
                    + KEY_TYPE + " TEXT)")
        db.execSQL(createServiceTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SERVICES")
        onCreate(db)
    }

    fun drop() {
        context!!.deleteDatabase(DATABASE_NAME)
    }

    fun getCount() : Int {
        val countQuery = "SELECT  * FROM $TABLE_SERVICES"
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.rawQuery(countQuery, null)
        cursor.close()
        var count = cursor.getCount()
        db.close()
        return count
    }

    fun getLastInsertedId(db: SQLiteDatabase) : Int {
        val query = "SELECT  last_insert_rowid()"
        val cursor: Cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        var id = cursor.getInt(0)
        cursor.close()
        return id
    }

    fun add(service: Service) : Service {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_ID, service.id)
        values.put(KEY_CONTROLLER_ID, service.controllerId)
        values.put(KEY_TYPE, service.type)
        db.insert(TABLE_SERVICES, null, values)
        service.id = getLastInsertedId(db)
        db.close()
        return service
    }

    fun get(id: Int): Service {
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_SERVICES,
            arrayOf(
                KEY_ID,
                KEY_CONTROLLER_ID,
                KEY_TYPE
            ),
            "$KEY_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        var service  = Service(
            cursor.getString(0).toInt(),
            cursor.getString(1).toInt(),
            cursor.getString(2)
        )
        db.close()
        return service
    }

    fun getByControllerIdAndType(controllerId : Int, type: String): Service? {
        var service : Service? = null
        val db: SQLiteDatabase = this.getReadableDatabase()
        val cursor: Cursor = db.query(
            TABLE_SERVICES,
            arrayOf(
                KEY_ID,
                KEY_CONTROLLER_ID,
                KEY_TYPE
            ),
            "$KEY_CONTROLLER_ID=? AND $KEY_TYPE=?",
            arrayOf(controllerId.toString(), type),
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        if(cursor.count > 0) {
            service = Service(
                cursor.getString(0).toInt(),
                cursor.getString(1).toInt(),
                cursor.getString(2)
            )
        }
        db.close()
        return service
    }

    val all: ArrayList<Service>
        get() {
            var serviceList: ArrayList<Service> = ArrayList<Service>()
            val selectQuery = "SELECT  * FROM $TABLE_SERVICES"
            val db: SQLiteDatabase = this.getWritableDatabase()
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    serviceList.add(Service(
                        cursor.getString(0).toInt(),
                        cursor.getString(1).toInt(),
                        cursor.getString(2)
                    ))
                } while (cursor.moveToNext())
            }
            db.close()
            return serviceList
        }

    fun update(service: Service): Int {
        val db: SQLiteDatabase = this.getWritableDatabase()
        val values = ContentValues()
        values.put(KEY_ID, service.id)
        values.put(KEY_CONTROLLER_ID, service.controllerId)
        values.put(KEY_TYPE, service.type)
        var response = db.update(
            TABLE_SERVICES,
            values,
            "$KEY_ID = ?",
            arrayOf<String>(java.lang.String.valueOf(service.id))
        )
        db.close()
        return response
    }

    fun delete(service: Service) {
        val db: SQLiteDatabase = this.getWritableDatabase()
        db.delete(
            TABLE_SERVICES,
            "$KEY_ID = ?",
            arrayOf<String>(java.lang.String.valueOf(service.id))
        )
        db.close()
    }
}
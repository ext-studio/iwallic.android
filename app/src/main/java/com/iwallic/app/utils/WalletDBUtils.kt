package com.iwallic.app.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.iwallic.app.models.WalletAgentModel
import com.iwallic.app.utils.WalletEntry.TABLE_NAME
import java.util.*

// Table contents are grouped together in an anonymous object.
object WalletEntry : BaseColumns {
    const val TABLE_NAME = "wallet"
    const val COLUMN_NAME_FILE = "file"
    const val COLUMN_NAME_SNAPSHOT = "snapshot"
    const val COLUMN_NAME_COUNT = "count"
    const val COLUMN_NAME_ADDR = "address"
    const val COLUMN_NAME_UPDATEAT = "update_at"
}

private const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${WalletEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${WalletEntry.COLUMN_NAME_FILE} TEXT," +
                "${WalletEntry.COLUMN_NAME_SNAPSHOT} TEXT," +
                "${WalletEntry.COLUMN_NAME_COUNT} INTEGER," +
                "${WalletEntry.COLUMN_NAME_ADDR} TEXT," +
                "${WalletEntry.COLUMN_NAME_UPDATEAT} INTEGER)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${WalletEntry.TABLE_NAME}"

class WalletDBUtils(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "iWallic.db"
    }
    fun add(file: String, addr: String, snapshot: String? = null, count: Int = 1): Long? {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(WalletEntry.COLUMN_NAME_FILE, file)
            put(WalletEntry.COLUMN_NAME_SNAPSHOT, snapshot)
            put(WalletEntry.COLUMN_NAME_COUNT, count)
            put(WalletEntry.COLUMN_NAME_ADDR, addr)
            put(WalletEntry.COLUMN_NAME_UPDATEAT, System.currentTimeMillis()/1000)
        }
        val newRowId = db?.insert(WalletEntry.TABLE_NAME, null, values)
        db.close()
        return newRowId
    }
    fun remove(id: Long): Boolean {
        val db = this.writableDatabase
        val success = db.delete(TABLE_NAME, BaseColumns._ID + "=?", arrayOf(id.toString())).toLong()
        db.close()
        return Integer.parseInt("$success") != -1
    }
    fun getAll(): ArrayList<WalletAgentModel> {
        val db = readableDatabase
        val projection = arrayOf(
                BaseColumns._ID,
                WalletEntry.COLUMN_NAME_FILE,
                WalletEntry.COLUMN_NAME_SNAPSHOT,
                WalletEntry.COLUMN_NAME_COUNT,
                WalletEntry.COLUMN_NAME_ADDR,
                WalletEntry.COLUMN_NAME_UPDATEAT
        )
        val sortOrder = "${WalletEntry.COLUMN_NAME_UPDATEAT} DESC"
        val cursor = db.query(
                WalletEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        )
        val items = arrayListOf<WalletAgentModel>()
        with(cursor) {
            while (moveToNext()) {
                val item = WalletAgentModel(
                        getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                        getString(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_FILE)),
                        getString(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_SNAPSHOT)),
                        getInt(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_COUNT)),
                        getString(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_ADDR)),
                        getLong(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_UPDATEAT))
                )
                items.add(item)
            }
        }
        db.close()
        return items
    }
    fun get(id: Long): WalletAgentModel? {
        val db = readableDatabase
        val projection = arrayOf(
                BaseColumns._ID,
                WalletEntry.COLUMN_NAME_FILE,
                WalletEntry.COLUMN_NAME_SNAPSHOT,
                WalletEntry.COLUMN_NAME_COUNT,
                WalletEntry.COLUMN_NAME_ADDR,
                WalletEntry.COLUMN_NAME_UPDATEAT
        )
        val cursor = db.query(
                WalletEntry.TABLE_NAME,
                projection,
                BaseColumns._ID + "=?",
                arrayOf(id.toString()),
                null,
                null,
                WalletEntry.COLUMN_NAME_UPDATEAT + " DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                val w = WalletAgentModel(
                    getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                    getString(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_FILE)),
                    getString(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_SNAPSHOT)),
                    getInt(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_COUNT)),
                    getString(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_ADDR)),
                    getLong(getColumnIndexOrThrow(WalletEntry.COLUMN_NAME_UPDATEAT))
                )
                db.close()
                return w
            }
        }
        db.close()
        return null
    }
    fun touch(id: Long): Int? {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(WalletEntry.COLUMN_NAME_UPDATEAT, System.currentTimeMillis()/1000)
        }
        val update = db?.update(WalletEntry.TABLE_NAME, values, BaseColumns._ID + "=?", arrayOf(id.toString()))
        db.close()
        return update
    }
}

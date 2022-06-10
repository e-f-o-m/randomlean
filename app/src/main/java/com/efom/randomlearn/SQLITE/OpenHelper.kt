package com.efom.randomlearn.SQLITE

import android.content.Context
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

class OpenHelper(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Constants_SQLite.CREATE_TABLE_LISTAS)
        db.execSQL(Constants_SQLite.CREATE_TABLE_TARJETAS)
        db.execSQL(Constants_SQLite.CREATE_TABLE_HORARIO)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        @JvmStatic
        fun newInstance(context: Context?): OpenHelper {
            return OpenHelper(context, Constants_SQLite.DB_NAME, null, 1)
        }
    }
}
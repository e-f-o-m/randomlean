package com.efom.randomlearn.database

import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.models.Metadata

@Database(entities = [Card::class, Metadata::class], version = 1, exportSchema = true)
abstract class MyDB : RoomDatabase() {
    abstract fun cardsDAO(): CardsDao
    abstract fun metadataDAO(): MetadataDao

    companion object {
        private var INSTANCE: MyDB? = null
        fun getDB(context: Context): MyDB? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    MyDB::class.java, "db_learncards"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
            }
            return INSTANCE
        }
    }
}
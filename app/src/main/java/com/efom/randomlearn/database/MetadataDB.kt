package com.efom.randomlearn.database
import androidx.room.Database
import androidx.room.RoomDatabase
import com.efom.randomlearn.models.Metadata

@Database(entities = [Metadata::class], version = 1)
abstract class MetadataDB : RoomDatabase() {
    abstract fun metadataDao(): MetadataDao
}

package com.efom.randomlearn.database

import androidx.room.*
import com.efom.randomlearn.models.Metadata

@Dao
interface MetadataDao {
    @Query("SELECT * FROM `Metadata`")
    fun getAll(): List<Metadata>

    @Query("SELECT * FROM `Metadata` WHERE idMetadata IN (:idMetadata)")
    fun getAllByMetadata(idMetadata: Int): List<Metadata>

    @Query("SELECT * FROM `Metadata` WHERE idMetadata IN (:id)")
    fun getById(id: Int): Metadata

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(card: Metadata): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cards: ArrayList<Metadata>)
    @Delete
    fun delete(card: Metadata): Int

    @Update
    fun update(card: Metadata): Int

    @Query("SELECT EXISTS (SELECT * FROM `Metadata`)")
    fun exist(): Boolean

    @Query("SELECT COUNT(idMetadata) FROM `Metadata` WHERE idMetadata IN(:idMetadata)")
    fun countByMetaGroup(idMetadata: Int): Int
}
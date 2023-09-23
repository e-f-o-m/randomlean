package com.efom.randomlearn.database

import androidx.room.*
import com.efom.randomlearn.models.Card
import com.efom.randomlearn.utils.CONSTS

@Dao
interface CardsDao {
    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata)")
    fun getAllByMetadata(idMetadata: Int): List<Card>

    fun getAllByMetaOrder(idMetadata: Int, order: Int): List<Card> {
        return when(order) {
            //0 Aleatorio
            //1 Por Nombre
            //2 Favoritos
            //3 No Favoritos
            //4 Aprendizaje Espaciado
            //5 En orden
            CONSTS.RANDOM -> getAllByMetaRandom(idMetadata)
            CONSTS.NAME -> getAllByMetaName(idMetadata)
            CONSTS.FAVORITE -> getAllByMetaFavorite(idMetadata)
            CONSTS.NOTFAVORITE -> getAllByMetaNotFavorite(idMetadata)
            CONSTS.SPACELEARN_OPTION -> getAllByMetaSpaceLearn(idMetadata)
            CONSTS.INORDER -> getAllByMetadata(idMetadata)
            else -> listOf()
        }
    }

    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata) ORDER BY RANDOM()")
    fun getAllByMetaRandom(idMetadata: Int): List<Card>
    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata) ORDER BY question ASC")
    fun getAllByMetaName(idMetadata: Int): List<Card>
    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata)")
    fun getAllByMetaQuestionDESC(idMetadata: Int): List<Card>

    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata) AND  " +
            "CAST((julianday('now') - 2440587.5)*86400000 AS INTEGER) >= startDate AND " +
            "CAST((julianday('now') - 2440587.5)*86400000 AS INTEGER) <= endDate AND " +
            " difficulty > 0;")
    fun getAllByMetaSpaceLearn(idMetadata: Int): List<Card>
    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata) AND favorite = 1")
    fun getAllByMetaFavorite(idMetadata: Int): List<Card>
    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata) AND favorite = 0")
    fun getAllByMetaNotFavorite(idMetadata: Int): List<Card>
    @Query("SELECT * FROM `Card` WHERE idCard IN (:id)")
    fun getById(id: Int): Card

    @Query("SELECT * FROM `Card` WHERE idMetadata IN (:idMetadata) " +
            "AND startDate < :datetime ")
    fun getRecord(idMetadata: Int, datetime: Long): List<Card>


    @Query("SELECT CAST((julianday('now') - 2440587.5)*86400000 AS INTEGER)")
    fun test(): Long

    @Query("SELECT * FROM `Card` WHERE idCard > :idCard AND idMetadata IN(:idMetadata)")
    fun getNextUp(idCard: Int, idMetadata: Int): Card?

    @Query("SELECT * FROM `Card` WHERE idCard < :idCard AND idMetadata IN(:idMetadata)")
    fun getNextDown(idCard: Int, idMetadata: Int): Card?

    fun getNext(idCard: Int, idMetadata: Int): Card? {
        val card = getNextUp(idCard, idMetadata)
        if(card != null ) {
            return card
        }
        val cardDown = getNextDown(idCard, idMetadata)
        if(cardDown != null ) {
            return cardDown
        }
        return null
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(card: Card): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( cards: ArrayList<Card>)

    @Delete
    fun delete(card: Card): Int

    @Update
    fun update(card: Card): Int

    @Query("UPDATE Card SET difficulty = 5.0, startDate = 0, endDate = 0, type = ${CONSTS.TEXTO_DB} WHERE idMetadata LIKE :idMetadata ")
    fun updateResetAll(idMetadata: Int): Int

    @Query("SELECT EXISTS (SELECT * FROM `Card`)")
    fun exist(): Boolean

    @Query("SELECT COUNT(idCard) FROM `Card` WHERE idMetadata IN(:idMetadata)")
    fun countByMeta(idMetadata: Int): Int

    @Query("SELECT COUNT(idCard) FROM `Card` WHERE idMetadata IN(:idMetadata) AND difficulty = 0.0" )
    fun countByMetaCompleted(idMetadata: Int): Int
}
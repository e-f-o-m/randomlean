package com.efom.randomlearn.database

import com.efom.randomlearn.models.Card
import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Card::class], version = 1)
abstract class CardsDB : RoomDatabase() {
    abstract fun cardsDao(): CardsDao
}

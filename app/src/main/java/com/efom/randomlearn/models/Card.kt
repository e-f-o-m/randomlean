package com.efom.randomlearn.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Card (
    @PrimaryKey(autoGenerate = true)  var idCard: Int? = null,
    var idMetadata: Int? = null,
    var question: String? = null,
    var answer: String? = null,
    var difficulty: Double? = null,
    var details: String? = null,
    var startDate: Long? = null,
    var endDate: Long? = null,
    var favorite: Boolean? = null,
    var type: Int? = null, //spaceLearn, 1, 0
    var state: Int? = null,
    var resourceAnswer: String? = null,
    var resourceQuestion: String? = null,
)
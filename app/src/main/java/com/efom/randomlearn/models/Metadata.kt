package com.efom.randomlearn.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Metadata (
    @PrimaryKey(autoGenerate = true) var idMetadata: Int? = null,
    var name: String? = null,
    var path: String? = null,
    var ico: String? = null,
    var details: String? = null,
    var nSpaceLearn: Int? = null,
    var enabledNotifications: Boolean? = null,
    var type: Int? = null,
    var state: Int? = null,
    var talkOption: Int? = null,
    var order: Int? = null,
)
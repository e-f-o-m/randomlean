package com.efom.randomlearn.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Notification (
    @PrimaryKey(autoGenerate = true) var idNotification: Int? = null,
    var title: String? = null,
    var text: String? = null,
    var bigText: String? = null,
    var nSpaceLearn: Int? = null,
    var enabledNotifications: Boolean? = null,
    var type: Int? = null,
    var state: Int? = null,
    var startDate: Long? = null,
    var endDate: Long? = null,
    var order: Int? = null,
    var ico: String? = null,
)
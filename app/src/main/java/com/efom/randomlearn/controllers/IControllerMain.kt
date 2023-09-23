package com.efom.randomlearn.controllers

import android.content.Context
import android.content.Intent
import com.efom.randomlearn.models.Metadata

interface IControllerMain {

    fun setFileToSqlite(data: Intent, context: Context): Metadata

    fun setNewMetadata(nameMetadata: String, context: Context): Metadata

}
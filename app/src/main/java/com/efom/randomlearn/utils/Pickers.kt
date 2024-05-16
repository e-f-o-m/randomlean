package com.efom.randomlearn.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.util.Calendar

class Pickers(val context: Activity, val _supportFragmentManager: FragmentManager) {
    fun showTimePicker(callback:(time: String?) -> Unit){
        TimePickerFragment(context, object : IComunicationPicker {
            override fun getDate(data: String?) {
                callback(data)
            }
        }).show( _supportFragmentManager, "Hora")
    }

    fun showDatePicker(){
        DatePickerFragment(context).show(_supportFragmentManager, "Fecha")
    }
}

interface IComunicationPicker {
    fun getDate(data: String?)
}

class TimePickerFragment(val context: Activity, _comunicationPicker: IComunicationPicker) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    var selectedTime: String? = null
    val comunicationPicker: IComunicationPicker = _comunicationPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(context, this, hour, minute, android.text.format.DateFormat.is24HourFormat(activity) )
    }
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        selectedTime = "$hourOfDay:$minute"
        comunicationPicker.getDate(selectedTime)
    }
}

class DatePickerFragment(val context: Activity) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var selectedDate: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(context, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        selectedDate = "$year/$month/$day"
    }
}

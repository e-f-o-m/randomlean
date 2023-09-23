package com.efom.randomlearn.utils

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DT {
    private val fecha = GregorianCalendar.getInstance()

    fun day(): Int {
        val value: Calendar = Calendar.getInstance()
        return value.get(Calendar.DAY_OF_MONTH)
    }

    fun day(date: Long): Int {
        val value: Calendar = Calendar.getInstance()
        value.time = toDate(date)!!
        return value.get(Calendar.DAY_OF_MONTH)
    }

    fun month(): String {
        return (fecha[Calendar.MONTH] + 1).toString()
    }

    fun year(): String {
        return fecha[Calendar.YEAR].toString()
    }

    fun fechaFinMes(): String {
        return fecha.getActualMaximum(Calendar.DAY_OF_MONTH)
            .toString() + "/" + month() + "/" + year()
    }

    fun fechaInicioMes(): String {
        return 1.toString() + "/" + (fecha[Calendar.MONTH] + 1) + "/" + fecha[Calendar.YEAR]
    }

    fun getMesAnio(fecha: String?): String? {
        return try {
            val date = SimpleDateFormat("dd/MM/yyyy").parse(fecha)
            SimpleDateFormat("dd/MM/y").format(date)
        } catch (E: ParseException) {
            E.message
        }
    }

    fun formatTexMonth_NunYear(fecha: String?): String? {
        return try {
            val date = SimpleDateFormat("dd/MM/yyyy").parse(fecha)
            var text = SimpleDateFormat("MMMM yyy").format(date)
            text =
                text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1).lowercase(
                    Locale.getDefault()
                )
            text
        } catch (E: ParseException) {
            E.message
        }
    }

    fun diasEntreFechas(fechaInicio: String?, fechaLimite: String?): Int {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val fechaInicial = dateFormat.parse(fechaInicio)
            val fechaFinal = dateFormat.parse(fechaLimite)
            ((fechaFinal.time - fechaInicial.time) / 86400000).toInt()
        } catch (E: ParseException) {
            0
        }
    }

    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @SuppressLint("SimpleDateFormat")
    fun formatYMD(dateLong: Long?): String? {
        val date = dateLong?.let { Date(it) }
        val format = SimpleDateFormat("dd-M-yyyy")
        return date?.let { format.format(it) }
    }

    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    fun start(): Long {
        return Date().time
    }
    fun startMonth(date: Long): Long {
        val cal = Calendar.getInstance()
        cal.time = toDate(date)!!
        cal[Calendar.DAY_OF_MONTH] = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
        return cal.time.time
    }

    fun endMonth(date: Long): Long {
        val cal = Calendar.getInstance()
        cal.time = toDate(date)!!
        cal[Calendar.DAY_OF_MONTH] = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return cal.time.time
    }

    fun endDay(date: Long): Long{
        val calendar = Calendar.getInstance()
        calendar.time = toDate(date)!!
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
        return calendar.time.time
    }


    fun startDay(): Long{
        val calendar = Calendar.getInstance()
        calendar.time = toDate(start())!!
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time.time
    }

    fun endDay(): Long{
        val calendar = Calendar.getInstance()
        calendar.time = toDate(start())!!
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
        return calendar.time.time
    }



    fun end(): Long {
        val instance = Calendar.getInstance()
        instance.set(Calendar.DATE, instance.getActualMaximum(Calendar.DATE))
        return instance.time.time
    }
}
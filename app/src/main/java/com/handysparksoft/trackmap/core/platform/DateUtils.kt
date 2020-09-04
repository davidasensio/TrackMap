package com.handysparksoft.trackmap.core.platform

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getDateFromString(time: String) : String {
        val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        val date = simpleDateFormat.parse(time)
        return simpleDateFormat.format(date)
    }

    fun getDateFromTime(time: Long) : String {
        val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        val formattedDate = simpleDateFormat.format(time)
        return formattedDate

    }
}

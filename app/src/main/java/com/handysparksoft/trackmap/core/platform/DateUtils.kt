package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat

object DateUtils {

    fun getDateFromString(time: String): String {
        val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        val date = simpleDateFormat.parse(time)
        return simpleDateFormat.format(date)
    }

    fun getDateFromTime(time: Long): String {
        val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        val formattedDate = simpleDateFormat.format(time)
        return formattedDate

    }

    fun getRelativeDateFromTime(context: Context, time: Long): String {
        return DateUtils.getRelativeTimeSpanString(context, time, true).toString()
    }
}

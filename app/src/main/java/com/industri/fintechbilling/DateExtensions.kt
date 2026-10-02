package com.industri.fintechbilling

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toReadableIndonesianDate(): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = parser.parse(this) ?: return this
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy - 'Pukul' HH:mm 'WIB'", Locale("id", "ID"))
        formatter.format(date)
    } catch (e: Exception) {
        this
    }
}

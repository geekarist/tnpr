package me.cpele.afk

import java.text.SimpleDateFormat
import java.util.*

fun parseDateTime(dateTimeStr: String?) =
    dateTimeStr?.let {
        SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US).parse(dateTimeStr)
    }
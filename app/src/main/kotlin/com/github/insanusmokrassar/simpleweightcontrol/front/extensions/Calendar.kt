package com.github.insanusmokrassar.simpleweightcontrol.front.extensions

import java.util.*

fun Calendar.year(): Int
        = get(Calendar.YEAR)

fun Calendar.month(): Int
        = get(Calendar.MONTH)

fun Calendar.day(): Int
        = get(Calendar.DAY_OF_MONTH)

fun Calendar.hourOfDay(): Int
        = get(Calendar.HOUR_OF_DAY)

fun Calendar.minutes(): Int
        = get(Calendar.MINUTE)

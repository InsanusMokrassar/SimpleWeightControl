package com.github.insanusmokrassar.simpleweightcontrol.common.models

import android.util.Log
import com.github.insanusmokrassar.SimpleAndroidORM.Autoincrement
import com.github.insanusmokrassar.SimpleAndroidORM.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

data class WeightData(
        val weight: Float,
        val date: Long,
        @PrimaryKey
        @Autoincrement
        val id: Int? = null
) {
        constructor(weight: Float): this(weight, Date().time)

        init {
                Log.i(
                        WeightData::class.java.simpleName,
                        "Weight data: $weight : ${
                        SimpleDateFormat
                                .getDateTimeInstance()
                                .format(date)
                        }"
                )
        }
}

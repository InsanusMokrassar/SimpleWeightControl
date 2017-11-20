package com.github.insanusmokrassar.simpleweightcontrol.back.utils.database

import android.content.Context
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData

class WeightHelper internal constructor(
        c: Context
): MutableListDatabase<WeightData>(
        WeightData::class,
        c,
        c.getString(R.string.standardDatabaseName),
        1,
        "date DESC"
)

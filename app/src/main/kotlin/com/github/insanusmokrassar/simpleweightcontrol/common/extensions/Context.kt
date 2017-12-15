package com.github.insanusmokrassar.simpleweightcontrol.common.extensions

import android.content.Context
import android.util.TypedValue

fun Context.spToPixels(sp: Float): Float =
        TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                resources.displayMetrics
        )

fun Context.spToDp(sp: Float): Float =
        spToPixels(sp) / resources.displayMetrics.density / resources.displayMetrics.scaledDensity

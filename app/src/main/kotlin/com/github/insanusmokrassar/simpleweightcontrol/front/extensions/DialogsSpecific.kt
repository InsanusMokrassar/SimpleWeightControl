package com.github.insanusmokrassar.simpleweightcontrol.front.extensions

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import java.util.*

fun Context.createEditWeightDialog(
        weight: WeightData? = null,
        success: (WeightData) -> Unit
): AlertDialog {
    val builder = AlertDialog.Builder(this)

    val view = LayoutInflater.from(this).inflate(
            R.layout.edit_weight_item,
            null,
            false
    )

    val weightEditText = view.findViewById<EditText>(R.id.weightEditText)
    val weightDatePicker = view.findViewById<DatePicker>(R.id.weightDatePicker)
    val weightTimePicker = view.findViewById<TimePicker>(R.id.weightTimePicker)

    val calendar: Calendar = Calendar.getInstance()
    weight ?. let {
        weightEditText.setText(it.weight.toString())

        calendar.time = Date(it.date)
    } ?: {
        calendar.time = Date(System.currentTimeMillis())
    }()

    weightDatePicker.updateDate(calendar.year(), calendar.month(), calendar.day())

    weightTimePicker.setIs24HourView(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        weightTimePicker.hour = calendar.hourOfDay()
        weightTimePicker.minute = calendar.minutes()
    } else {
        weightTimePicker.currentHour = calendar.hourOfDay()
        weightTimePicker.currentMinute = calendar.minutes()
    }

    weightDatePicker.maxDate = System.currentTimeMillis()

    builder.setView(view)

    var wasError = false
    builder.setPositiveButton(
            android.R.string.ok,
            {
                dialogInterface, i ->
                wasError = try {
                    val actualTime = getCalendar(weightDatePicker, weightTimePicker).timeInMillis
                    if (actualTime > System.currentTimeMillis()) {
                        Toast.makeText(this, getString(R.string.typedWrongTime), Toast.LENGTH_LONG).show()
                        true
                    } else {
                        success(
                                WeightData(
                                        weightEditText.text.toString().toFloat(),
                                        actualTime
                                )
                        )
                        false
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, getString(R.string.typedWrongWeight), Toast.LENGTH_LONG).show()
                    weightEditText.requestFocus()
                    true
                }
            }
    )

    return builder.create().setDismissChecker {
        wasError = !wasError
        wasError
    }
}

fun getCalendar(datePicker: DatePicker, timePicker: TimePicker): Calendar {
    val calendar: Calendar = Calendar.getInstance()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        calendar.set(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.hour,
                timePicker.minute
        )
    } else {
        calendar.set(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.currentHour,
                timePicker.currentMinute
        )
    }

    return calendar
}

package com.github.insanusmokrassar.simpleweightcontrol.front.extensions

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.*
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import java.util.*

fun Context.createEditWeightDialog(
        weight: WeightData? = null,
        success: (WeightData) -> Unit,
        delete: (WeightData) -> Unit = {}
): AlertDialog {
    val builder = AlertDialog.Builder(this)

    val view = LayoutInflater.from(this).inflate(
            R.layout.fragment_edit_weight_item,
            null,
            false
    )

    val weightEditText = view.findViewById<EditText>(R.id.weightEditText)
    val weightDateButton = view.findViewById<Button>(R.id.weightDateButton)
    val weightTimeButton = view.findViewById<Button>(R.id.weightTimeButton)

    val calendar: Calendar = Calendar.getInstance()
    weight ?. let {
        weightEditText.setText(it.weight.toString())

        calendar.time = Date(it.date)

        builder.setNegativeButton(
                getString(R.string.delete),
                { dialogInterface, i ->
                    delete(it)
                    dialogInterface.dismiss()
                }
        )
    } ?: {
        calendar.time = Date(System.currentTimeMillis())
    }()

    weightDateButton.setOnClickListener {
        DatePickerDialog(
                this,
                {
                    _, year, monthOfYear, dayOfMonth ->
                    calendar.set(year, monthOfYear, dayOfMonth)
                },
                calendar.year(),
                calendar.month(),
                calendar.day()
        ).show()
    }

    weightTimeButton.setOnClickListener {
        TimePickerDialog(
                this,
                { _: TimePicker, hours: Int, minutes: Int ->
                    calendar.set(Calendar.HOUR_OF_DAY, hours)
                    calendar.set(Calendar.MINUTE, minutes)
                },
                calendar.hourOfDay(),
                calendar.minutes(),
                true
        ).show()
    }

//    weightDatePicker.updateDate(calendar.year(), calendar.month(), calendar.day())
//
//    weightTimePicker.setIs24HourView(true)
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        weightTimePicker.hour = calendar.hourOfDay()
//        weightTimePicker.minute = calendar.minutes()
//    } else {
//        weightTimePicker.currentHour = calendar.hourOfDay()
//        weightTimePicker.currentMinute = calendar.minutes()
//    }
//
//    weightDatePicker.maxDate = System.currentTimeMillis()

    builder.setView(view)

    var wasError = false
    builder.setPositiveButton(
            android.R.string.ok,
            {
                dialogInterface, i ->
                wasError = try {
                    val actualTime = calendar.timeInMillis
                    if (actualTime > System.currentTimeMillis()) {
                        Toast.makeText(this, getString(R.string.typedWrongTime), Toast.LENGTH_LONG).show()
                        true
                    } else {
                        success(
                                WeightData(
                                        weightEditText.text.toString().toFloat(),
                                        actualTime,
                                        weight ?. id
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
                timePicker.minute,
                0
        )
    } else {
        calendar.set(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.currentHour,
                timePicker.currentMinute,
                0
        )
    }

    return calendar
}

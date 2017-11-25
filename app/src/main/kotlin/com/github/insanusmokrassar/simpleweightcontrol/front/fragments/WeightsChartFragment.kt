package com.github.insanusmokrassar.simpleweightcontrol.front.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.getDateString
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.millisInDay
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.WeightsDaysList
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.calculateAverage
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.getDate
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlin.collections.ArrayList

class WeightsChartFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_weights_chart, container, false)

        context ?.let {
            val chart = view.findViewById<LineChart>(R.id.weightsLineChart)

            chart.isLogEnabled = true

            val setArrayList = ArrayList<Entry>()

            fillDataSet(chart, setArrayList)

            it.weightHelper().databaseObserver.subscribe {
                fillDataSet(chart, setArrayList)
            }

            val set = LineDataSet(setArrayList, "")
            set.lineWidth = 2.5f
            set.circleRadius = 5f
            set.mode = LineDataSet.Mode.CUBIC_BEZIER
            set.setDrawValues(true)
            set.valueTextSize = 10f

            set.axisDependency = YAxis.AxisDependency.LEFT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                set.color = getColor(it, R.color.colorPrimary)
                set.fillColor = getColor(it, R.color.colorPrimaryDark)
                set.valueTextColor = getColor(it, R.color.colorAccent)
                set.setCircleColor(getColor(it, R.color.colorPrimaryDark))
            } else {
                set.color = resources.getColor(R.color.colorPrimary)
                set.fillColor = resources.getColor(R.color.colorPrimaryDark)
                set.valueTextColor = resources.getColor(R.color.colorAccent)
                set.setCircleColor(resources.getColor(R.color.colorPrimaryDark))
            }

            val data = LineData(set)
            chart.data = data

            chart.xAxis.valueFormatter = DatesAxisValueFormatter()
        }

        return view
    }

    private fun fillDataSet(chart: LineChart, setArrayList: ArrayList<Entry>) {
        context ?.let {
            setArrayList.clear()

            WeightsDaysList(it.weightHelper()).reversed().forEach {
                val average = it.calculateAverage()
                val date = it.getDate()
                setArrayList.add(Entry(date.toFloat(), average))
            }

            launch (UI) {
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        }
    }
}

private class DatesAxisValueFormatter : IAxisValueFormatter {
    override fun getFormattedValue(value: Float, axis: AxisBase?): String =
            getDateString(value.toLong())

}

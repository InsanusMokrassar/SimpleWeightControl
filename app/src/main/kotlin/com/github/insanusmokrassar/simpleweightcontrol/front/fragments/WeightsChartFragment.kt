package com.github.insanusmokrassar.simpleweightcontrol.front.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.insanusmokrassar.SimpleAndroidORM.ORMSimpleDatabase.SimpleDatabase
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.getShortDateString
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.millisInDay
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.WeightsDaysMap
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.calculateAverage
import com.github.insanusmokrassar.simpleweightcontrol.common.extensions.spToDp
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch


class WeightsChartFragment: Fragment() {

    private var chartDaysPadding: Int = 0
    private var chartWeightPadding: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context ?.let {
            chartDaysPadding = it.resources.getInteger(
                R.integer.weightDatesChartDaysInViewDaysPadding
            )
            chartWeightPadding = it.resources.getInteger(
                    R.integer.weightDatesChartDaysInViewKgPadding
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weights_chart, container, false).apply {
            val chart = findViewById<LineChart>(R.id.weightsChartLineChart)
            val data = LineData()

            chart.data = data

            chart.legend.isEnabled = false
            chart.description.isEnabled = false
            chart.setScaleEnabled(true)
            chart.setPinchZoom(true)
            chart.setDrawGridBackground(false)

            chart.xAxis.valueFormatter = IAxisValueFormatter {
                value, _ ->
                getShortDateString(value.toLong())
            }
            chart.xAxis.granularity = millisInDay.toFloat()

            val weightsDaysMap = WeightsDaysMap()

            val update: (SimpleDatabase<WeightData>) -> Unit = {
                weightsDaysMap.refresh(it.find())
                fillDataSet(chart, weightsDaysMap)
            }

            update(context.weightHelper())
            context.weightHelper().observable.subscribe(update)
        }
    }

    private fun fillDataSet(
            chart: LineChart,
            map: WeightsDaysMap
    ) {
        try {
            context?.let {
                val weights = ArrayList<Float>()

                val dataset = if (map.isNotEmpty()) {
                    map.toSortedMap().map {
                        val average = it.value.calculateAverage()
                        weights.add(average)
                        Entry(it.key.toFloat(), average)
                    }.let { data ->
                        LineDataSet(data, "Weights data").apply {
                            axisDependency = YAxis.AxisDependency.LEFT
                            lineWidth = resources.getDimension(R.dimen.viewDefaultExtraSmallMargin) / 2
                            circleRadius = resources.getDimension(R.dimen.viewDefaultExtraSmallMargin)
                            valueTextSize = chart.context.spToDp(resources.getDimension(R.dimen.textDefaultSmallSize))
                            setDrawValues(true)
                            valueTextColor = ContextCompat.getColor(chart.context, R.color.colorPrimary)
                            setCircleColor(ContextCompat.getColor(chart.context, R.color.colorAccent))
                            color = ContextCompat.getColor(chart.context, R.color.colorPrimaryDark)
                        }
                    }
                } else {
                    null
                }

                chart.data = dataset ?.let {
                    LineData(it)
                } ?:let {
                    null
                }
                chart.data ?: return

                val daysInView = it.resources.getInteger(
                        R.integer.weightDatesChartDaysInView
                )

                val minDate = map.keys.min()
                val maxDate = map.keys.max()
                val minWeight = weights.min()
                val maxWeight = weights.max()

                maxDate?.let {
                    chart.xAxis.axisMaximum = it.toFloat() + (daysInView + chartDaysPadding) * millisInDay
                }
                minDate?.let {
                    chart.xAxis.axisMinimum = it.toFloat() - (daysInView + chartDaysPadding) * millisInDay
                }

                maxWeight?.let {
                    chart.axisLeft.axisMaximum = it + chartWeightPadding
                }
                minWeight?.let {
                    chart.axisLeft.axisMinimum = it - chartWeightPadding
                }

                map.keys.max()?.toFloat()?.let { chart.moveViewToX(it) }

                launch(UI) {
                    chart.animateX(
                            map.size * resources.getInteger(R.integer.weightChartItemDrawTime)
                    )
                }
            }
        } finally {
            launch (UI) {
                chart.data ?. notifyDataChanged()
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        }
    }
}

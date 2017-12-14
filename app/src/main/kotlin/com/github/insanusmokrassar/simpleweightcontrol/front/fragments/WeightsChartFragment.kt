package com.github.insanusmokrassar.simpleweightcontrol.front.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.ORMSimpleDatabase.SimpleDatabase
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.millisInDay
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.WeightsDaysMap
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.calculateAverage
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.getDate
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlin.collections.ArrayList

class WeightsChartFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_weights_chart, container, false)

        context ?.let {
            val chart = view.findViewById<GraphView>(R.id.weightsChartGraphView)

            chart.viewport.isScrollable = true

            val gridRenderer = chart.gridLabelRenderer
            gridRenderer.labelFormatter = DateAsXAxisLabelFormatter(it)

            chart.viewport.isXAxisBoundsManual = true
            chart.viewport.isYAxisBoundsManual = true

            val series = LineGraphSeries<DataPoint>()
            series.isDrawDataPoints = true
            series.dataPointsRadius = resources.getDimension(R.dimen.viewDefaultSmallMargin)

            chart.addSeries(series)

            val weightsDaysMap = WeightsDaysMap()

            val update: (SimpleDatabase<WeightData>) -> Unit = {
                weightsDaysMap.refresh(it.find())
                fillDataSet(chart, series, weightsDaysMap)
            }

            update(it.weightHelper())
            it.weightHelper().observable.subscribe(update)
        }

        return view
    }

    private fun fillDataSet(
            chart: GraphView,
            series: LineGraphSeries<DataPoint>,
            map: WeightsDaysMap
    ) {
        context ?.let {
            val data = ArrayList<DataPoint>()

            map.filter { it.value.isNotEmpty() }.forEach {
                val average = it.value.calculateAverage()
                data.add(DataPoint(it.key.toDouble(), average.toDouble()))
            }

            val daysInView = it.resources.getInteger(
                    R.integer.weightDatesChartDaysInView
            ).let {
                if (it > data.size) {
                    data.size
                } else {
                    it
                }
            }
            val chartDaysPadding = it.resources.getInteger(
                    R.integer.weightDatesChartDaysInViewDaysPadding
            )
            val chartKgPadding = it.resources.getInteger(
                    R.integer.weightDatesChartDaysInViewKgPadding
            )

            val weights = data.map { it.y }

            val minDate = map.keys.min()
            val maxDate = map.keys.max()
            val minWeight = weights.min()
            val maxWeight = weights.max()

            data.sortBy { it.x }

            launch (UI) {
                minDate ?.toDouble() ?.let {
                    chart.viewport.setMinX(it - (daysInView + chartDaysPadding) * millisInDay)
                }
                maxDate ?.toDouble() ?.let {
                    chart.viewport.setMaxX(it + chartDaysPadding * millisInDay)
                }
                minWeight ?.let {
                    chart.viewport.setMinY(it - chartKgPadding)
                }
                maxWeight ?.let {
                    chart.viewport.setMaxY(it + chartKgPadding)
                }
                chart.gridLabelRenderer.numHorizontalLabels = resources.getInteger(
                        R.integer.weightDatesChartNumHorizontalLabels
                )
                series.resetData(data.toTypedArray())
            }
        }
    }
}

package com.github.insanusmokrassar.simpleweightcontrol.front.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.common.ORMSimpleDatabase.SimpleDatabase
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.WeightsDaysMap
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.WeightDateHolderAdapter
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.RecyclerViewAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class DatesOfWeightsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dates_of_weights, container, false)

        context ?. let {
            val weightsDaysMap = WeightsDaysMap()
            val list = ArrayList<Pair<Long, List<WeightData>>>()

            val adapter = RecyclerViewAdapter(
                    {
                        parent, _, _ ->
                        WeightDateHolderAdapter(layoutInflater, parent)
                    },
                    list
            )

            val update: (SimpleDatabase<WeightData>) -> Unit = {
                weightsDaysMap.refresh(it.find())
                list.clear()
                list.addAll(weightsDaysMap.pairs().sortedByDescending { it.first })
                launch (UI) {
                    adapter.notifyDataSetChanged()
                }
            }

            update(it.weightHelper())
            it.weightHelper().observable.subscribe(update)

            adapter.emptyView = view.findViewById(R.id.emptyWeightListView)

            val recyclerView = view.findViewById<RecyclerView>(R.id.weightsRecyclerView)
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(
                    DividerItemDecoration(
                            it,
                            DividerItemDecoration.VERTICAL
                    )
            )
        }

        return view
    }
}

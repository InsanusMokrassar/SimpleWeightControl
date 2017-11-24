package com.github.insanusmokrassar.simpleweightcontrol.front.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.WeightHelper
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.lists.WeightsDaysList
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.WeightDateHolderAdapter
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.RecyclerViewAdapter
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditWeightDialog
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class HomeActivity: AppCompatActivity() {
    private var adapter: RecyclerViewAdapter<List<WeightData>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val helper = WeightHelper(this)

        adapter = RecyclerViewAdapter(
                {
                    parent: ViewGroup,
                    _: Int,
                    _: RecyclerViewAdapter<List<WeightData>> ->
                    WeightDateHolderAdapter(layoutInflater, parent)
                },
                WeightsDaysList(helper)
        )

        helper.databaseObserver.subscribe {
            launch (UI) {
                adapter ?. notifyDataSetChanged()
            }
        }

        adapter ?. emptyView = findViewById(R.id.emptyWeightListView)

        val recyclerView = findViewById<RecyclerView>(R.id.weightsRecyclerView)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addWeightMenuItem) {
            createEditWeightDialog (
                    success = { weightData ->
                        WeightHelper(this).insert(weightData)
                        adapter ?. notifyDataSetChanged()
                    }
            ).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
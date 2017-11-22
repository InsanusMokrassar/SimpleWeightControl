package com.github.insanusmokrassar.simpleweightcontrol.front.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.WeightHelper
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.WeightViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.RecyclerViewAdapter
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditWeightDialog

class HomeActivity: AppCompatActivity() {
    private var adapter: RecyclerViewAdapter<WeightData>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        adapter = RecyclerViewAdapter(
                {
                    parent: ViewGroup,
                    _: Int,
                    adapter: RecyclerViewAdapter<WeightData> ->
                    WeightViewHolder(layoutInflater, parent, adapter)
                },
                WeightHelper(this)
        )

        adapter ?. emptyView = findViewById(R.id.emptyWeightListView)

        findViewById<RecyclerView>(R.id.weightsRecyclerView).adapter = adapter
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
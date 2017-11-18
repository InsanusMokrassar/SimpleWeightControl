package com.github.insanusmokrassar.simpleweightcontrol.front.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightsDatabase
import com.github.insanusmokrassar.simpleweightcontrol.common.models.WeightData
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.WeightViewHolder
import com.github.insanusmokrassar.simpleweightcontrol.front.RecyclerView.common.RecyclerViewAdapter
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditTextDialog
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*

class HomeActivity: AppCompatActivity() {
    private var adapter: RecyclerViewAdapter<WeightData>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        adapter = RecyclerViewAdapter({
            parent: ViewGroup,
            _: Int,
            _: RecyclerViewAdapter<WeightData> ->
            WeightViewHolder(layoutInflater, parent)
        })

        adapter ?. emptyView = findViewById(R.id.emptyWeightListView)

        findViewById<RecyclerView>(R.id.weightsRecyclerView).adapter = adapter

        updateWeightList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addWeightMenuItem) {
            createEditTextDialog(
                    {
                        text ->
                        try {
                            weightsDatabase().insert(
                                    WeightData(
                                            text.toFloat()
                                    )
                            )
                            updateWeightList()
                            true
                        } catch (e: NumberFormatException) {
                            false
                        }
                    },
                    R.string.typeWeightOffer,
                    inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
            ).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateWeightList(page: Int = 0) {
        adapter ?.let {
            async {
                val items = weightsDatabase().lastWeights(page, 20).toTypedArray()
                launch (UI) {
                    if (page == 0) {
                        it.clear()
                    }
                    it.addItems(*items)
                }
            }
        }
    }
}
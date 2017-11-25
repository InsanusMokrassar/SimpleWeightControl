package com.github.insanusmokrassar.simpleweightcontrol.front.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.github.insanusmokrassar.simpleweightcontrol.R
import com.github.insanusmokrassar.simpleweightcontrol.back.utils.database.weightHelper
import com.github.insanusmokrassar.simpleweightcontrol.front.extensions.createEditWeightDialog

class HomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addWeightMenuItem) {
            createEditWeightDialog (
                    success = {
                        weightHelper().insert(it)
                    }
            ).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
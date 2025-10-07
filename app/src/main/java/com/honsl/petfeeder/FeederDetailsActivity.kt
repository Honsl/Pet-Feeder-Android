package com.honsl.petfeeder

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.honsl.petfeeder.R.id.textViewFeederName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FeederDetailsActivity : AppCompatActivity() {
    var feeder: Feeder? = null
    var schedule: MutableList<Schedule>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feeder_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val app = applicationContext as GlobalFeeder
        feeder = app.feeder
        schedule = feeder?.schedule?.map {it.copy() }?.toMutableList()
        findViewById<TextView>(textViewFeederName)?.text = (feeder?.name + "'s Feeder")

        findViewById<Button>(R.id.buttonFeedNow).setOnClickListener {
            //TODO set the feeder
        }
        findViewById<Button>(R.id.buttonDeleteFeeder).setOnClickListener {
            //TODO delete the feeder
        }

    }

    override fun onStop() {
        saveFeeder()
        super.onStop()
    }

    override fun onDestroy() {
        saveFeeder()
        super.onDestroy()
    }

     fun saveFeeder() {
        val app = applicationContext as GlobalFeeder
        //check if any of the feeder information has changed
         if (this.schedule != app.feeder?.schedule) {
             JsonManager(this).updateFeeder(app.feeder!!)
             val wifi =  WifiManager(this, "http://" + feeder!!.ipAddress)
             GlobalScope.launch {
                 try {
                     val response = wifi.apiService.setSchedule(app.feeder?.schedule)
                     Log.d("FeederDetails", "Feed response: $response")
                 } catch (e: Exception) {
                     Log.e("FeederDetails", "Feeding failed", e)
                 }
             }
         }
    }
}
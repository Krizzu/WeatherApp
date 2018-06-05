package com.krizzu.weatherapp.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*


class TimeService : Service() {
    private val localBinder = LocalBinder()
    private val timer = Timer()
    private val callbacksList: MutableList<(String) -> Unit> = mutableListOf()

    override fun onBind(intent: Intent?): IBinder? {
        Toast
            .makeText(this, "TimeService bound", Toast.LENGTH_SHORT)
            .show()

        timer.schedule(object : TimerTask() {
            override fun run() {
                for (cb in callbacksList) {
                    cb(getCurrentTime())
                }
            }
        }, 0, 1000)
        return localBinder
    }

    override fun onCreate() {
        Toast
            .makeText(this, "TimeService created", Toast.LENGTH_SHORT)
            .show()

        super.onCreate()
    }


    override fun onDestroy() {
        Toast
            .makeText(this, "TimeService destroyed", Toast.LENGTH_SHORT)
            .show()
        timer.cancel()
        super.onDestroy()
    }

    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat.getTimeInstance()
        return timeFormat.format(Calendar.getInstance().time)
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimeService {
            return this@TimeService
        }

        fun subscribeToTimeUpdate(callback: (currentDate: String) -> Unit) {
            callbacksList.add(0, callback)
            callback(getCurrentTime())
        }
    }


}
package com.krizzu.weatherapp

import android.animation.AnimatorInflater
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.krizzu.weatherapp.services.TimeService
import com.krizzu.weatherapp.utils.WeatherStatus
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var weatherStatus: WeatherStatus

    // UI views
    private lateinit var weatherStatusContainer: ConstraintLayout
    private lateinit var tempStatusContainer: ConstraintLayout
    private lateinit var weatherDescriptionContainer: LinearLayout

    private lateinit var tempValue: TextView
    private lateinit var tempDesc: TextView
    private lateinit var currentTime: TextView
    private lateinit var currentCity: TextView

    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var weatherAnimation: LottieAnimationView

    // dev button
    private lateinit var changeTimeOfTheDay: Button
    private lateinit var loadData: Button

    // animation values
    private var isDayMode = true

    // Time service
    private val localServiceConnection = LocalServiceConnection()
    private var timeService: TimeService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar()
        setContentView(R.layout.activity_main)
        prepareReferences()
        sendWeatherRequest()

        changeTimeOfTheDay.setOnClickListener {
            animateWeatherStatus(isDayMode)
        }

        loadData.setOnClickListener {
            runUI()
        }

        /*
        DESIGNS
        https://dribbble.com/shots/2390525-Weather-app-037

        PALETTON
        http://paletton.com/#uid=33i0u0kg0K+5vYpb7SCkcBKo7uI

        API
        https://openweathermap.org/current


        To do list:

        TODO(General: APP crashing on startup without internet connection) [because of TODO in error handler]
        TODO(General: Need to handle bigger screen sizes - use dimensions folder for that)
        TODO(General: Update weather on intervals [maybe sync it in a service]
        TODO(General: App crashlytics, analytics?)

        TODO(WeatherContainer: Add background like in the designs (for day and night))
        TODO(WeatherContainer: Add float button for changing options: city (maybe from google maps?), time zone [based on city])
        TODO(WeatherContainer: Make time and date update on set intervals - in BG, set services)
        TODO(WeatherContainer: Move moon/sun according to actual time [over the ellipsis]

        TODO(InfoContainer: add pictures for each weather, downloadable from the API)
        TODO(InfoContainer: add more information (higest temp, lower temp, wind, huminity info etc.)
        TODO(InfoContainer: create animations for each weather condition)

         */
    }

    override fun onDestroy() {
        unbindService(localServiceConnection)
        super.onDestroy()
    }

    private fun prepareReferences() {
        weatherStatusContainer = findViewById(R.id.ConstraintLayout_main_weatherContainer)
        tempStatusContainer = findViewById(R.id.ConstraintLayout_main_infoContainer)
        weatherDescriptionContainer = findViewById(R.id.LinearLayout_main_descriptionContainer)

        tempValue = findViewById(R.id.TextView_main_temperatureValue)
        tempDesc = findViewById(R.id.TextView_main_weatherDescription)
        currentTime = findViewById(R.id.TextView_main_currentTime)
        currentCity = findViewById(R.id.TextView_main_currentCity)

        loadingAnimation = findViewById(R.id.Lottie_main_loadingAnim)
        weatherAnimation = findViewById(R.id.Lottie_main_weatherAnim)

        changeTimeOfTheDay = findViewById(R.id.Button_mainDEV_AnimateWeather)
        loadData = findViewById(R.id.Button_mainDEV_LoadWeather)
    }

    private fun setupTimeService() {
        val timeServiceIntent = Intent(this, TimeService::class.java)
        bindService(timeServiceIntent, localServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun runUI() {
        setupTimeService()
        loadingAnimation.pauseAnimation()

        tempValue.text = "${weatherStatus.temp.temp}°C"
        tempDesc.text = weatherStatus.weather.description

        loadingAnimation.visibility = View.GONE
        loadData.visibility = View.GONE

        currentCity.visibility = View.VISIBLE
        currentTime.visibility = View.VISIBLE
        weatherStatusContainer.visibility = View.VISIBLE
        tempStatusContainer.visibility = View.VISIBLE

        changeTimeOfTheDay.visibility = View.VISIBLE
    }

    private fun animateWeatherStatus(nightMode: Boolean = false, duration: Long = 1800) {
        val animResource =
            if (nightMode) R.animator.day_to_night_background else R.animator.night_to_day_background
        val animatorSet =
            AnimatorInflater.loadAnimator(this, animResource)
        animatorSet.setTarget(weatherStatusContainer)
        animatorSet.duration = duration

        if (!nightMode) {
            weatherAnimation.setSpeed(-1f)
            weatherAnimation.playAnimation()
        } else {
            weatherAnimation.setSpeed(1f)
            weatherAnimation.playAnimation()
        }

        animatorSet.start()
        isDayMode = !nightMode
    }

    private fun sendWeatherRequest(city: String = "Wroclaw") {
        loadingAnimation.scale = 3f
        loadingAnimation.loop(true)
        loadingAnimation.playAnimation()

        currentCity.text = city.toUpperCase()


        val apiKey = getString(R.string.weatherApiKey)

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://api.openweathermap.org/data/2.5/weather?APPID=$apiKey&q=$city&units=metric")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                println("XXX - FAIL WITH CALL")

            }

            override fun onResponse(call: Call?, response: Response?) {
                val res = JSONObject(response?.body()?.string())

                weatherStatus = WeatherStatus(res)
                runOnUiThread {
                    loadData.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    inner class LocalServiceConnection : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder?) {
            val b = binder as TimeService.LocalBinder

            fun updateTime(newTime: String) {
                runOnUiThread {
                    currentTime.text = newTime
                }
            }

            timeService = b.getService()
            binder.subscribeToTimeUpdate(::updateTime)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            timeService = null
        }
    }
}

package com.krizzu.weatherapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import com.krizzu.weatherapp.fragments.*
import com.krizzu.weatherapp.services.TimeService


private const val TAG_WEATHER_DISPLAY: String = "Fragment_Weather_Display"
private const val TAG_WEATHER_INFO = "Fragment_Weather_Info"

class MainActivity : AppCompatActivity(), WeatherRequestHandler, TimeServiceHandler {
    // dev button
    private lateinit var loadData: Button

    // Time service
    private val localServiceConnection = LocalServiceConnection()
    private var timeService: TimeService? = null
    private var timeServiceListener: ((String) -> Unit)? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar()
        setContentView(R.layout.activity_main)
        loadData = findViewById(R.id.Button_mainDEV_LoadWeather)

        toggleLoadingAnimation()


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
        stopTimeService()
        super.onDestroy()
    }

    override fun onWeatherRequestRetry() {
        onWeatherRequestError(null)
        toggleLoadingAnimation(true)

        loadData.visibility = View.VISIBLE

        runUI()
    }

    override fun onWeatherRequestError(errorMessage: String?) {
        toggleLoadingAnimation(false)

        if (errorMessage != null) {

            val removingTrans = supportFragmentManager.beginTransaction()
            supportFragmentManager.fragments.forEach {
                removingTrans.remove(it)
            }

            val errorFragment = FragmentError()
            val args = Bundle()
            args.putString("error", errorMessage)
            errorFragment.arguments = args

            removingTrans
                .add(R.id.ConstraintLayout_main_mainContainer, errorFragment, "ERROR")
                .commit()
        } else {
            val fragment = supportFragmentManager.findFragmentByTag("ERROR")
            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commit()
            }

        }
    }

    override fun setupTimeService(cb: ((String) -> Unit)?) {
        timeServiceListener = if (cb == null) null else cb
        val timeServiceIntent = Intent(this, TimeService::class.java)
        bindService(timeServiceIntent, localServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun stopTimeService() {
        if (timeServiceListener != null) {
            timeServiceListener = null
            unbindService(localServiceConnection)
        }
    }

    private fun runUI() {
        setupWeatherDisplay()
        toggleLoadingAnimation(startAnimation = false)
        initCurrentWeather()
    }

    private fun setupWeatherDisplay() {
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.ConstraintLayout_main_mainContainer,
                FragmentWeatherDisplay(),
                TAG_WEATHER_DISPLAY
            )
            .commit()
    }

    private fun toggleLoadingAnimation(startAnimation: Boolean = true) {
        if (startAnimation) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.ConstraintLayout_main_mainContainer,
                    FragmentLoadingAnim(),
                    "LOADING_ANIM"
                )
                .commit()
        } else {
            val fragment = supportFragmentManager.findFragmentByTag("LOADING_ANIM")
            if (fragment != null) {
                supportFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commit()
            }
        }
    }

    // TODO(Fragment: use city, pass it as arg to fragment)
    private fun initCurrentWeather(city: String = "Wroclaw") {
        loadData.visibility = View.GONE
        onWeatherRequestError(null)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.ConstraintLayout_main_mainContainer, FragmentWeatherInfo(), TAG_WEATHER_INFO)
            .commit()
    }

    private fun setupStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    inner class LocalServiceConnection : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder?) {
            val b = binder as TimeService.LocalBinder

            fun updateTime(newTime: String) {
                if (timeServiceListener != null) {
                    timeServiceListener?.invoke(newTime)
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

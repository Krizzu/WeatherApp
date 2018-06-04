package com.krizzu.weatherapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.krizzu.weatherapp.R
import com.krizzu.weatherapp.utils.WeatherStatus
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

// Current weather information

class FragmentWeatherInfo : Fragment() {

    private lateinit var mainContainer: LinearLayout
    private lateinit var weatherCondition: TextView
    private lateinit var weatherValue: TextView
    private lateinit var currentTime: TextView
    private lateinit var currentCity: TextView

    lateinit var weatherResponse: WeatherStatus

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather_info, container!!, false)

        mainContainer = view.findViewById(R.id.LinearLayout_main_weatherInfoContainer)
        weatherCondition = view.findViewById(R.id.TextView_main_weatherStatus)
        weatherValue = view.findViewById(R.id.TextView_main_temperatureValue)
        currentTime = view.findViewById(R.id.TextView_main_currentTime)
        currentCity = view.findViewById(R.id.TextView_main_currentCity)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO(Fetching: Read arguments from fragment transaction to get city name)
        fetchCurrentWeather()
        runTimeService()
    }

    override fun onDestroy() {
        super.onDestroy()
        val act = activity
        if (act != null && act is TimeServiceHandler) {
            act.stopTimeService()
        }
    }

    private fun setWeatherStatus() {
        activity?.runOnUiThread {
            weatherValue.text = "${weatherResponse.temp.temp}°C"
            weatherCondition.text = weatherResponse.weather.description
        }
    }

    private fun setCity() {
        activity?.runOnUiThread {
            currentCity.text = "${weatherResponse.place.city}, ${weatherResponse.place.country}"
        }
    }


    private fun handleError(errorMessage: String?) {
        val act = activity
        // call context's interface on error
        if (act != null && act is WeatherRequestHandler) {
            act.errorHandler(errorMessage)
        }
    }

    private fun updateTime(newTime: String?) {
        activity?.runOnUiThread {
            currentTime.text = newTime
        }
    }

    private fun runTimeService() {
        val act = activity
        if (act != null && act is TimeServiceHandler) {
            act.setupTimeService(::updateTime)
        }
    }

    private fun fetchCurrentWeather(city: String = "wroclaw") {
        val apiKey = getString(R.string.weatherApiKey)

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://api.openweathermap.org/data/2.5/weather?APPID=$apiKey&q=$city&units=metric")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                handleError(e?.message)
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response?.isSuccessful == true) {
                    val res = JSONObject(response.body()?.string())
                    weatherResponse = WeatherStatus(res)
                    setWeatherStatus()
                    setCity()
                } else {
                    handleError(response?.message())
                }

            }
        })
    }


}


interface WeatherRequestHandler {
    fun errorHandler(errorMessage: String?)
}

interface TimeServiceHandler {
    fun setupTimeService(cb: ((String) -> Unit)?)
    fun stopTimeService()
}
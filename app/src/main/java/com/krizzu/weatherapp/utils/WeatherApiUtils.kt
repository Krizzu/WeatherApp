package com.krizzu.weatherapp.utils

import org.json.JSONObject

data class ApiWeatherResponse(private val weatherStatus: JSONObject) {
    val description: String = weatherStatus.getString("description")
    val status: String = weatherStatus.getString("main")
}

data class ApiTempResponse(private val tempStatus: JSONObject) {
    val temp: Int = tempStatus.getInt("temp")
}

data class WeatherStatus(private val response: JSONObject) {
    val weather = ApiWeatherResponse(response.getJSONArray("weather")[0] as JSONObject)
    val temp = ApiTempResponse(response.getJSONObject("main"))
}

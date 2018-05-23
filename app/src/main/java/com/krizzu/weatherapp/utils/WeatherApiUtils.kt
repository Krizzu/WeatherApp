package com.krizzu.weatherapp.utils

import org.json.JSONObject

data class ApiWeatherResponse(private val weatherStatus: JSONObject) {
    val description: String = weatherStatus.getString("description")
    val status: String = weatherStatus.getString("main")
}

data class ApiTempResponse(private val tempStatus: JSONObject) {
    val temp: Int = tempStatus.getInt("temp")
    val tempMin: Int = tempStatus.getInt("temp_min")
    val tempMax: Int = tempStatus.getInt("temp_max")
}

data class ApiPlaceResponse(private val placeInfo: JSONObject, val city: String) {
    val country: String = placeInfo.getString("country")
}

data class WeatherStatus(private val response: JSONObject) {
    val weather = ApiWeatherResponse(response.getJSONArray("weather")[0] as JSONObject)
    val temp = ApiTempResponse(response.getJSONObject("main"))
    val place = ApiPlaceResponse(response.getJSONObject("sys"), response.getString("name"))
}

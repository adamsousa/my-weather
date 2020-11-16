package com.github.adamsousa.myweather.model

data class CurrentWeatherResponse(
    var coord: Coord,
    var weather: ArrayList<Weather>,
    var main: Main,
    var visibility: Int,
    var wind: Wind,
    var clouds: Clouds,
    var dt: Float,
    var sys: Sys,
    var timezone: Int,
    var id: Int,
    var name: String,
    var cod: Float
)
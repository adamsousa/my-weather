package com.github.adamsousa.myweather.model

import kotlin.collections.List

data class List(
    val dt : Int,
    val main : Main,
    val weather : List<Weather>,
    val clouds : Clouds,
    val wind : Wind,
    val sys : Sys,
    val dt_txt : String
) {
    data class Sys (
        val pod : String
    )
}
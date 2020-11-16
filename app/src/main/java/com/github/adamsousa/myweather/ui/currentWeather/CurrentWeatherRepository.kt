package com.github.adamsousa.myweather.ui.currentWeather

import com.github.adamsousa.myweather.service.CurrentWeatherService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentWeatherRepository @Inject constructor(private val currentWeatherService: CurrentWeatherService) {

    suspend fun getCurrentWeatherByCity(query: String)
            = currentWeatherService.getCurrentWeatherByCity(query)

    suspend fun getCurrentWeatherByZip(query: String)
            = currentWeatherService.getCurrentWeatherByZip(query)

    suspend fun getCurrentWeatherByLocation(lat: Double, lon: Double)
            = currentWeatherService.getCurrentWeatherByLocation(lat, lon)
}
package com.github.adamsousa.myweather.service

import com.github.adamsousa.myweather.BuildConfig
import com.github.adamsousa.myweather.model.CurrentWeatherResponse
import retrofit2.http.*

interface CurrentWeatherService {

    @Headers("x-api-key: " + BuildConfig.OPEN_WEATHER_ACCESS_KEY)
    @GET("data/2.5/weather?")
    suspend fun getCurrentWeatherByCity(
        @Query("q") query: String,
    ): CurrentWeatherResponse

    @Headers("x-api-key: " + BuildConfig.OPEN_WEATHER_ACCESS_KEY)
    @GET("data/2.5/weather?")
    suspend fun getCurrentWeatherByZip(
        @Query("zip") zip: String,
    ): CurrentWeatherResponse

    @Headers("x-api-key: " + BuildConfig.OPEN_WEATHER_ACCESS_KEY)
    @GET("data/2.5/weather?")
    suspend fun getCurrentWeatherByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): CurrentWeatherResponse
}
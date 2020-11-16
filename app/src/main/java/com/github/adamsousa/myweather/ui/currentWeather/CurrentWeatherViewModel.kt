package com.github.adamsousa.myweather.ui.currentWeather

import android.location.Location
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.github.adamsousa.myweather.custom.ErrorWrapper
import com.github.adamsousa.myweather.custom.Event
import com.github.adamsousa.myweather.custom.LoadState
import com.github.adamsousa.myweather.model.CurrentWeatherResponse
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest

class CurrentWeatherViewModel @ViewModelInject constructor(
    private val repository: CurrentWeatherRepository
) : ViewModel() {

    enum class ErrorType {
        FAILED_TO_RETRIEVE_CITY,
        FAILED_TO_RETRIEVE_ZIP,
        FAILED_TO_RETRIEVE_LOCATION
    }

    val query: MutableLiveData<String> = MutableLiveData()
    val state: MutableLiveData<LoadState> = MutableLiveData()
    val error: MutableLiveData<Event<ErrorWrapper<ErrorType>>> = MutableLiveData()

    // Device location vars
    var currentLocation: Location? = null
    var locationRequest: LocationRequest = LocationRequest()
    var locationCallback: LocationCallback? = null
    var requestingLocationUpdates = false

    // Retrieve weather when query values changes
    val currentWeather: LiveData<CurrentWeatherResponse> = query.switchMap { queryString ->
        // Parse string values
        val strings = queryString.split(delimiters = arrayOf(","), limit = 2)
        val firstVal = strings.elementAtOrNull(0)?.toDoubleOrNull()
        val secondVal = strings.elementAtOrNull(1)?.toDoubleOrNull()

        //Determine if value(s) entered are location coordinates, zip code, or city name

        state.value = LoadState.Loading
        liveData {
            if (firstVal != null && secondVal != null) {
                try {
                    emit(repository.getCurrentWeatherByLocation(firstVal, secondVal))
                    state.postValue(LoadState.NotLoading)
                } catch (exception: Exception) {
                    postError(exception, ErrorType.FAILED_TO_RETRIEVE_LOCATION)
                }
            } else if (firstVal != null) {
                try {
                    emit(repository.getCurrentWeatherByZip(queryString))
                    state.postValue(LoadState.NotLoading)
                } catch (exception: Exception) {
                    postError(exception, ErrorType.FAILED_TO_RETRIEVE_ZIP)
                }
            } else {
                try {
                    emit(repository.getCurrentWeatherByCity(queryString))
                    state.postValue(LoadState.NotLoading)
                } catch (exception: Exception) {
                    postError(exception, ErrorType.FAILED_TO_RETRIEVE_CITY)
                }
            }
        }
    }

    private fun postError(exception: Exception, errorType: ErrorType) {
        error.postValue(
            Event(
                ErrorWrapper(
                    exception,
                    errorType
                )
            )
        )
        state.postValue(LoadState.Error)
    }

    init {
        // Default location request params
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
    }

    fun refreshSearch(): Boolean {
        val validQuery = query.value
        if (validQuery != null) {
            query.value = query.value
            return true
        }
        return false
    }
}
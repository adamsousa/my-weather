package com.github.adamsousa.myweather.ui.currentWeather

import android.Manifest
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.github.adamsousa.myweather.R
import com.github.adamsousa.myweather.custom.ErrorWrapper
import com.github.adamsousa.myweather.custom.LoadState
import com.github.adamsousa.myweather.custom.OpenWeatherError
import com.github.adamsousa.myweather.databinding.FragmentCurrentWeatherBinding
import com.github.adamsousa.myweather.helper.ViewHelper.Companion.hideKeyboard
import com.github.adamsousa.myweather.model.CurrentWeatherResponse
import com.github.adamsousa.myweather.ui.base.BaseFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.wanderingcan.persistentsearch.PersistentSearchView.OnIconClickListener
import com.wanderingcan.persistentsearch.PersistentSearchView.OnSearchListener
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import java.util.*
import kotlin.math.roundToInt


@AndroidEntryPoint
class CurrentWeatherFragment : BaseFragment() {

    private lateinit var binding: FragmentCurrentWeatherBinding

    private lateinit var rootView: View
    private val viewModel by viewModels<CurrentWeatherViewModel>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // Dialog frag id
    private val dialogEnableLocationId = 1

    // Permission request code
    private val permissionRequestReadLocation = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrentWeatherBinding.inflate(layoutInflater)
        rootView = binding.root

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        // Set callback when current location is retrieved
        viewModel.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    attemptQueryCurrentLocation(location)
                    viewModel.currentLocation = location
                    viewModel.requestingLocationUpdates = false
                    stopLocationUpdates()
                }
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Populate search bar text
        binding.searchBar.populateSearchText(viewModel.query.value)

        // Observe live data
        viewModel.currentWeather.observe(viewLifecycleOwner, { currentWeather ->
            showCurrentWeather(currentWeather)
        })
        viewModel.state.observe(viewLifecycleOwner, { state ->
            showLoadState(state)
        })
        viewModel.error.observe(viewLifecycleOwner, { errorEvent ->
            val error = errorEvent.contentIfNotHandled
            if (error != null) {
                showError(error)
            }
        })

        initViewElements()
    }

    private fun initViewElements() {
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.bright_turquoise
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            if (!viewModel.refreshSearch()) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.errorView.emptyViewActionButton.setOnClickListener {
            viewModel.refreshSearch()
        }

        binding.searchBar.setOnSearchListener(object : OnSearchListener {
            override fun onSearchOpened() {
            }

            override fun onSearchClosed() {
            }

            override fun onSearchCleared() {
                onSearchTermChanged("")
            }

            override fun onSearchTermChanged(term: CharSequence?) {
                //Reset error if shown
                if (viewModel.state.value == LoadState.Error) {
                    viewModel.state.value = LoadState.NotLoading
                    binding.weatherVisible = false
                }
            }

            override fun onSearch(text: CharSequence?) {
                if (!text.isNullOrEmpty()) {
                    hideKeyboard()
                    viewModel.query.value = text.toString()
                }
            }
        })

        binding.searchBar.setOnIconClickListener(object : OnIconClickListener {
            override fun OnNavigationIconClick() {
            }

            override fun OnEndIconClick() {
                hideKeyboard()
                attemptQueryCurrentLocation(viewModel.currentLocation)
            }
        })
    }

    private fun showCurrentWeather(currentWeather: CurrentWeatherResponse) {
        val weather = currentWeather.weather[0]
        val main = currentWeather.main
        val wind = currentWeather.wind
        val clouds = currentWeather.clouds

        val degreeFah = getString(R.string.label_degree_f)

        // Set main content
        binding.condition.text = weather.main
        val temperatureString = main.temp.roundToInt().toString() + degreeFah
        binding.temperature.text = temperatureString
        binding.weatherDescription.text = weather.description?.capitalize(Locale.getDefault())
        Glide.with(rootView)
            .load(getString(R.string.open_weather_icon_url, weather.icon))
            .centerCrop()
            .error(R.drawable.ic_baseline_error)
            .into(binding.weatherIcon)

        // Format weather detail strings
        val pressureText = main.pressure.roundToInt().toString() + getString(R.string.label_hpa)
        val humidityText = main.humidity.toString() + "%"
        val tempMin = main.temp_min.roundToInt().toString() + degreeFah
        val tempMax = main.temp_max.roundToInt().toString() + degreeFah
        val windSpeed = wind.speed.toString() + getString(R.string.label_mph)

        // Determine cloud label
        val cloudStringId: Int
        val cloudsRaw = clouds.all
        cloudStringId = when {
            cloudsRaw >= 85 -> {
                R.string.label_overcast_clouds
            }
            cloudsRaw in 51..84 -> {
                R.string.label_broken_clouds
            }
            cloudsRaw in 25..50 -> {
                R.string.label_scattered_clouds
            }
            cloudsRaw in 11..24 -> {
                R.string.label_few_clouds
            }
            else -> {
                R.string.label_clear_sky
            }
        }

        // Set weather details
        binding.pressure.text = pressureText
        binding.humidity.text = humidityText
        binding.tempMin.text = tempMin
        binding.tempMax.text = tempMax
        binding.windSpeed.text = windSpeed
        binding.clouds.text = getString(cloudStringId)

        // Set weather view as visible (empty view will then be hidden)
        binding.weatherVisible = true
    }

    private fun showLoadState(state: LoadState) {
        // Update loading progress and error view based on current load state
        if (binding.swipeRefresh.isRefreshing) {
            binding.swipeRefresh.isRefreshing = (state == LoadState.Loading)
            binding.loadingViewVisible = false
        } else {
            binding.loadingViewVisible = (state == LoadState.Loading)
        }

        binding.errorViewVisible = (state == LoadState.Error)
    }

    private fun showError(error: ErrorWrapper<CurrentWeatherViewModel.ErrorType>) {
        val messageId: Int =
            if (error.exception is HttpException && error.exception.code() == OpenWeatherError.notFound) {
                when (error.errorType) {
                    CurrentWeatherViewModel.ErrorType.FAILED_TO_RETRIEVE_CITY ->
                        R.string.error_retrieve_weather_city
                    CurrentWeatherViewModel.ErrorType.FAILED_TO_RETRIEVE_ZIP ->
                        R.string.error_retrieve_weather_zip
                    CurrentWeatherViewModel.ErrorType.FAILED_TO_RETRIEVE_LOCATION ->
                        R.string.error_retrieve_weather_location
                }
            } else {
                R.string.error_retrieve_weather
            }

        showAnchoredSnackBar(
            Snackbar.make(
                rootView,
                messageId,
                Snackbar.LENGTH_LONG
            )
        )
    }

    private fun attemptQueryCurrentLocation(location: Location?) {
        // Check if location is valid, if not attempt to request location from system and user
        if (location != null) {
            val latLonString = location.latitude.toString() +
                    "," + location.longitude.toString()
            binding.searchBar.populateSearchText(latLonString)
            viewModel.query.value = latLonString
        } else {
            val currentContext = context

            if (currentContext != null && ContextCompat.checkSelfPermission(
                    currentContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startLocationUpdates(currentContext)
                checkLocationAccessStatus(currentContext)
            } else {
                requestLocationUpdatesPermission()
                showAnchoredSnackBar(
                    Snackbar.make(
                        rootView,
                        R.string.error_enable_location_permission,
                        Snackbar.LENGTH_LONG
                    )
                )
            }
        }
    }

    private fun requestLocationUpdatesPermission() {
        // Request permission to access location
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            permissionRequestReadLocation
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        // Check if user allowed to access location
        if (requestCode == permissionRequestReadLocation) {
            viewModel.requestingLocationUpdates =
                (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            if (viewModel.requestingLocationUpdates) {
                val context = context
                if (context != null) {
                    checkLocationAccessStatus(context)
                    startLocationUpdates(context)
                }
            } else {
                stopLocationUpdates()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkLocationAccessStatus(context: Context) {
        // Determine if locations enable dialog should be shown
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(viewModel.locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { e: Exception? ->
            val resolvableApi = e as ResolvableApiException?
            if (resolvableApi != null) {
                showEnableLocationDialog(resolvableApi)
            }
        }
    }

    private fun showEnableLocationDialog(resolvable: ResolvableApiException) {
        val activity = activity
        activity?.let {
            try {
                // If user has disabled locations, display a message prompting them to enable it
                resolvable.startResolutionForResult(activity, dialogEnableLocationId)
            } catch (sendEx: SendIntentException) {
                // Ignore errors.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.requestingLocationUpdates) {
            startLocationUpdates(requireContext())
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.requestingLocationUpdates) {
            stopLocationUpdates()
        }
    }

    private fun startLocationUpdates(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If user has given permission, get location updates
            fusedLocationProviderClient.requestLocationUpdates(
                viewModel.locationRequest,
                viewModel.locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // If permission is not granted, request permission
            requestLocationUpdatesPermission()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(viewModel.locationCallback)
    }
}
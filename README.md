# My-Weather

MyWeather Android Mobile Application.

### Prerequisites

Download Android Studio

### Running the App
- To Run/Install the App In Different Variants in Andorid
    1. Click on the Build Variants tab on the left side of the IDE
    2. Click the Option below the title Active Build Variant
    3. Wait for the project to sync
    4. Then Run the App
    
## Instructions

### Get Current Weather
- To Retrieve the Current Weather
    1. Launch app
    2. Click the Search Bar at the very top
    3. Enter city name or zip code (you may also include state and/or country by seperating each with a ",")
    4. Click the "Search" button in the lower right corner of the Keypad
    5. The weather results will load  (App will notify you if the city or zip code entered is invalid)
    
### Example Inputs
City Name Query:
- London
- London, GB
- Saint Louis
- Saint Louis, MO, US

Zip Code Query:
- 63101
- 63101, US
- 49534
- 49534, US
    
### Get Current Weather by Location
- To Retrieve the Current Weather by Device Location
    1. Launch app
    2. Click the Current Location icon in the Search bar in the upper right hand corner
    3. Device may ask permisson to your location as Location Services if it's disabled
    4. The weather results will load according to your location (Latitude and Longitude coordinates will be displayed in the search bar)
    
### BONUS
Try enabling Dark Mode :sunglasses:
    
## Other App Info
    
### App Architecture

This project files the MVVM (Model-View-ViewModel) Architecture. Guidelines can be found here: https://developer.android.com/jetpack/guide

### Versioning

Google Play requires that the **versionCode** be incremented on every app store submission. In addition to choosing a versionName, the versionCode must be incremented by one before building an APK.

### Enviroments

There are currently 3 environments set up in the project:
* debug (development)
* stage (test)
* release

### Authors

* Adam Sousa

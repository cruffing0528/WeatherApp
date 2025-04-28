package com.zybooks.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var tempTextview: TextView
    private lateinit var cityTextview: TextView
    private lateinit var dateTextview: TextView
    private lateinit var weatherTextview: TextView
    private lateinit var weatherImageview: ImageView

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var googleMap: GoogleMap
    private var client: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private val API_KEY = "AIzaSyBhFReBS3QWewxVDf2-7gt7jkeP8GZ1enw";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tempTextview = findViewById(R.id.temperatureTextview)
        cityTextview = findViewById(R.id.cityTextview)
        dateTextview = findViewById(R.id.dateTextview)
        weatherTextview = findViewById(R.id.weatherTextview)
        weatherImageview = findViewById(R.id.weatherImageview)

        getWeather("Folsom")

        // Registering a context menu when the user clicks on the city name
        registerForContextMenu(cityTextview)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (hasLocationPermission()) {
            trackLocation()
        }
    }

    // This function retrieves weather data from a weather API, parses the JSON response, and updates
    // UI elements (TextViews and an ImageView) to display the temperature, city, date, weather description,
    // and a weather icon.
    // It also adds a bounce animation to the weather image when the image is clicked, as well as plays
    // a sound that matches the weather condition.
    // It also includes error handling for both JSON parsing issues and network/server request failures.
    private fun getWeather(city: String) {

        val queue = Volley.newRequestQueue(this)

        val url = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=97d7fec7fb5625168efda6ff1f0a2b9b&units=Imperial"

        val requestObject = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val mainJSONObject: JSONObject = response.getJSONObject("main")
                    val temperature: String = Math.round(mainJSONObject.getDouble("temp")).toString()
                    tempTextview.setText(temperature)

                    val city: String = response.getString("name")
                    cityTextview.setText(city)

                    dateTextview.setText(getDate())

                    val weatherJSONArray = response.getJSONArray("weather")
                    val weatherJSONObject: JSONObject = weatherJSONArray.getJSONObject(0)
                    val weatherDescription: String = weatherJSONObject.getString("description")
                    weatherTextview.setText(weatherDescription)

                    val iconResourceId = getResources().getIdentifier(
                        "icon_" + weatherDescription.replace(" ", ""),
                        "drawable",
                        packageName
                    )
                    weatherImageview.setImageResource(iconResourceId)
                    // Bounce view animation is triggered when the weather image is clicked
                    weatherImageview.setOnClickListener {
                        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
                        weatherImageview.startAnimation(animation)
//                        Log.v("Icon resource id is ", "Icon resource id is " + iconResourceId.toString())
                        playSound(iconResourceId)
                    }



                } catch (e: JSONException) {
                    e.printStackTrace()
                    tempTextview.setText("Try block didnt work!")
                }
            },
            { error ->
                error.printStackTrace()
                tempTextview.setText("That didnt work!")
            }
        )

        queue.add(requestObject)
    }

    private fun playSound(iconResourceId: Int) {
        // Release any previous MediaPlayer instance
        mediaPlayer?.release()
        mediaPlayer = null

        // Create a MediaPlayer instance for each image
        val rainSound = MediaPlayer.create(this, R.raw.rain_sound)
        val sunnySound = MediaPlayer.create(this, R.raw.sunny_sound)
        val windSound = MediaPlayer.create(this, R.raw.wind_sound)

        // Assign the media player to the correct sound
        if(hasRainSound(resources, iconResourceId)) {
            mediaPlayer = rainSound
        } else if (hasWindSound(resources, iconResourceId)) {
            mediaPlayer = windSound
        } else {
            mediaPlayer = sunnySound
        }

        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener {
            it.release()
            mediaPlayer = null
        }
    }

    private fun hasRainSound(resources: Resources, weatherImageResourceID: Int): Boolean {
        val rainSoundArray = resources.obtainTypedArray(R.array.raining_sound)
        var hasSound = false

        for (i in 0 until rainSoundArray.length()) {
            val rainSoundResourceId = rainSoundArray.getResourceId(i, 0)
            if (rainSoundResourceId == weatherImageResourceID) {
                hasSound = true
                break
            }
        }

        rainSoundArray.recycle()
        return hasSound
    }

    private fun hasWindSound(resources: Resources, weatherImageResourceID: Int): Boolean {
        val windSoundArray = resources.obtainTypedArray(R.array.windy_sound)
        var hasSound = false

        for (i in 0 until windSoundArray.length()) {
            val windSoundResourceId = windSoundArray.getResourceId(i, 0)
            if (windSoundResourceId == weatherImageResourceID) {
                hasSound = true
                break
            }
        }

        windSoundArray.recycle()
        return hasSound
    }

    private fun getDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd")
        val formattedDate = dateFormat.format(calendar.time)
        return formattedDate
    }

    // This is creating the context menu
    override fun onCreateContextMenu(menu: ContextMenu?,
                                     v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    // the context menu displays when the user long-clicks on the city name
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.change_locations -> {
                // Call showCityListFragment() function to display a list of locations to choose from
                showCityListFragment()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // This function displays the CItyFragment in the UI by starting a fragment transaction
    // and uses replace to put the CityFragment in the fragmentContainerView container, and then
    // commits the transaction applying the changes
    private fun showCityListFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, CityFragment())
            commit()
        }
    }

    private fun trackLocation() {

        // Create location request
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .build()

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    // update the api url with the location
                    reverseGeocode(location.latitude, location.longitude)
                }
            }
        }

        client = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Hide the map fragment's view
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null && mapFragment.view != null) {
            mapFragment.view?.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        client?.removeLocationUpdates(locationCallback!!)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            client?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, Looper.getMainLooper())
        }
    }

    private fun hasLocationPermission(): Boolean {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            trackLocation()
        }
    }

    // Takes the latitude and longitude from location on Google Maps creates a request that gets the city name in one of the
    // response fields so that it can be used to call getWeather() to get the weather information for that city displayed in the UI
    private fun reverseGeocode(latitude: Double, longitude: Double) {
        val apiKey = API_KEY;
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey"
        var city = ""

        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val results = response.getJSONArray("results")
                    if (results.length() > 0) {
                        val firstResult = results.getJSONObject(0)
                        val addressComponents = firstResult.getJSONArray("address_components")

                        for (i in 0 until addressComponents.length()) {
                            val component = addressComponents.getJSONObject(i)
                            val types = component.getJSONArray("types")
                            for (j in 0 until types.length()) {
                                if (types.getString(j) == "locality") {
                                    city = component.getString("long_name")
                                    break
                                }
                            }
                            if (city.isNotEmpty()) {
                                break
                            }
                        }

                        if (city.isNotEmpty()) {
                            Log.d("ReverseGeocode", "City: $city")
                            // Update the UI with the city name here after 5 seconds has passed
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                cityTextview.setText(city)
                                getWeather(city)
                            }, 5000) // 5000 milliseconds = 5 seconds
                        } else {
                            Log.w("ReverseGeocode", "City not found in response")
                        }
                    } else {
                        Log.w("ReverseGeocode", "No results found")
                    }
                } catch (e: JSONException) {
                    Log.e("ReverseGeocode", "Error parsing JSON", e)
                }
            },
            { error ->
                Log.e("ReverseGeocode", "Error: ${error.message}")
            }
        )

        queue.add(request)
    }

}


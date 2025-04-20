package com.zybooks.weatherapp

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private lateinit var tempTextview: TextView
    private lateinit var cityTextview: TextView
    private lateinit var dateTextview: TextView
    private lateinit var weatherTextview: TextView
    private lateinit var weatherImageview: ImageView

    private val url = "http://api.openweathermap.org/data/2.5/weather?q=San Diego&appid=97d7fec7fb5625168efda6ff1f0a2b9b&units=Imperial"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tempTextview = findViewById(R.id.temperatureTextview)
        cityTextview = findViewById(R.id.cityTextview)
        dateTextview = findViewById(R.id.dateTextview)
        weatherTextview = findViewById(R.id.weatherTextview)
        weatherImageview = findViewById(R.id.weatherImageview)

        getWeather()

        // Registering a context menu when the user clicks on the city name
        registerForContextMenu(cityTextview)
    }

    // This function retrieves weather data from a weather API, parses the JSON response, and updates
    // UI elements (TextViews and an ImageView) to display the temperature, city, date, weather description,
    // and a weather icon. It also includes error handling for both JSON parsing issues and network/server request failures.
    private fun getWeather() {

        val queue = Volley.newRequestQueue(this)

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
                    // Bounce animation is triggered when the weather image is clicked
                    weatherImageview.setOnClickListener {
                        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
                        weatherImageview.startAnimation(animation)
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


}
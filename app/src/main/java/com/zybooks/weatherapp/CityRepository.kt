package com.zybooks.weatherapp

import android.content.Context

class CityRepository private constructor(context: Context) {

    var cityList: MutableList<City> = mutableListOf()

    companion object {
        private var instance: CityRepository? = null

        fun getInstance(context: Context): CityRepository {
            if (instance == null) {
                instance = CityRepository(context)
            }
            return instance!!
        }
    }

    // where we initialize the cityList with city data from the string resource array
    // The init block reads a list of city names from string resources and populates cityList
    // with corresponding City objects.
    init {
        val cities = context.resources.getStringArray(R.array.cities_list)
        for (i in cities.indices) {
            cityList.add(City(cities[i]))
        }
    }
}

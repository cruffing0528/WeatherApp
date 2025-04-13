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

    init {
        val cities = context.resources.getStringArray(R.array.Cities)
        for (i in cities.indices) {
            cityList.add(City(cities[i]))
        }
    }
}
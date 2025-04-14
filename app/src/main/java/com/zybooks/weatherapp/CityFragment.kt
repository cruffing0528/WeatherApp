package com.zybooks.weatherapp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A fragment representing a list of Cities. It inflates the fragment_item_list layout,
 * retrieves city data from the City Repository, and sets up a RecyclerView to display the list of cities.
 */
class CityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
//        val cities = resources.getStringArray(R.array.Cities)

        // Click listener for the RecyclerView
        val onClickListener = View.OnClickListener { itemView: View ->
            println("HELLO THERE CLAIRE")
//            cityTextview.setText(city)

        }

        // Set the recyclerView android.R.id.list is for ListFragment's MyCityRecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.city_list)
        val cities = CityRepository.getInstance(requireContext()).cityList

        // Set the adapter
        recyclerView.adapter = MyCityRecyclerViewAdapter(cities, onClickListener)

        return view
    }


}
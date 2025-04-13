package com.zybooks.weatherapp

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class MyCityRecyclerViewAdapter(private val cities: MutableList<City>, private val onClickListener: View.OnClickListener) :
    RecyclerView.Adapter<MyCityRecyclerViewAdapter.CityViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */

    class CityViewHolder(inflater: LayoutInflater, parent: ViewGroup?) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_item, parent, false)) {

        private val cityNameTextView: TextView

        init {
            cityNameTextView = itemView.findViewById(R.id.city_name)
        }

        fun bind(city: City) {
            cityNameTextView.text = city.cityName
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parentView: ViewGroup, viewType: Int): CityViewHolder {
        // defines the UI of the list item
        val layoutInflater = LayoutInflater.from(parentView.context)

        return CityViewHolder(layoutInflater, parentView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: CityViewHolder, position: Int) {

        // Get city from cities array at this position and replace the
        // contents of the view with that element
        val city = cities[position]
        viewHolder.bind(city)
        viewHolder.itemView.setOnClickListener(onClickListener)

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return cities.size
    }

}
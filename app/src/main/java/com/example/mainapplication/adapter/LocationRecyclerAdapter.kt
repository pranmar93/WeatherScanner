package com.example.mainapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.example.mainapplication.R
import com.example.mainapplication.activities.sharedRepository
import com.example.mainapplication.models.Weather

class LocationsRecyclerAdapter(
    private val context: Context,
    private val weatherArrayList: ArrayList<Weather>,
    private val favouriteCities: ArrayList<Weather>
) : RecyclerView.Adapter<LocationsRecyclerAdapter.LocationsViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationsViewHolder {
        return LocationsViewHolder(
            inflater.inflate(
                R.layout.list_location_row,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onBindViewHolder(
        holder: LocationsViewHolder,
        position: Int
    ) {
        val weatherFont =
            Typeface.createFromAsset(context.assets, "fonts/weather.ttf")
        val weather: Weather = weatherArrayList[position]
        holder.cityTextView.text = String.format(
            "%s, %s",
            weather.city,
            weather.country
        )
        holder.temperatureTextView.text = weather.temperature
        holder.descriptionTextView.text = weather.description
        holder.iconTextView.text = weather.icon
        holder.iconTextView.typeface = weatherFont

        holder.favouriteButton.isChecked = weather.cityId in favouriteCities.map { it.cityId }

        holder.favouriteButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                favouriteCities.add(weather)
            } else {
                val city = favouriteCities.find { it.cityId == weather.cityId }

                if (city != null) {
                    favouriteCities.remove(city)
                }
            }
            sharedRepository.setFavourites(favouriteCities)
        }
    }

    override fun getItemCount(): Int {
        return weatherArrayList.size
    }

    inner class LocationsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val cityTextView: TextView = itemView.findViewById(R.id.rowCityTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.rowTemperatureTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.rowDescriptionTextView)
        val iconTextView: TextView = itemView.findViewById(R.id.rowIconTextView)
        val favouriteButton: ToggleButton = itemView.findViewById(R.id.button_favourite)

        override fun onClick(view: View) {
            if (itemClickListener != null) {
                itemClickListener!!.onItemClickListener(view, adapterPosition)
            }
        }

        init {
            itemView.setOnClickListener(this)
            favouriteButton.setBackgroundResource(R.drawable.drawable_favourite_state)
        }
    }

    fun getItem(position: Int): Weather {
        return weatherArrayList[position]
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClickListener(view: View?, position: Int)
    }

}
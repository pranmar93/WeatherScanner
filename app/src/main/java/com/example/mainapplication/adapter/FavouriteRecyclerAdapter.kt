package com.example.mainapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.example.mainapplication.R
import com.example.mainapplication.activities.sharedRepository
import com.example.mainapplication.fragment.FavouriteDialogFragment
import com.example.mainapplication.models.Weather

class FavouriteRecyclerAdapter(
    private val fragment: FavouriteDialogFragment,
    private val favouriteCities: ArrayList<Weather>
) : RecyclerView.Adapter<FavouriteRecyclerAdapter.LocationsViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(fragment.context)
    private var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationsViewHolder {
        return LocationsViewHolder(
            inflater.inflate(
                R.layout.list_favourite_row,
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
        val weather: Weather = favouriteCities[position]
        holder.cityTextView.text = String.format(
            "%s, %s",
            weather.city,
            weather.country
        )

        holder.favouriteButton.isChecked = true

        holder.favouriteButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                favouriteCities.add(weather)
            } else {
                favouriteCities.remove(weather)
                updateView(position)
            }
            sharedRepository.setFavourites(favouriteCities)
        }
    }

    private fun updateView(pos: Int) {
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, favouriteCities.size - pos)
        if (favouriteCities.isEmpty())
            fragment.updateView()
    }

    override fun getItemCount(): Int {
        return favouriteCities.size
    }

    inner class LocationsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val cityTextView: TextView = itemView.findViewById(R.id.rowCityTextView)
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
        return favouriteCities[position]
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClickListener(view: View?, position: Int)
    }

}
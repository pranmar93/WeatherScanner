package com.example.mainapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainapplication.R
import com.example.mainapplication.activities.MainActivity
import com.example.mainapplication.activities.sharedRepository
import com.example.mainapplication.models.Weather
import com.example.mainapplication.utils.UnitConverter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class WeatherRecyclerAdapter(private var itemList: List<Weather>?): RecyclerView.Adapter<WeatherRecyclerAdapter.WeatherViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): WeatherViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_row, viewGroup, false)
        return WeatherViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(customViewHolder: WeatherViewHolder, i: Int) {
        if (i < 0 || i >= itemList!!.size) return
        val context: Context = customViewHolder.itemView.context
        val weatherItem: Weather = itemList!![i]

        // Temperature
        val temperature: Float =
            UnitConverter.convertTemperature(weatherItem.temperature!!.toFloat())

        // Rain
        val rain: Double = weatherItem.rain!!.toDouble()
        val rainString: String = UnitConverter.getRainString(rain)

        // Wind
        var wind: Double
        wind = try {
            weatherItem.wind!!.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
        wind = UnitConverter.convertWind(wind)

        // Pressure
        val pressure: Double = UnitConverter.convertPressure(weatherItem.pressure!!.toDouble())
        val tz = TimeZone.getDefault()
        val defaultDateFormat = "E dd.MM.yyyy - HH:mm"
        var dateString: String
        try {
            val resultFormat = SimpleDateFormat(defaultDateFormat, Locale.ENGLISH)
            resultFormat.timeZone = tz
            dateString = resultFormat.format(weatherItem.date!!)
        } catch (e: IllegalArgumentException) {
            dateString = context.resources.getString(R.string.error_dateFormat)
        }
        customViewHolder.itemDate.text = dateString
        customViewHolder.itemTemperature.text = DecimalFormat("#.#")
            .format(temperature.toDouble()) + " " + sharedRepository.getTempUnit()
        customViewHolder.itemDescription.text = weatherItem.description!!.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                weatherItem.description!!.substring(1) + rainString
        val weatherFont =
            Typeface.createFromAsset(context.assets, "fonts/weather.ttf")
        customViewHolder.itemIcon.typeface = weatherFont
        customViewHolder.itemIcon.text = weatherItem.icon
        customViewHolder.itemyWind.text = (context.getString(R.string.wind) + ": " + DecimalFormat("0.0").format(wind)) + " " +
                sharedRepository.getSpeedUnit() + " " + MainActivity.getWindDirectionString(context, weatherItem)
        customViewHolder.itemPressure.text = context.getString(R.string.pressure) + ": " + DecimalFormat("0.0").format(pressure)
        customViewHolder.itemHumidity.text = context.getString(R.string.humidity) + ": " + weatherItem.humidity + " %"
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: 0
    }

    inner class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemDate: TextView = view.findViewById(R.id.itemDate)
        var itemTemperature: TextView = view.findViewById(R.id.itemTemperature)
        var itemDescription: TextView = view.findViewById(R.id.itemDescription)
        var itemyWind: TextView = view.findViewById(R.id.itemWind)
        var itemPressure: TextView = view.findViewById(R.id.itemPressure)
        var itemHumidity: TextView = view.findViewById(R.id.itemHumidity)
        var itemIcon: TextView = view.findViewById(R.id.itemIcon)

    }
}
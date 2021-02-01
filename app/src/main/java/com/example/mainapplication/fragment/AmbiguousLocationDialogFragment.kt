package com.example.mainapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainapplication.R
import com.example.mainapplication.activities.MainActivity
import com.example.mainapplication.activities.sharedRepository
import com.example.mainapplication.adapter.LocationsRecyclerAdapter
import com.example.mainapplication.models.Weather
import com.example.mainapplication.utils.TimeUtils.isDayTime
import com.example.mainapplication.utils.UnitConverter
import com.example.mainapplication.utils.WeatherFormatter
import org.json.JSONArray
import org.json.JSONException
import java.text.DecimalFormat
import java.util.*

class AmbiguousLocationDialogFragment : DialogFragment(), LocationsRecyclerAdapter.ItemClickListener {

    private var recyclerAdapter: LocationsRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_ambiguous_location, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        val toolbar: Toolbar = view.findViewById(R.id.dialogToolbar)
        val recyclerView: RecyclerView = view.findViewById(R.id.locationsRecyclerView)
        toolbar.title = getString(R.string.location_search_heading)
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { close() }

        try {
            val cityListArray = JSONArray(bundle!!.getString("cityList"))
            val weatherArrayList: ArrayList<Weather> = ArrayList()
            val favouriteCities = sharedRepository.getFavourites()
            recyclerAdapter = LocationsRecyclerAdapter(
                view.context.applicationContext,
                weatherArrayList, favouriteCities
            )
            recyclerAdapter!!.setClickListener(this@AmbiguousLocationDialogFragment)
            for (i in 0 until cityListArray.length()) {
                val cityObject = cityListArray.getJSONObject(i)
                val weatherObject = cityObject.getJSONArray("weather").getJSONObject(0)
                val mainObject = cityObject.getJSONObject("main")
                val coordObject = cityObject.getJSONObject("coord")
                val sysObject = cityObject.getJSONObject("sys")
                val calendar = Calendar.getInstance()
                val dateMsString = cityObject.getString("dt") + "000"
                val city = cityObject.getString("name")
                val country = sysObject.getString("country")
                val cityId = cityObject.getString("id")
                val description = weatherObject.getString("description")
                val weatherId = weatherObject.getString("id")
                val temperature: Float = UnitConverter.convertTemperature(
                    mainObject.getString("temp").toFloat()
                )
                val lat = coordObject.getDouble("lat")
                val lon = coordObject.getDouble("lon")
                calendar.timeInMillis = dateMsString.toLong()
                val weather = Weather("", "", "", Date(), "", "", "", 0.0, "", "", "", "", "", Date(), Date(), 0.0, 0.0)
                weather.city = city
                weather.country = country
                weather.cityId = cityId
                weather.id = weatherId
                weather.description = description.substring(0, 1).toUpperCase(Locale.ENGLISH) + description.substring(1)
                weather.lat = lat
                weather.lon = lon
                weather.icon = WeatherFormatter.getWeatherIcon(weatherId.toInt(), isDayTime(weather, calendar), requireContext())

                weather.temperature = DecimalFormat("#.#").format(temperature.toDouble()) + " " + sharedRepository.getTempUnit()
                weatherArrayList.add(weather)
            }
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onItemClickListener(view: View?, position: Int) {
        val weather: Weather = recyclerAdapter!!.getItem(position)
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val bundle = Bundle()
        sharedRepository.setCityID(weather.cityId!!)
        bundle.putBoolean(MainActivity.SHOULD_REFRESH_FLAG, true)
        intent.putExtras(bundle)
        startActivity(intent)
        close()
    }

    private fun close() {
        val activity = activity
        activity?.supportFragmentManager?.popBackStack()
    }
}
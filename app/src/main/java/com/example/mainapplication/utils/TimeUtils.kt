package com.example.mainapplication.utils

import com.example.mainapplication.models.Weather
import java.util.*

object TimeUtils {
    fun isDayTime(weather: Weather, Cal: Calendar): Boolean {
        val sunrise = weather.sunrise
        val sunset = weather.sunset
        val day: Boolean
        day = if (sunrise != null && sunset != null) {
            val currentTime = Calendar.getInstance().time // Cal is always set to midnight
            // then get real time
            currentTime.after(weather.sunrise) && currentTime.before(weather.sunset)
        } else {
            // fallback
            val hourOfDay = Cal[Calendar.HOUR_OF_DAY]
            hourOfDay in 7..19
        }
        return day
    }
}
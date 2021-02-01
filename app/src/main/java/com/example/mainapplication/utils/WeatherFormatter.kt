package com.example.mainapplication.utils

import android.content.Context
import com.example.mainapplication.R

object WeatherFormatter {

    //Returns weather icon as text from weather.ttf
    fun getWeatherIcon(
        weatherId: Int, isDay: Boolean,
        context: Context
    ): String {
        val id = weatherId / 100
        var icon = ""
        when (id) {
            2 -> {
                // thunderstorm
                icon = when (weatherId) {
                    210, 211, 212, 221 -> context.getString(R.string.weather_lightning)
                    200, 201, 202, 230, 231, 232 -> context.getString(R.string.weather_thunderstorm)
                    else -> context.getString(R.string.weather_thunderstorm)
                }
            }
            3 -> {
                // drizzle/sprinkle
                icon = when (weatherId) {
                    302, 311, 312, 314 -> context.getString(R.string.weather_rain)
                    310 -> context.getString(R.string.weather_rain_mix)
                    313 -> context.getString(R.string.weather_showers)
                    300, 301, 321 -> context.getString(R.string.weather_sprinkle)
                    else -> context.getString(R.string.weather_sprinkle)
                }
            }
            5 -> {
                // rain
                icon = when (weatherId) {
                    500 -> context.getString(R.string.weather_sprinkle)
                    511 -> context.getString(R.string.weather_rain_mix)
                    520, 521, 522 -> context.getString(R.string.weather_showers)
                    531 -> context.getString(R.string.weather_storm_showers)
                    501, 502, 503, 504 -> context.getString(R.string.weather_rain)
                    else -> context.getString(R.string.weather_rain)
                }
            }
            6 -> {
                // snow
                icon = when (weatherId) {
                    611 -> context.getString(R.string.weather_sleet)
                    612, 613, 615, 616, 620 -> context.getString(R.string.weather_rain_mix)
                    600, 601, 602, 621, 622 -> context.getString(R.string.weather_snow)
                    else -> context.getString(R.string.weather_snow)
                }
            }
            7 -> {
                // atmosphere
                icon = when (weatherId) {
                    711 -> context.getString(R.string.weather_smoke)
                    721 -> context.getString(R.string.weather_day_haze)
                    731, 761, 762 -> context.getString(R.string.weather_dust)
                    751 -> context.getString(R.string.weather_sandstorm)
                    771 -> context.getString(R.string.weather_cloudy_gusts)
                    781 -> context.getString(R.string.weather_tornado)
                    701, 741 -> context.getString(R.string.weather_fog)
                    else -> context.getString(R.string.weather_fog)
                }
            }
            8 -> {
                // clear sky or cloudy
                icon = when (weatherId) {
                    800 -> if (isDay) context.getString(R.string.weather_day_sunny) else context.getString(
                        R.string.weather_night_clear
                    )
                    801, 802 -> if (isDay) context.getString(R.string.weather_day_cloudy) else context.getString(
                        R.string.weather_night_alt_cloudy
                    )
                    803, 804 -> context.getString(R.string.weather_cloudy)
                    else -> context.getString(R.string.weather_cloudy)
                }
            }
            9 -> {
                icon = when (weatherId) {
                    900 -> context.getString(R.string.weather_tornado)
                    901 -> context.getString(R.string.weather_storm_showers)
                    902 -> context.getString(R.string.weather_hurricane)
                    903 -> context.getString(R.string.weather_snowflake_cold)
                    904 -> context.getString(R.string.weather_hot)
                    905 -> context.getString(R.string.weather_windy)
                    906 -> context.getString(R.string.weather_hail)
                    957 -> context.getString(R.string.weather_strong_wind)
                    else -> context.getString(R.string.weather_strong_wind)
                }
            }
        }
        return icon
    }
}
package com.example.mainapplication.utils

import android.content.Context
import com.example.mainapplication.R
import com.example.mainapplication.activities.sharedRepository
import java.util.*

object UnitConverter {
    //Converts Temperature into different units
    fun convertTemperature(temperature: Float): Float {
        val unit = sharedRepository.getTempUnit()
        val result: Float
        result = when (unit) {
            "°C" -> temperature - 273.15f
            "°F" -> 9 * (temperature - 273.15f) / 5 + 32
            else -> temperature
        }
        return result
    }

    //Converts Rain height into different units
    fun getRainString(rainValue: Double): String {
        var rain = rainValue
        return if (rain > 0) {
            if (sharedRepository.getRainUnit() == "mm") {
                if (rain < 0.1) {
                    " (<0.1 mm)"
                } else {
                    String.format(Locale.ENGLISH, " (%.1f %s)", rain, sharedRepository.getRainUnit())
                }
            } else {
                rain /= 25.4
                if (rain < 0.01) {
                    " (<0.01 in)"
                } else {
                    String.format(Locale.ENGLISH, " (%.2f %s)", rain, sharedRepository.getRainUnit())
                }
            }
        } else {
            ""
        }
    }

    //Converts Pressure into different units
    fun convertPressure(pressure: Double): Double {
        return when {
            sharedRepository.getPressureUnit() == "kPa" -> {
                pressure / 10
            }
            sharedRepository.getPressureUnit() == "mm Hg" -> {
                (pressure * 0.750061561303)
            }
            sharedRepository.getPressureUnit() == "in Hg" -> {
                (pressure * 0.0295299830714)
            }
            else -> {
                pressure
            }
        }
    }

    //Converts Wind Speed into different units
    fun convertWind(wind: Double): Double {
        val result: Double
        val unit = sharedRepository.getSpeedUnit()
        result = when (unit) {
            "kph" -> wind * 3.6
            "mph" -> wind * 2.23693629205
            else -> wind
        }
        return result
    }
}
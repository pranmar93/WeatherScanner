package com.example.mainapplication.utils

import android.content.Context
import com.example.mainapplication.R
import kotlin.math.floor

enum class WindDirectionFormatter {
    NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST, EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST, WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST;

    //Return direction in the string form from the Wind Direction Array
    fun getDirectionString(context: Context): String {
        return context.resources.getStringArray(R.array.windDirections)[ordinal]
    }

    companion object {
        //Return type of Wind Direction
        fun byDegree(degree: Double): WindDirectionFormatter {
            val directions: Array<WindDirectionFormatter> = values()
            val availableNumberOfDirections = directions.size
            val index: Int = degreeToIndex(
                degree,
                values().size
            ) * availableNumberOfDirections / values().size
            return directions[index]
        }

        //Converting degree to index of the enum
        private fun degreeToIndex(degree1: Double, numberOfDirections: Int): Int {

            var degree = degree1
            degree %= 360.0

            // Adding Offset to make North as 0
            if (degree < 0)
                degree += 360.0

            degree += 180.0 / numberOfDirections
            val direction = floor(degree * numberOfDirections / 360).toInt()
            return direction % numberOfDirections
        }
    }
}
package com.example.mainapplication.models

import java.io.Serializable
import java.util.*

data class Weather (
    var cityId: String?,
    var city: String?,
    var country: String?,
    var date: Date?,
    var temperature: String?,
    var description: String?,
    var wind: String?,
    var windDirectionDegree: Double?,
    var pressure: String?,
    var humidity: String?,
    var rain: String?,
    var id: String?,
    var icon: String?,
    var sunrise: Date?,
    var sunset: Date?,
    var lat: Double = 0.0,
    var lon: Double = 0.0
): Serializable
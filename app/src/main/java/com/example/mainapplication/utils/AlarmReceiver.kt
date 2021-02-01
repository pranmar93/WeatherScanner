package com.example.mainapplication.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.example.mainapplication.activities.sharedRepository
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    var context: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        val packageReplacedAction: Boolean

        packageReplacedAction = if (Intent.ACTION_PACKAGE_REPLACED == intent.action) {
            val packageName = intent.getStringExtra(Intent.EXTRA_UID)
            packageName != null && packageName == context.packageName
        } else {
            false
        }

        if (Intent.ACTION_BOOT_COMPLETED == intent.action || packageReplacedAction) {
            val interval = sharedRepository.getRefreshInt()
            if (interval != "0") {
                setRecurringUpdate(context)
                getWeather()
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            // Get weather if last attempt failed or if 'update location in background' is activated
            val interval = sharedRepository.getRefreshInt()
            if (interval != "0" &&
                    (sharedRepository.getBackRefreshFailed() || isUpdateLocation())) {
                getWeather()
            }
        } else {
            getWeather()
        }
    }

    // This method calls the two methods below once it has determined a location
    private fun getWeather() {
            Log.d("Alarm", "Recurring alarm; requesting download service.")
            val failed: Boolean
            if (isNetworkAvailable()) {
                failed = false
                if (isUpdateLocation()) {
                    GetLocationAndWeatherTask().execute() // This method calls the two methods below once it has determined a location
                } else {
                    GetWeatherTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    GetForecastWeatherTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            } else {
                failed = true
            }
            sharedRepository.setBackRefreshFailed(failed)
        }

    private fun isNetworkAvailable(): Boolean {
        try {
            val connectivityManager =
                context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                return networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } catch (ex: Exception) {
            Log.d("AlarmReceiver", "isNetworkAvailable: ${ex.message}")
        }
        Log.d("AlarmReceiver", "isNetworkAvailable() exiting with false Exception")
        return false
    }

    private fun isUpdateLocation(): Boolean {
        return sharedRepository.getUpdateLocationAutomatically()
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetWeatherTask : AsyncTask<String?, String?, Void?>() {
        override fun doInBackground(vararg params: String?): Void? {
            try {
                val url = URL("https://api.openweathermap.org/data/2.5/weather?id=" + URLEncoder.encode(sharedRepository.getCityID(), "UTF-8") + "&appid=" + Constants.API_KEY)
                val urlConnection = url.openConnection() as HttpURLConnection
                var connectionBufferedReader: BufferedReader? = null
                try {
                    connectionBufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    if (urlConnection.responseCode == 200) {
                        val result = StringBuilder()
                        var line: String?
                        while (connectionBufferedReader.readLine().also { line = it } != null) {
                            result.append(line).append("\n")
                        }

                        sharedRepository.setLastToday(result.toString())

                        val now = Calendar.getInstance()
                        val lastUpdate = now.timeInMillis
                        sharedRepository.setLastUpdateTime(lastUpdate)
                    }
                } finally {
                    connectionBufferedReader?.close()
                }
            } catch (e: IOException) {
                // No connection
            }
            return null
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetForecastWeatherTask : AsyncTask<String?, String?, Void?>() {
        override fun onPreExecute() {}
        override fun doInBackground(vararg params: String?): Void? {
            try {
                val url = URL("https://api.openweathermap.org/data/2.5/forecast?id=" + URLEncoder.encode(sharedRepository.getCityID(), "UTF-8") + "&mode=json&appid=" + Constants.API_KEY)
                val urlConnection = url.openConnection() as HttpURLConnection
                var connectionBufferedReader: BufferedReader? = null
                try {
                    connectionBufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    if (urlConnection.responseCode == 200) {
                        val result = StringBuilder()
                        var line: String?
                        while (connectionBufferedReader.readLine().also { line = it } != null) {
                            result.append(line).append("\n")
                        }

                        sharedRepository.setLastForecast(result.toString())
                    }
                } finally {
                    connectionBufferedReader?.close()
                }
            } catch (e: IOException) {
                // No connection
            }
            return null
        }

        override fun onPostExecute(v: Void?) {}
    }

    //Detects the Current Location and returns the current weather updates on clicking Detect Location
    @SuppressLint("StaticFieldLeak")
    inner class GetLocationAndWeatherTask : AsyncTask<String?, String?, Void?>() {
        private var locationManager: LocationManager? = null
        private var locationListener: BackgroundLocationListener? = null
        override fun onPreExecute() {
            Log.d(Companion.TAG, "Trying to determine location...")
            locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = BackgroundLocationListener()
            try {
                if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    // Only uses 'network' location, as asking the GPS every time would drain too much battery
                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
                } else {
                    Log.d(Companion.TAG, "'Network' location is not enabled. Cancelling determining location.")
                    onPostExecute(null)
                }
            } catch (e: SecurityException) {
                Log.e(Companion.TAG, "Couldn't request location updates. Probably this is an Android (>M) runtime permissions issue ", e)
            }
        }

        override fun doInBackground(vararg params: String?): Void? {
            val startTime = System.currentTimeMillis()
            var runningTime: Long = 0
            while (locationListener!!.location == null && runningTime < MAX_RUNNING_TIME) { // Give up after 30 seconds
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    Log.e(Companion.TAG, "Error occurred while waiting for location update", e)
                }
                runningTime = System.currentTimeMillis() - startTime
            }
            if (locationListener!!.location == null) {
                Log.d(Companion.TAG, String.format("Couldn't determine location in less than %s seconds", MAX_RUNNING_TIME / 1000))
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            val location = locationListener!!.location
            if (location != null) {
                Log.d(Companion.TAG, String.format("Determined location: latitude %f - longitude %f", location.latitude, location.longitude))
                GetCityNameTask().execute(location.latitude.toString(), location.longitude.toString())
            } else {
                Log.e(Companion.TAG, "Couldn't determine location. Using last known location.")
                GetWeatherTask().executeOnExecutor(THREAD_POOL_EXECUTOR)
                GetForecastWeatherTask().executeOnExecutor(THREAD_POOL_EXECUTOR)
            }
            try {
                locationManager!!.removeUpdates(locationListener)
            } catch (e: SecurityException) {
                Log.e(Companion.TAG, "Couldn't remove location updates. Probably this is an Android (>M) runtime permissions", e)
            }
        }

        inner class BackgroundLocationListener : LocationListener {
            var location: Location? = null
                private set

            override fun onLocationChanged(location: Location) {
                this.location = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

        }
    }

    //Getting City Name using the Latitude and Longitude
    @SuppressLint("StaticFieldLeak")
    inner class GetCityNameTask : AsyncTask<String?, String?, Void?>() {
        override fun doInBackground(vararg params: String?): Void? {
            val lat = params[0]
            val lon = params[1]
            try {
                val url = URL("https://api.openweathermap.org/data/2.5/weather?q=&lat=$lat&lon=$lon&appid=${Constants.API_KEY}")
                Log.d(Companion.TAG, "Request: $url")
                val urlConnection = url.openConnection() as HttpURLConnection
                if (urlConnection.responseCode == 200) {
                    var connectionBufferedReader: BufferedReader? = null
                    try {
                        connectionBufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                        val result = StringBuilder()
                        var line: String?
                        while (connectionBufferedReader.readLine().also { line = it } != null) {
                            result.append(line).append("\n")
                        }
                        Log.d(Companion.TAG, "JSON Result: $result")
                        val reader = JSONObject(result.toString())
                        val cityId = reader.getString("id")
                        val city = reader.getString("name")
                        var country = ""
                        val countryObj = reader.optJSONObject("sys")
                        if (countryObj != null) {
                            country = ", " + countryObj.getString("country")
                        }
                        Log.d(Companion.TAG, "City: $city$country")
                        val lastCity = sharedRepository.getCurrentCity()
                        val currentCity = city + country

                        sharedRepository.setCityID(cityId)
                        sharedRepository.setCurrentCity(currentCity)
                        sharedRepository.setCityChanged(currentCity != lastCity)

                    } catch (e: JSONException) {
                        Log.e(Companion.TAG, "An error occurred while reading the JSON object", e)
                    } finally {
                        connectionBufferedReader?.close()
                    }
                } else {
                    Log.e(Companion.TAG, "Error: Response code " + urlConnection.responseCode)
                }
            } catch (e: IOException) {
                Log.e(Companion.TAG, "Connection error", e)
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            GetWeatherTask().execute()
            GetForecastWeatherTask().execute()
        }

    }

    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
        private const val MAX_RUNNING_TIME = 30 * 1000.toDouble()

        //retreiving Alarm Manager Constants
        private fun intervalMillisForRecurringAlarm(intervalPref: String?): Long {
            return when (val interval = intervalPref!!.toInt()) {
                0 -> 0 // special case for cancel
                15 -> AlarmManager.INTERVAL_FIFTEEN_MINUTES
                30 -> AlarmManager.INTERVAL_HALF_HOUR
                1 -> AlarmManager.INTERVAL_HOUR
                12 -> AlarmManager.INTERVAL_HALF_DAY
                24 -> AlarmManager.INTERVAL_DAY
                else -> (interval * 3600000).toLong()
            }
        }

        //set auto update according to the user chosen setting
        //by default set to 1 hr
        fun setRecurringUpdate(context: Context) {
            val intervalPref = sharedRepository.getRefreshInt()
            val refresh = Intent(context, AlarmReceiver::class.java)
            val recurringRefresh = PendingIntent.getBroadcast(context,
                    0, refresh, PendingIntent.FLAG_CANCEL_CURRENT)
            val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intervalMillis = intervalMillisForRecurringAlarm(intervalPref)
            if (intervalMillis == 0L) {
                // Cancel previous alarm
                alarms.cancel(recurringRefresh)
            } else {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + intervalMillis,
                        intervalMillis,
                        recurringRefresh)
            }
        }
    }
}
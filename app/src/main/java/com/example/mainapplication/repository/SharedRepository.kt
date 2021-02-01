package com.example.mainapplication.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import com.example.mainapplication.models.Weather
import com.example.mainapplication.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedRepository {

    private val TAG = SharedRepository::class.java.simpleName

    fun getLastUpdateTime(): Long {
        return sharedPref?.getLong("lastUpdate", -1)!!
    }

    fun setLastUpdateTime(time: Long) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putLong("lastUpdate", time)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun setCityID(id: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("cityId", id)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getCityID(): String {
        return sharedPref?.getString("cityId", Constants.DEFAULT_CITY_ID)!!
    }

    fun setTempUnit(unit: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("unit", unit)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getTempUnit(): String {
        return sharedPref?.getString("unit", "°C")!!
    }

    fun setSpeedUnit(unit: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("speedUnit", unit)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getSpeedUnit(): String {
        return sharedPref?.getString("speedUnit", "m/s")!!
    }

    fun setPressureUnit(unit: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("pressureUnit", unit)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getPressureUnit(): String {
        return sharedPref?.getString("pressureUnit", "hPa")!!
    }

    fun setRainUnit(unit: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("lengthUnit", unit)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getRainUnit(): String {
        return sharedPref?.getString("lengthUnit", "mm")!!
    }

    fun setCurrentCity(city: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("city", city)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getCurrentCity(): String {
        return sharedPref?.getString("city", "")!!
    }

    fun setRefreshInt(interval: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("refreshInterval", interval)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getRefreshInt(): String {
        return sharedPref?.getString("refreshInterval", "1")!!
    }

    fun setCityChanged(changed: Boolean) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putBoolean("cityChanged", changed)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun isCityChanged(): Boolean {
        return sharedPref?.getBoolean("cityChanged", false)!!
    }

    fun setBackRefreshFailed(fail: Boolean) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putBoolean("backgroundRefreshFailed", fail)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getBackRefreshFailed(): Boolean {
        return sharedPref?.getBoolean("backgroundRefreshFailed", false)!!
    }

    fun getUpdateLocationAutomatically(): Boolean {
        return sharedPref?.getBoolean("updateLocationAutomatically", false)!!
    }

    fun setLastToday(result: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("lastToday", result)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getLastToday(): String? {
        return sharedPref?.getString("lastToday", null)
    }

    fun setLastForecast(result: String) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putString("lastforecast", result)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getLastForecast(): String? {
        return sharedPref?.getString("lastforecast", null)
    }

    fun setLatitude(lat: Float) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putFloat("latitude", lat)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getLatitude(): Float? {
        return sharedPref?.getFloat("latitude", 0.0f)
    }

    fun setLongitude(lon: Float) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                spEditor?.putFloat("longitude", lon)
                spEditor?.apply()
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getLongitude(): Float? {
        return sharedPref?.getFloat("longitude", 0.0f)
    }

    fun getFavourites(): ArrayList<Weather> {
        val obj = Gson()
        val jsonPreferences = sharedPref?.getString("FavouriteLocations", null)
        if (jsonPreferences != null) {
            try {
                val type = object : TypeToken<ArrayList<Weather>>() {}.type
                return obj.fromJson(jsonPreferences, type)
            } catch (ex: Exception) {
                Log.d(TAG, "getFavourites: ${ex.message}")
            }
        }
        return ArrayList()
    }

    fun setFavourites(location: ArrayList<Weather>) {
        class ExecutorFunction : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                val obj = Gson()
                try {
                    val jsonFavLoc: String = obj.toJson(location)
                    spEditor?.putString("FavouriteLocations", jsonFavLoc)
                    spEditor?.apply()
                } catch (ex: Exception) {
                    Log.d(TAG, "setFavourites: ${ex.message}")
                }
                return null
            }
        }

        val func = ExecutorFunction()
        func.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    companion object {

        @Volatile
        private var sharedRepository: SharedRepository? = null
        private var sharedPref: SharedPreferences? = null
        private var spEditor: SharedPreferences.Editor? = null

        fun getSharedRepository(activity: Activity): SharedRepository? {
            if (sharedRepository == null) {
                synchronized(SharedRepository::class.java) {
                    if (sharedRepository == null) {
                        sharedRepository = SharedRepository()
                        sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
                        spEditor = sharedPref!!.edit()
                    }
                }
            }
            return sharedRepository
        }
    }
}
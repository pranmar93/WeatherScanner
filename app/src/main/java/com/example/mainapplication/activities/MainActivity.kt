package com.example.mainapplication.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.mainapplication.GenericRequestTask
import com.example.mainapplication.R
import com.example.mainapplication.adapter.ViewPagerAdapter
import com.example.mainapplication.adapter.WeatherRecyclerAdapter
import com.example.mainapplication.fragment.*
import com.example.mainapplication.models.Weather
import com.example.mainapplication.repository.SharedRepository
import com.example.mainapplication.utils.*
import com.example.mainapplication.utils.TimeUtils.isDayTime
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*

lateinit var sharedRepository: SharedRepository

class MainActivity : AppCompatActivity(), LocationListener {

    private val todayWeather: Weather = Weather("", "", "", Date(), "", "", "", 0.0, "", "", "", "", "", Date(), Date(), 0.0, 0.0)

    private var todayTemperature: TextView? = null
    private var todayDescription: TextView? = null
    private var todayWind: TextView? = null
    private var todayPressure: TextView? = null
    private var todayHumidity: TextView? = null
    private var todaySunrise: TextView? = null
    private var todaySunset: TextView? = null
    private var lastUpdate: TextView? = null
    private var todayIcon: TextView? = null
    private var tabLayout: TabLayout? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var viewPager2: ViewPager2? = null
    private var viewPagerAdapter: ViewPagerAdapter? = null
    private var favouriteButton: ToggleButton? = null

    private var appView: View? = null

    private var locationManager: LocationManager? = null

    private var destroyed = false

    private var listLaterWeather: MutableList<Weather>? = ArrayList()
    private var listTodayWeather: MutableList<Weather> = ArrayList()
    private var listTomorrowWeather: MutableList<Weather> = ArrayList()

    var recentCityId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initiate activity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedRepository = SharedRepository.getSharedRepository(this)!!

        appView = findViewById(R.id.viewApp)

        //Swipe Refresh Layout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)

        // Load toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize textboxes
        todayTemperature = findViewById(R.id.todayTemperature)
        todayDescription = findViewById(R.id.todayDescription)
        todayWind = findViewById(R.id.todayWind)
        todayPressure = findViewById(R.id.todayPressure)
        todayHumidity = findViewById(R.id.todayHumidity)
        todaySunrise = findViewById(R.id.todaySunrise)
        todaySunset = findViewById(R.id.todaySunset)
        lastUpdate = findViewById(R.id.lastUpdate)
        todayIcon = findViewById(R.id.todayIcon)
        favouriteButton = findViewById(R.id.button_favourite)
        favouriteButton!!.setBackgroundResource(R.drawable.drawable_favourite_state_main)

        favouriteButton!!.setOnCheckedChangeListener { _, isChecked ->
            val favouritesCities = sharedRepository.getFavourites()
            val cityIDs = favouritesCities.map {it.cityId}
            if (isChecked) {
                if (todayWeather.cityId !in cityIDs) {
                    favouritesCities.add(todayWeather)
                    sharedRepository.setFavourites(favouritesCities)
                    Snackbar.make(appView!!, "Marked Favourite", Snackbar.LENGTH_LONG).show()
                }
            } else {
                if (todayWeather.cityId in cityIDs) {
                    val city = favouritesCities.find { it.cityId == todayWeather.cityId }
                    favouritesCities.remove(city)
                    sharedRepository.setFavourites(favouritesCities)
                    Snackbar.make(appView!!, "Marked Unfavourite", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        val weatherFont = Typeface.createFromAsset(this.assets, "fonts/weather.ttf")
        todayIcon!!.typeface = weatherFont

        // Initialize viewPager
        viewPager2 = findViewById(R.id.viewPager2)
        tabLayout = findViewById(R.id.tabs)

        destroyed = false

        // Preload data from repository cache
        preloadWeather()
        updateLastUpdateTime(sharedRepository.getLastUpdateTime())

        // Schedule Update
        AlarmReceiver.setRecurringUpdate(this)
        swipeRefreshLayout!!.setOnRefreshListener {
            refreshWeather()
            swipeRefreshLayout!!.isRefreshing = false
        }

        //Refresh only when scrolled to top
        appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { _, verticalOffset ->
            swipeRefreshLayout!!.isEnabled = verticalOffset == 0
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) return
        val bundle = intent.extras
        if (bundle != null && bundle.getBoolean(SHOULD_REFRESH_FLAG)) {
            refreshWeather()
        }
    }

    fun getAdapter(fragment: Fragment): WeatherRecyclerAdapter? {
        return when (fragment) {
            is TodayFragment -> {
                WeatherRecyclerAdapter(listTodayWeather)
            }
            is TomorrowFragment -> {
                WeatherRecyclerAdapter(listTomorrowWeather)
            }
            else -> {
                WeatherRecyclerAdapter(listLaterWeather)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
        updateTodayWeatherUI()
        updateForecastWeatherUI()
    }

    override fun onResume() {
        super.onResume()
        if (shouldUpdate() && isNetworkAvailable()) {
            getTodayWeather()
            getForecastWeather()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
        if (locationManager != null) {
            try {
                locationManager!!.removeUpdates(this@MainActivity)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkPermissions() {
        val hasForegroundLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasForegroundLocationPermission) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), PERMISSION_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCityByLocation()
                } else {
                    val showRationale: Boolean =
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    if (showRationale) {
                        val builder = AlertDialog.Builder(this).create()
                        val title = resources.getString(R.string.perm_title_location)
                        builder.setTitle(title)
                        val msg = resources.getString(R.string.perm_temp_location)
                        builder.setMessage(msg)
                        builder.setButton(
                            Dialog.BUTTON_POSITIVE,
                            resources.getString(R.string.dialog_ok)
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.setOnDismissListener {
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                PERMISSION_FINE_LOCATION
                            )
                        }
                        builder.setCanceledOnTouchOutside(false)
                        builder.setCancelable(false)
                        builder.show()
                    } else {
                        val builder = AlertDialog.Builder(this).create()
                        val title = resources.getString(R.string.perm_title_location)
                        builder.setTitle(title)
                        val msg = resources.getString(R.string.perm_perm_location)
                        builder.setMessage(msg)
                        builder.setButton(
                            Dialog.BUTTON_POSITIVE,
                            resources.getString(R.string.dialog_ok)
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.setOnDismissListener { openAppSettings() }
                        builder.setCanceledOnTouchOutside(false)
                        builder.setCancelable(false)
                        builder.show()
                    }
                }
                return
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        startActivity(intent)
        finishAffinity()
    }

    private fun preloadWeather() {
        val lastToday = sharedRepository.getLastToday()
        if (lastToday != null && lastToday.isNotEmpty()) {
            TodayWeatherTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastToday)
        }
        val lastForecast = sharedRepository.getLastForecast()
        if (lastForecast != null && lastForecast.isNotEmpty()) {
            ForecastWeatherTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastForecast)
        }
    }

    private fun getTodayWeather() {
        TodayWeatherTask(this, this).execute()
    }

    private fun getForecastWeather() {
        ForecastWeatherTask(this, this).execute()
    }

    //Alert Dialog for Searching a city
    private fun searchCities() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.maxLines = 1
        input.isSingleLine = true
        val inputLayout = TextInputLayout(this)
        inputLayout.setPadding(32, 0, 32, 0)
        inputLayout.addView(input)
        val alert = AlertDialog.Builder(this)
        alert.setTitle(this.getString(R.string.search_title))
        alert.setView(inputLayout)
        alert.setPositiveButton(R.string.dialog_ok) { _, _ ->
            val result = input.text.toString().trim { it <= ' ' }
            if (result.isNotEmpty()) {
                FindCitiesByNameTask(applicationContext,
                    this@MainActivity).execute("city", result)
            }
        }
        alert.setNegativeButton(R.string.dialog_cancel) { _, _ ->

        }
        alert.show()
    }

    //Loading About Fragment
    private fun aboutDialog() {
        AboutDialogFragment().show(supportFragmentManager, null)
    }

    //Getting rain from the JSON result
    private fun getRainString(rainObj: JSONObject?): String {
        var rain = "0"
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail")
            if ("fail" == rain) {
                rain = rainObj.optString("1h", "0")
            }
        }
        return rain
    }

    //Parsing JSON response for the Current Weather data
    private fun parseTodayJson(result: String): ParseResults? {
        try {
            val reader = JSONObject(result)
            val code = reader.optString("cod")
            if ("404" == code) {
                return ParseResults.CITY_NOT_FOUND
            }
            val cityId = reader.getString("id")
            todayWeather.cityId = cityId
            val city = reader.getString("name")
            var country = ""
            val countryObj = reader.optJSONObject("sys")
            if (countryObj != null) {
                country = countryObj.getString("country")
                todayWeather.sunrise = Date(java.lang.Long.parseLong(countryObj.getString("sunrise")) * 1000)
                todayWeather.sunset = Date(java.lang.Long.parseLong(countryObj.getString("sunset"))* 1000)
            }
            todayWeather.city = (city)
            todayWeather.country = (country)
            val coordinates = reader.getJSONObject("coord")
            todayWeather.lat = (coordinates.getDouble("lat"))
            todayWeather.lon = (coordinates.getDouble("lon"))

            sharedRepository.setLatitude(todayWeather.lat.toFloat())
            sharedRepository.setLongitude(todayWeather.lon.toFloat())

            val main = reader.getJSONObject("main")
            todayWeather.temperature = (main.getString("temp"))
            todayWeather.description = (reader.getJSONArray("weather").getJSONObject(0).getString("description"))
            val windObj = reader.getJSONObject("wind")
            todayWeather.wind = (windObj.getString("speed"))
            if (windObj.has("deg")) {
                todayWeather.windDirectionDegree = (windObj.getDouble("deg"))
            } else {
                Log.e("parseTodayJson", "No wind direction available")
                todayWeather.windDirectionDegree = (null)
            }
            todayWeather.pressure = (main.getString("pressure"))
            todayWeather.humidity = (main.getString("humidity"))
            val rainObj = reader.optJSONObject("rain")
            val rain: String
            rain = if (rainObj != null) {
                getRainString(rainObj)
            } else {
                val snowObj = reader.optJSONObject("snow")
                snowObj?.let { getRainString(it) } ?: "0"
            }
            todayWeather.rain = (rain)
            val idString = reader.getJSONArray("weather").getJSONObject(0).getString("id")
            todayWeather.id = (idString)
            todayWeather.icon = (WeatherFormatter.getWeatherIcon(idString.toInt(), isDayTime(todayWeather, Calendar.getInstance()), this))
            sharedRepository.setLastToday(result)
        } catch (e: JSONException) {
            Log.e("JSONException Data", result)
            e.printStackTrace()
            return ParseResults.JSON_EXCEPTION
        }
        return ParseResults.OK
    }

    //Updating Current Weather in UI
    @SuppressLint("SetTextI18n")
    private fun updateTodayWeatherUI() {
        try {
            if (todayWeather.country!!.isEmpty()) {
                preloadWeather()
                return
            }
        } catch (e: Exception) {
            preloadWeather()
            return
        }
        val city: String = todayWeather.city!!
        val country: String = todayWeather.country!!
        val timeFormat = DateFormat.getTimeFormat(applicationContext)
        val actionBar = supportActionBar
        val cityName = city + if (country.isEmpty()) "" else ", $country"
        actionBar?.title = cityName

        // Temperature
        val temperature: Float = UnitConverter.convertTemperature(todayWeather.temperature!!.toFloat())

        // Rain
        val rain: Double = todayWeather.rain!!.toDouble()
        val rainString: String = UnitConverter.getRainString(rain)

        // Wind
        var wind: Double
        wind = try {
            todayWeather.wind!!.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
        wind = UnitConverter.convertWind(wind)

        // Pressure
        val pressure: Double = UnitConverter.convertPressure(todayWeather.pressure!!.toDouble())
        todayTemperature!!.text = DecimalFormat("0.#").format(temperature.toDouble()) + " " + sharedRepository.getTempUnit()
        todayDescription!!.text = todayWeather.description!!.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                todayWeather.description!!.substring(1) + rainString
        todayWind!!.text = getString(R.string.wind) + ": " + DecimalFormat("0.0").format(wind) + " " +
                    sharedRepository.getSpeedUnit() + " " + getWindDirectionString(this, todayWeather)
        todayPressure!!.text = getString(R.string.pressure) + ": " + DecimalFormat("0.0").format(pressure) + " " +
                sharedRepository.getPressureUnit()
        todayHumidity!!.text = getString(R.string.humidity) + ": " + todayWeather.humidity + " %"
        todaySunrise!!.text = getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.sunrise!!)
        todaySunset!!.text = getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.sunset!!)
        todayIcon!!.text = todayWeather.icon

        val favourites = sharedRepository.getFavourites()
        favouriteButton!!.isChecked = todayWeather.cityId in favourites.map {it.cityId}
    }

    //Parsing JSON Response for Forecast Data
    fun parseForecastJson(result: String?): ParseResults? {
        var i: Int
        try {
            val reader = JSONObject(result!!)
            val code = reader.optString("cod")
            if ("404" == code) {
                if (listLaterWeather == null) {
                    listLaterWeather = ArrayList()
                    listTodayWeather = ArrayList()
                    listTomorrowWeather = ArrayList()
                }
                return ParseResults.CITY_NOT_FOUND
            }
            listLaterWeather = ArrayList()
            listTodayWeather = ArrayList()
            listTomorrowWeather = ArrayList()
            val list = reader.getJSONArray("list")
            i = 0
            while (i < list.length()) {
                val weather = Weather("", "", "", Date(), "", "", "", 0.0, "", "", "", "", "", Date(), Date(), 0.0, 0.0)
                val listItem = list.getJSONObject(i)
                val main = listItem.getJSONObject("main")
                weather.date = Date(java.lang.Long.parseLong(listItem.getString("dt"))*1000)
                weather.temperature = (main.getString("temp"))
                weather.description = (listItem.optJSONArray("weather").getJSONObject(0).getString("description"))
                val windObj = listItem.optJSONObject("wind")
                if (windObj != null) {
                    weather.wind = (windObj.getString("speed"))
                    weather.windDirectionDegree = (windObj.getDouble("deg"))
                }
                weather.pressure = main.getString("pressure")
                weather.humidity = main.getString("humidity")
                val rainObj = listItem.optJSONObject("rain")
                var rain: String
                rain = if (rainObj != null) {
                    getRainString(rainObj)
                } else {
                    val snowObj = listItem.optJSONObject("snow")
                    snowObj?.let { getRainString(it) } ?: "0"
                }
                weather.rain = rain
                val idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id")
                weather.id = idString
                val dateMsString = listItem.getString("dt") + "000"
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateMsString.toLong()
                weather.icon = WeatherFormatter.getWeatherIcon(idString.toInt(), isDayTime(weather, cal), this)
                val today = Calendar.getInstance()
                today[Calendar.HOUR_OF_DAY] = 0
                today[Calendar.MINUTE] = 0
                today[Calendar.SECOND] = 0
                today[Calendar.MILLISECOND] = 0
                val tomorrow = today.clone() as Calendar
                tomorrow.add(Calendar.DAY_OF_YEAR, 1)
                val later = today.clone() as Calendar
                later.add(Calendar.DAY_OF_YEAR, 2)
                when {
                    cal.before(tomorrow) -> {
                        listTodayWeather.add(weather)
                    }
                    cal.before(later) -> {
                        listTomorrowWeather.add(weather)
                    }
                    else -> {
                        listLaterWeather!!.add(weather)
                    }
                }
                i++
            }
            sharedRepository.setLastForecast(result)
        } catch (e: JSONException) {
            Log.e("JSONException Data", result!!)
            e.printStackTrace()
            return ParseResults.JSON_EXCEPTION
        }
        return ParseResults.OK
    }

    private fun updateForecastWeatherUI() {
        if (destroyed) {
            return
        }

        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager2!!.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout!!, viewPager2!!) { tab, position ->
            when(position) {
                0 -> tab.text = getString(R.string.today)
                1 -> tab.text = getString(R.string.tomorrow)
                2 -> tab.text = getString(R.string.later)
            }
        }.attach()


        val currentPage = viewPager2!!.currentItem

        viewPager2!!.setCurrentItem(currentPage, false)
    }

    private fun isNetworkAvailable(): Boolean {
        try {
            val connectivityManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                return networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } catch (ex: Exception) {
            Log.d("MainActivity", "isNetworkAvailable: ${ex.message}")
        }
        Log.d("MainActivity", "isNetworkAvailable() exiting with false Exception")
        return false
    }

    private fun shouldUpdate(): Boolean {
        val lastUpdate = sharedRepository.getLastUpdateTime()
        val cityChanged = sharedRepository.isCityChanged()
        // Update if never checked or last update is longer ago than specified threshold
        return cityChanged || lastUpdate < 0 || Calendar.getInstance().timeInMillis - lastUpdate > NO_UPDATE_REQUIRED_THRESHOLD
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_refresh) {
            refreshWeather()
            return true
        }
        if (id == R.id.action_search) {
            searchCities()
            return true
        }
        if (id == R.id.action_location) {
            getCityByLocation()
            return true
        }
        if (id == R.id.action_favourites) {
            val fragment =
                FavouriteDialogFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            fragmentTransaction.add(android.R.id.content, fragment)
                .addToBackStack(null).commit()
        }
        if (id == R.id.action_settings) {
            val fragment =
                SettingsDialogFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            fragmentTransaction.add(android.R.id.content, fragment)
                .addToBackStack(null).commit()
        }
        if (id == R.id.action_about) {
            aboutDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun refreshWeather() {
        if (isNetworkAvailable()) {
            getTodayWeather()
            getForecastWeather()
        } else {
            Snackbar.make(appView!!, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getCityByLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                showLocationSettingsDialog()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_FINE_LOCATION)
            }
        } else if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showProgressBar(getString(R.string.getting_location), true, locationManager!!)

            if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)
            }

        } else {
            showLocationSettingsDialog()
        }
    }

    private fun showLocationSettingsDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.location_settings)
        alertDialog.setMessage(R.string.location_settings_message)
        alertDialog.setPositiveButton(R.string.location_settings_button) { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        alertDialog.setNegativeButton(R.string.dialog_cancel) { dialog, _ -> dialog.cancel() }
        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {
        hideProgressBar()
        try {
            locationManager!!.removeUpdates(this)
        } catch (e: SecurityException) {
            Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e)
        }
        Log.i("LOCATION (" + location.provider.toUpperCase(Locale.ENGLISH) + ")", location.latitude.toString() + ", " + location.longitude)
        val latitude = location.latitude
        val longitude = location.longitude
        ProvideCityNameTask(this, this).execute("coords", latitude.toString(),
            longitude.toString()
        )
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String?) {}

    override fun onProviderDisabled(provider: String?) {}


    @SuppressLint("StaticFieldLeak")
    inner class TodayWeatherTask(context: Context?, activity: MainActivity?) : GenericRequestTask(context!!, activity!!) {
        override fun onPreExecute() {
            loading = 0
            super.onPreExecute()
        }

        override fun parseResponse(response: String?): ParseResults {
            return parseTodayJson(response!!)!!
        }

        override val aPIName: String
            get() = "weather"

        override fun updateMainUI() {
            updateTodayWeatherUI()
            updateLastUpdateTime(
                    sharedRepository.getLastUpdateTime()
            )
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ForecastWeatherTask(context: Context?, activity: MainActivity?) : GenericRequestTask(context!!, activity!!) {
        override fun parseResponse(response: String?): ParseResults {
            return parseForecastJson(response)!!
        }

        override val aPIName: String
            get() = "forecast"

        override fun updateMainUI() {
            updateForecastWeatherUI()
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class FindCitiesByNameTask(context: Context?, activity: MainActivity?) : GenericRequestTask(context!!, activity!!) {
        override fun onPreExecute() { /*Nothing*/
        }

        override fun parseResponse(response: String?): ParseResults {
            try {
                val reader = JSONObject(response!!)
                val count = reader.optInt("count")
                if (count == 0) {
                    Log.e("Geolocation", "No city found")
                    return ParseResults.CITY_NOT_FOUND
                }

//                saveLocation(reader.getString("id"));
                val cityList = reader.getJSONArray("list")
                if (cityList.length() > 1) {
                    launchLocationPickerDialog(cityList)
                } else {
                    recentCityId = sharedRepository.getCityID()
                    sharedRepository.setCityID(cityList.getJSONObject(0).getString("id"))
                }
            } catch (e: JSONException) {
                Log.e("JSONException Data", response!!)
                e.printStackTrace()
                return ParseResults.JSON_EXCEPTION
            }
            return ParseResults.OK
        }

        override val aPIName: String
            get() = "find"

        override fun onPostExecute(output: TaskOutput) {
            /* Handle possible errors only */
            handleTaskOutput(output)
            refreshWeather()
        }
    }

    private fun launchLocationPickerDialog(cityList: JSONArray) {
        val fragment = AmbiguousLocationDialogFragment()
        val bundle = Bundle()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        bundle.putString("cityList", cityList.toString())
        fragment.arguments = bundle
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.add(android.R.id.content, fragment)
                .addToBackStack(null).commit()
    }


    @SuppressLint("StaticFieldLeak")
    inner class ProvideCityNameTask(context: Context?, activity: MainActivity?) : GenericRequestTask(context!!, activity!!) {
        override fun onPreExecute() { /*Nothing*/
        }

        override val aPIName: String
            get() = "weather"

        override fun parseResponse(response: String?): ParseResults {
            Log.i("RESULT", response!!)
            try {
                val reader = JSONObject(response)
                val code = reader.optString("cod")
                if ("404" == code) {
                    Log.e("Geolocation", "No city found")
                    return ParseResults.CITY_NOT_FOUND
                }
                recentCityId = sharedRepository.getCityID()
                sharedRepository.setCityID(reader.getString("id"))
            } catch (e: JSONException) {
                Log.e("JSONException Data", response)
                e.printStackTrace()
                return ParseResults.JSON_EXCEPTION
            }
            return ParseResults.OK
        }

        override fun onPostExecute(output: TaskOutput) {
            /* Handle possible errors only */
            handleTaskOutput(output)
            refreshWeather()
        }
    }

    //Display Last Update Time
    private fun updateLastUpdateTime(timeInMillis: Long) {
        if (timeInMillis < 0) {
            // No time
            lastUpdate!!.text = ""
        } else {
            val lastCheckedDate = Date(timeInMillis)
            val timeFormat = DateFormat.getTimeFormat(this).format(lastCheckedDate)
            lastUpdate!!.text = getString(R.string.last_update) + timeFormat
        }
    }

    //To display progress bar
    fun showProgressBar(text: String, showButton: Boolean, action: LocationManager?) {
        val loadingProgress = findViewById<ConstraintLayout>(R.id.progress_homepage)
        val loadingText = loadingProgress.findViewById<TextView>(R.id.loadingText)
        val layoutCancel = loadingProgress.findViewById<LinearLayout>(R.id.layout_cancel)
        val btnCancel = loadingProgress.findViewById<Button>(R.id.btn_cancel)

        loadingText.text = text

        if (loadingProgress.visibility != View.VISIBLE) {
            if (showButton) {
                layoutCancel.visibility = View.VISIBLE
                btnCancel.setOnClickListener {
                    action!!.removeUpdates(this@MainActivity)
                    hideProgressBar()
                }
            } else {
                layoutCancel.visibility = View.GONE
            }

            loadingProgress.visibility = View.VISIBLE
        }
    }

    //To hide progress bar
    fun hideProgressBar() {
        val loadingProgress = findViewById<ConstraintLayout>(R.id.progress_homepage)

        if (loadingProgress.visibility == View.VISIBLE) {
            loadingProgress.visibility = View.GONE
        }
    }

    companion object {
        const val PERMISSION_FINE_LOCATION = 786

        //Flag to represent if UI should refresh
        const val SHOULD_REFRESH_FLAG = "shouldRefresh"

        // Update weather if last update is older than this much time
        const val NO_UPDATE_REQUIRED_THRESHOLD = 300000

        // get direction of the wind according to the standards
        fun getWindDirectionString(context: Context?, weather: Weather): String {
            if (weather.wind!!.toDouble() != 0.0) {
                return WindDirectionFormatter.byDegree(weather.windDirectionDegree!!)
                    .getDirectionString(context!!)
            }
            return ""
        }
    }
}
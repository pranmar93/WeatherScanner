package com.example.mainapplication

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import com.example.mainapplication.activities.MainActivity
import com.example.mainapplication.activities.sharedRepository
import com.example.mainapplication.utils.Constants
import com.example.mainapplication.utils.ParseResults
import com.example.mainapplication.utils.TaskOutput
import com.example.mainapplication.utils.TaskResults
import com.google.android.material.snackbar.Snackbar
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

abstract class GenericRequestTask(
    protected var context: Context,
    private var activity: MainActivity
) : AsyncTask<String?, String?, TaskOutput>() {

    var loading = 0
    override fun onPreExecute() {
        incLoadingCounter()
        activity.showProgressBar(context.getString(R.string.downloading_data), false, null)
    }

    override fun doInBackground(vararg params: String?): TaskOutput {
        val output = TaskOutput()
        var response = ""
        var reqParams = arrayOf<String>()
        if (params.isNotEmpty()) {
            when (params[0]) {
                "cachedResponse" -> {
                    response = params[1]!!
                    // Actually we did nothing in this case :)
                    output.taskResult = TaskResults.SUCCESS
                }
                "coords" -> {
                    val lat = params[1]
                    val lon = params[2]
                    reqParams = arrayOf("coords", lat!!, lon!!)
                }
                "city" -> {
                    reqParams = arrayOf("city", params[1]!!)
                }
            }
        }
        if (response.isEmpty()) {
            response = makeRequest(output, response, reqParams)
        }
        if (TaskResults.SUCCESS == output.taskResult) {
            // Parse JSON data
            val parseResult: ParseResults = parseResponse(response)
            if (ParseResults.CITY_NOT_FOUND == parseResult) {
                // Retain previously specified city if current one was not recognized
                restorePreviousCity()
            }
            output.parseResult = parseResult
        }
        return output
    }

    private fun makeRequest(
        output: TaskOutput,
        responseString: String,
        reqParams: Array<String>
    ): String {
        var response = responseString
        try {
            val url = provideURL(reqParams)
            Log.i("URL", url.toString())
            val urlConnection =
                url.openConnection() as HttpURLConnection
            if (urlConnection is HttpsURLConnection) {
                try {
                    certificateCountDownLatch.await()
                    if (sslContext != null) {
                        val socketFactory =
                            sslContext.socketFactory
                        urlConnection.sslSocketFactory = socketFactory
                    }
                    certificateCountDownLatch.countDown()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            when (urlConnection.responseCode) {
                200 -> {
                    val inputStreamReader =
                        InputStreamReader(urlConnection.inputStream)
                    val r = BufferedReader(inputStreamReader)
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (r.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                        stringBuilder.append("\n")
                    }
                    response += stringBuilder.toString()
                    close(r)
                    urlConnection.disconnect()
                    // Background work finished successfully
                    Log.i("Task", "done successfully")
                    output.taskResult = TaskResults.SUCCESS
                    // Save date/time for latest successful result
                    val now = Calendar.getInstance()
                    val lastUpdate = now.timeInMillis
                    sharedRepository.setLastUpdateTime(lastUpdate)
                }
                401 -> {
                    // Invalid API key
                    Log.w("Task", "invalid API key")
                    output.taskResult = TaskResults.INVALID_API_KEY
                }
                429 -> {
                    // Too many requests
                    Log.w("Task", "too many requests")
                    output.taskResult = TaskResults.TOO_MANY_REQUESTS
                }
                else -> {
                    // Bad response from server
                    Log.w("Task", "http error " + urlConnection.responseCode)
                    output.taskResult = TaskResults.HTTP_ERROR
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Exception while reading data from url connection
            output.taskResult = TaskResults.IO_EXCEPTION
            output.taskError = e
        }
        return response
    }

    override fun onPostExecute(output: TaskOutput) {
        if (loading == 1) {
            activity.hideProgressBar()
        }
        decLoadingCounter()
        updateMainUI()
        handleTaskOutput(output)
    }

    fun handleTaskOutput(output: TaskOutput) {
        when (output.taskResult) {
            TaskResults.SUCCESS -> {
                val parseResult: ParseResults = output.parseResult!!
                if (ParseResults.CITY_NOT_FOUND == parseResult) {
                    Snackbar.make(
                        activity.findViewById(android.R.id.content),
                        context.getString(R.string.msg_city_not_found),
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if (ParseResults.JSON_EXCEPTION == parseResult) {
                    Snackbar.make(
                        activity.findViewById(android.R.id.content),
                        context.getString(R.string.msg_err_parsing_json),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            TaskResults.TOO_MANY_REQUESTS -> Snackbar.make(
                activity.findViewById(android.R.id.content),
                context.getString(R.string.msg_too_many_requests),
                Snackbar.LENGTH_LONG
            ).show()
            TaskResults.INVALID_API_KEY -> Snackbar.make(
                activity.findViewById(android.R.id.content),
                context.getString(R.string.msg_invalid_api_key),
                Snackbar.LENGTH_LONG
            ).show()
            TaskResults.HTTP_ERROR -> Snackbar.make(
                activity.findViewById(android.R.id.content),
                context.getString(R.string.msg_http_error),
                Snackbar.LENGTH_LONG
            ).show()
            TaskResults.IO_EXCEPTION -> Snackbar.make(
                activity.findViewById(android.R.id.content),
                context.getString(R.string.msg_connection_not_available),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    @Throws(UnsupportedEncodingException::class, MalformedURLException::class)
    private fun provideURL(reqParams: Array<String>): URL {

        val urlBuilder =
            StringBuilder("https://api.openweathermap.org/data/2.5/")
        urlBuilder.append(aPIName).append("?")
        if (reqParams.isNotEmpty()) {
            val zeroParam = reqParams[0]
            if ("coords" == zeroParam) {
                urlBuilder.append("lat=").append(reqParams[1]).append("&lon=")
                    .append(reqParams[2])
            } else if ("city" == zeroParam) {
                urlBuilder.append("q=").append(reqParams[1])
            }
        } else {
            val cityId = sharedRepository.getCityID()
            urlBuilder.append("id=").append(URLEncoder.encode(cityId, "UTF-8"))
        }
        urlBuilder.append("&mode=json")
        urlBuilder.append("&appid=").append(Constants.API_KEY)
        return URL(urlBuilder.toString())
    }

    private fun restorePreviousCity() {
        if (!TextUtils.isEmpty(activity.recentCityId)) {
            sharedRepository.setCityID(activity.recentCityId!!)
            activity.recentCityId = ""
        }
    }

    private fun incLoadingCounter() {
        loading++
    }

    private fun decLoadingCounter() {
        loading--
    }

    open fun updateMainUI() {}
    protected abstract fun parseResponse(response: String?): ParseResults
    protected abstract val aPIName: String?

    companion object {
        private val certificateCountDownLatch =
            CountDownLatch(0)
        private val sslContext: SSLContext? = null
        private fun close(x: Closeable?) {
            try {
                x?.close()
            } catch (e: IOException) {
                Log.e("IOException Data", "Error occurred while closing stream")
            }
        }
    }

}
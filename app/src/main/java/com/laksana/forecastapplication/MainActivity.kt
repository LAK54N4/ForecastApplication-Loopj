package com.laksana.forecastapplication

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.laksana.forecastapplication.databinding.ActivityMainBinding
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        val actionBar: ActionBar = supportActionBar!!
        actionBar.title = "Forecast"

        mainBinding.cardView.visibility = View.GONE
        showLoading(false)

        mainBinding.btnSearch.setOnClickListener {
            val apiKey = BuildConfig.API_KEY
            val location = mainBinding.editLocation.text.toString()
            searchLocation(location, apiKey)
            showLoading(true)
        }
    }

    private fun searchLocation(location: String, apiKey: String) {
        mainBinding.progressBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        val url = "http://api.openweathermap.org/data/2.5/weather?q=$location&appid=$apiKey"
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray
            ) {
                mainBinding.progressBar.visibility = View.INVISIBLE
                mainBinding.cardView.visibility =View.VISIBLE

                val result = String(responseBody)
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)
                    val name = responseObject.getString("name")
                    val timezone = responseObject.getString("timezone")

                    val responseMain = responseObject.getJSONObject("main")
                    val temp = responseMain.getString("temp")
                    val tempMin = responseMain.getString("temp_min")
                    val tempMax = responseMain.getString("temp_max")

                    val jsonArray = responseObject.getJSONArray("weather")
                    val jsonObject = jsonArray.getJSONObject(0)
                    val iconCode = jsonObject.getString("icon")

                    val temperature = convertTemp(temp.toFloat())
                    val tempMinimum = convertTemp(tempMin.toFloat())
                    val tempMaximum = convertTemp(tempMax.toFloat())

                    val iconUrl = "http://openweathermap.org/img/w/$iconCode.png"

                    mainBinding.tvNameCity.text = name
                    mainBinding.tvTimezone.text = getDateTime(timezone)
                    mainBinding.tvTemperature.text = String.format("$temperature °C")
                    mainBinding.tvTempMin.text = String.format("$tempMinimum \u2103")
                    mainBinding.tvTempMax.text = String.format("$tempMaximum \u2103")

                    Glide.with(this@MainActivity).load(iconUrl)
                        .placeholder(R.drawable.img_cloud)
                        .error(R.drawable.logo)
                        .into(mainBinding.icon)

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable
            ) {
                //koneksi gagal
                mainBinding.progressBar.visibility = View.INVISIBLE

                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun convertTemp(temp: Float): String {
        val temperature = temp - 273.15
        return String.format("%.2f", temperature)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(timezone: String): String {
        try {
            val sdf = SimpleDateFormat("dd/mm/yyyy HH:mm:ss")
            val netDate = Date(timezone.toLong()*1000+System.currentTimeMillis())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    private fun showLoading(state: Boolean) {
        if(state) {
            mainBinding.progressBar.visibility = View.VISIBLE
        }
        else {
            mainBinding.progressBar.visibility = View.INVISIBLE
        }
    }
}
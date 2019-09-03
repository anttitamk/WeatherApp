package com.example.weatherapp

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import android.text.Html
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private var city = "TAMPERE, FI"
    /* Please Put your API KEY here */
    internal var OPEN_WEATHER_MAP_API = "1cd9b65e4fa1cbc737b84b34fdee088d"
    /* Please Put your API KEY here */


    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        taskLoadUp(city)

        selectCity.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle("Select City")
            val input = EditText(this@MainActivity)
            input.setText(city)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            alertDialog.setView(input)

            alertDialog.setPositiveButton("Change"
            ) { _, _ ->
                city = input.text.toString()
                taskLoadUp(city)
            }
            alertDialog.setNegativeButton("Cancel"
            ) { dialog, _ -> dialog.cancel() }
            alertDialog.show()
        }

        refreshButton.setOnClickListener {
            taskLoadUp(city)
        }
    }


    private fun taskLoadUp(query:String) {
        if (Function.isNetworkAvailable(applicationContext))
        {
            val task = DownloadWeather()
            task.execute(query)
        }
        else
        {
            Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_LONG).show()
        }
    }



    internal inner class DownloadWeather:AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg args:String): String {
            return Function.executeGet(("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] + "&units=metric&appid=" + OPEN_WEATHER_MAP_API)).toString()
        }

        override fun onPostExecute(xml: String) {

            var cityField = city_field as TextView
            var updatedField = updated_field as TextView
            var detailsField = details_field as TextView
            var currentTemperatureField = current_temperature_field as TextView
            var weatherIcon = weather_icon as TextView
            var weatherFont = Typeface.createFromAsset(assets, "fonts/weathericons-regular-webfont.ttf")
            weatherIcon.typeface = weatherFont

            try
            {
                var json = JSONObject(xml)
                if (json != null) {
                    val details = json.getJSONArray("weather").getJSONObject(0)
                    val main = json.getJSONObject("main")
                    var temperature = main.getDouble("temp")

                    val current = ZonedDateTime.now(ZoneId.of("Europe/Helsinki"))
                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm")
                    val formatted = current.format(formatter)

                    cityField.text = json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country")
                    detailsField.text = details.getString("description").toUpperCase(Locale.US)
                    currentTemperatureField.text = String.format("%.0f", temperature) + "°C"
                    humidity_field.text = "Humidity: " + main.getString("humidity") + "%"
                    pressure_field.text = "Pressure: " + main.getString("pressure") + " hPa"
                    updatedField.text = "Last updated: $formatted"
                    weatherIcon.text = Html.fromHtml(Function.setWeatherIcon(
                        details.getInt("id"),
                        json.getJSONObject("sys").getLong("sunrise") * 1000,
                        json.getJSONObject("sys").getLong("sunset") * 1000
                    ))

                    loader.visibility = View.GONE

                    var weathertype = details.getString("main")

                    when (weathertype) {
                        "Clear" -> screen.setBackgroundResource(R.drawable.clear_sky)
                        "Clouds" -> screen.setBackgroundResource(R.drawable.few_clouds)
                        "Scattered clouds" -> screen.setBackgroundResource(R.drawable.scattered_clouds)
                        "Broken clouds" -> screen.setBackgroundResource(R.drawable.broken_clouds)
                        "Shower rain" -> screen.setBackgroundResource(R.drawable.shower_rain)
                        "Rain" -> screen.setBackgroundResource(R.drawable.rain)
                        "Thunderstorm" -> screen.setBackgroundResource(R.drawable.thunderstorm)
                        "Snow" -> screen.setBackgroundResource(R.drawable.snow)
                        "Mist" -> screen.setBackgroundResource(R.drawable.mist)
                        "Fog" -> screen.setBackgroundResource(R.drawable.mist)
                    }

                    if (temperature < 0 && weathertype == "Clear") {screen.setBackgroundResource(R.drawable.clear_winter)}
                    if (temperature < -8 && weathertype == "Clear") {screen.setBackgroundResource(R.drawable.clear_winter_cold)}
                }
            }
            catch (e:JSONException) {
                Toast.makeText(applicationContext, "Weather data for $city is unavailable", Toast.LENGTH_LONG).show()
                loader.visibility = View.GONE
            }
        }
    }
}
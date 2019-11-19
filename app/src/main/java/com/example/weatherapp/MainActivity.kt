package com.example.weatherapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.text.Html
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private var city = "TAMPERE"
    /* Please Put your API KEY here */
    internal var OPEN_WEATHER_MAP_API = "1cd9b65e4fa1cbc737b84b34fdee088d"
    private val cityAndCountryRows = FavouriteCities.getCities()


    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // check whether default city or a favourite city should be used
        val favouriteCity : String? = intent.getStringExtra("city")
        if (favouriteCity != null) city = favouriteCity

        // set favourite checkbox value
        setFavouriteCheckboxValue(city)


        taskLoadUp(city)


        // BUTTONS

        // select city
        selectCity.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle("Select City")
            val input = EditText(this@MainActivity)
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

        // refresh
        refreshButton.setOnClickListener {
            taskLoadUp(city)
        }

        // favourite cities view
        cityList.setOnClickListener {
            if (cityAndCountryRows.count() != 0)
            {
                val myIntent = Intent(baseContext, FavouriteCities::class.java)
                startActivity(myIntent)
            }
            else
            {
                Toast.makeText(applicationContext, "There are no cities in favourites!", Toast.LENGTH_LONG).show()
            }
        }

        // adds city to favourites
        add_to_favourites.setOnClickListener {

            val cityAndCountryTextView = findViewById<TextView>(R.id.city_field)
            val cityAndCountryString: String = cityAndCountryTextView.text.toString()
            val cityAndCountryCode = cityAndCountryString.split(",")
            val cityToFav = cityAndCountryCode[0]
            val countryToFav = cityAndCountryCode[1].drop(1)

            val cityCount = FavouriteCities.getCities().count()
            setFavouriteCheckboxValue(cityToFav)

            // add to favourites
            if (!add_to_favourites.isChecked || cityCount == 0)
            {
                if (!cityAndCountryRows.any { row -> row.cityName == cityToFav})
                {
                    FavouriteCities.addToFavourites(cityToFav, countryToFav)
                    setFavouriteCheckboxValue(cityToFav)
                    Toast.makeText(applicationContext, "$cityToFav has been added to favourites!", Toast.LENGTH_LONG).show()
                }
                else Toast.makeText(applicationContext, "$cityToFav has already been added to favourites!", Toast.LENGTH_LONG).show()
            }
            // delete from favourites
            else
            {
                FavouriteCities.deleteFromFavourites(cityToFav)
                Toast.makeText(applicationContext, "$cityToFav has been deleted from favourites!", Toast.LENGTH_LONG).show()
                setFavouriteCheckboxValue(cityToFav)
            }
        }
    }

    internal inner class DownloadWeather:AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg args:String): String {
            return Function.executeGet(("""http://api.openweathermap.org/data/2.5/forecast?q=${args[0]}&units=metric&appid=$OPEN_WEATHER_MAP_API""")).toString()
        }

        override fun onPostExecute(xml: String) {

            val cityField = city_field as TextView
            val updatedField = updated_field as TextView
            val detailsField = details_field as TextView
            val currentTemperatureField = current_temperature_field as TextView
            val weatherIcon = weather_icon as TextView
            val weatherIconFont = Typeface.createFromAsset(assets, "fonts/weathericons-regular-webfont.ttf")
            weatherIcon.typeface = weatherIconFont

            try
            {
                val json = JSONObject(xml)
                val data = json.getJSONArray("list")
                val mainViewData = data.getJSONObject(0)
                val details = mainViewData.getJSONArray("weather").getJSONObject(0)
                val main = mainViewData.getJSONObject("main")
                val temperature = main.getDouble("temp")

                val current = ZonedDateTime.now(ZoneId.of("Europe/Helsinki"))
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm")
                val formatted = current.format(formatter)

                val cityInfo = json.getJSONObject("city")

                cityField.text = cityInfo.getString("name").toUpperCase(Locale.US) + ", " + cityInfo.getString("country")
                detailsField.text = details.getString("description").toUpperCase(Locale.US)
                currentTemperatureField.text = String.format("%.0f", temperature)
                humidity_field.text = "Humidity: " + main.getString("humidity") + "%"
                pressure_field.text = "Pressure: " + main.getString("pressure") + " hPa"
                updatedField.text = "$formatted"
                weatherIcon.text = Html.fromHtml(Function.setWeatherIcon(
                    details.getInt("id"),
                    cityInfo.getLong("sunrise") * 1000,
                    cityInfo.getLong("sunset") * 1000
                ))

                // pass JSON data to populate the slider
                populateSlider(json, weatherIconFont)

                loader.visibility = View.GONE
            }
            catch (e:JSONException) {
                Toast.makeText(applicationContext, "Weather data for $city is unavailable", Toast.LENGTH_LONG).show()
                loader.visibility = View.GONE
            }
        }
    }

    private fun taskLoadUp(query:String) {
        if (Function.isNetworkAvailable(applicationContext))
        {
            val task = DownloadWeather()
            task.execute(query)
            setFavouriteCheckboxValue(query)
        }
        else
        {
            Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun setFavouriteCheckboxValue (city : String)
    {
        add_to_favourites.isChecked = FavouriteCities.getCities().any { row -> row.cityName == city.toUpperCase()}
    }

    // parses JSON and populates an ArrayList of ForecastItems
    private fun populateSlider(json: JSONObject, weatherIconFont: Typeface) {

        val forecast : ArrayList<ForecastItem> = ArrayList()

        val data = json.getJSONArray("list")

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        for (i in 0..39)
        {
            val forecastJson = data.getJSONObject(i)

            val dateTimeString = forecastJson.getString("dt_txt")
            val formattedDT = LocalDateTime.parse(dateTimeString, dateTimeFormatter)

            val cityInfo = json.getJSONObject("city")

            val forecastItem = ForecastItem(
                dayOfWeek = formattedDT.dayOfWeek.toString().take(3),
                forecastIcon = Html.fromHtml(Function.setWeatherIcon(
                    forecastJson.getJSONArray("weather").getJSONObject(0).getInt("id"),
                    cityInfo.getLong("sunrise") * 1000,
                    cityInfo.getLong("sunset") * 1000
                )).toString(),
                forecastTemp = forecastJson.getJSONObject("main").getDouble("temp"),
                time = formattedDT.toLocalTime().toString()
            )

            forecast.add(forecastItem)
        }

        // populate the slider with parsed JSON data
        recyclerView.layoutManager = LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false)
        recyclerView.adapter = ForecastAdapter(forecast, weatherIconFont)
    }
}
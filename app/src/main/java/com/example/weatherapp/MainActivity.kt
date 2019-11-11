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
import android.content.Intent




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
            return Function.executeGet(("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] + "&units=metric&appid=" + OPEN_WEATHER_MAP_API)).toString()
        }

        override fun onPostExecute(xml: String) {

            val cityField = city_field as TextView
            val updatedField = updated_field as TextView
            val detailsField = details_field as TextView
            val currentTemperatureField = current_temperature_field as TextView
            val weatherIcon = weather_icon as TextView
            val weatherFont = Typeface.createFromAsset(assets, "fonts/weathericons-regular-webfont.ttf")
            weatherIcon.typeface = weatherFont

            try
            {
                val json = JSONObject(xml)
                if (json != null) {
                    val details = json.getJSONArray("weather").getJSONObject(0)
                    val main = json.getJSONObject("main")
                    val temperature = main.getDouble("temp")

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

                    val weatherType = details.getString("main")

                    when (weatherType) {
                        "Clear" -> screen.setBackgroundResource(R.drawable.clear_sky)
                        "Clouds" -> screen.setBackgroundResource(R.drawable.few_clouds)
                        "Scattered clouds" -> screen.setBackgroundResource(R.drawable.scattered_clouds)
                        "Broken clouds" -> screen.setBackgroundResource(R.drawable.broken_clouds)
                        "Shower rain" -> screen.setBackgroundResource(R.drawable.shower_rain)
                        "Rain"  -> screen.setBackgroundResource(R.drawable.rain)
                        "Drizzle"  -> screen.setBackgroundResource(R.drawable.rain)
                        "Thunderstorm" -> screen.setBackgroundResource(R.drawable.thunderstorm)
                        "Snow" -> screen.setBackgroundResource(R.drawable.snow)
                        "Mist" -> screen.setBackgroundResource(R.drawable.mist)
                        "Fog" -> screen.setBackgroundResource(R.drawable.mist)
                    }

                    if (temperature < 0 && weatherType == "Clear") {screen.setBackgroundResource(R.drawable.clear_winter)}
                    if (temperature < -8 && weatherType == "Clear") {screen.setBackgroundResource(R.drawable.clear_winter_cold)}
                }
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
}
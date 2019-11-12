package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.favourite_cities.*
import kotlinx.android.synthetic.main.favourite_cities_row.*
import kotlin.collections.ArrayList

class FavouriteCities : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favourite_cities)
        supportActionBar?.hide()

        val cityAndCountryRows = getCities()

        // renders the list
        var listAdapter = ListAdapter(this, cityAndCountryRows)

        if (cityAndCountryRows.count() != 0)
        {
            val listView = findViewById<ListView>(R.id.list_view)

            listView.adapter = ListAdapter(this, cityAndCountryRows)
        }
        else Toast.makeText(applicationContext, "No cities have been added to favourites yet!", Toast.LENGTH_LONG).show()


        // handles list item click
        list_view.setOnItemClickListener { _, _, position, _ ->
            val element = listAdapter.getCity(position) // The item that was clicked
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("city", element)
            startActivity(intent)
        }
    }

    companion object Cities {

        var cityAndCountryRows = ArrayList<RowItem>()

        // adds city to favourites
        fun addToFavourites(city : String, country : String) : ArrayList<RowItem> {

            if (!cityAndCountryRows.contains(RowItem(city, country)))
            {
                cityAndCountryRows.add(RowItem(city, country))
            }

            return cityAndCountryRows
        }

        // deletes city from favourites
        fun deleteFromFavourites(city : String)
        {
            cityAndCountryRows.removeIf{ row -> row.cityName == city}
        }

        // returns list of current favourite cities
        fun getCities(): ArrayList<RowItem> {
            return cityAndCountryRows
        }
    }
}
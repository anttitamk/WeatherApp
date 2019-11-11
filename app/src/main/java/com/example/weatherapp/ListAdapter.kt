package com.example.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class ListAdapter(context : Context, rows : ArrayList<RowItem>) : BaseAdapter() {

    private val mContext : Context = context
    private val mRows : ArrayList<RowItem> = rows

    // gets the count of rows in the list
    override fun getCount(): Int {
        return mRows.count()
    }

    override fun getItem(position: Int): Any {
        return mRows[position]
    }

    override fun getItemId(position: Int): Long {
        return 1
    }

    fun getCity(position: Int): String {
        return mRows[position].cityName
    }

    // renders out each row
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {

        val layoutInflater = LayoutInflater.from(mContext)
        val row = layoutInflater.inflate(R.layout.favourite_cities_row, viewGroup, false)

        val countryName = row.findViewById<TextView>(R.id.country_name)
        countryName.text = mRows[position].countryName

        val cityName = row.findViewById<TextView>(R.id.city_name)
        cityName.text = mRows[position].cityName

        return row
    }
}
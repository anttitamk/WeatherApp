package com.example.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class CustomAdapter : BaseAdapter() {

    var singleRow : ArrayList<RowItem>? = null
    var thisInflater : LayoutInflater? = null


    fun CustomAdapter (context : Context, aRow : ArrayList<RowItem>) {
        singleRow = aRow
        thisInflater = (LayoutInflater.from(context))
    }


    override fun getCount(): Int {
        return singleRow!!.size
    }

    override fun getItem(position: Int): Any {
        return singleRow!!.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View, parent: ViewGroup): View {
        if (view == null) {
            view == thisInflater!!.inflate(R.layout.favourite_cities_row, parent, false)



        }

        return view
    }

}
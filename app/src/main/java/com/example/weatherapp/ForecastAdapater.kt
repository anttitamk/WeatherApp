package com.example.weatherapp

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

// 5-day weather forecast slider
class ForecastAdapter(private val forecast: ArrayList<ForecastItem>, private val weatherIconFont: Typeface) : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.forecast, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return forecast.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.dayOfWeek.text = forecast[position].dayOfWeek
        holder.forecastIcon.typeface = weatherIconFont
        holder.forecastIcon.text = forecast[position].forecastIcon
        holder.forecastTemp.text = String.format("%.0f", forecast[position].forecastTemp)
        holder.time.text = forecast[position].time
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek : TextView = itemView.findViewById(R.id.dayOfWeek)
        val forecastIcon : TextView = itemView.findViewById(R.id.forecastIcon)
        val forecastTemp : TextView = itemView.findViewById(R.id.forecastTemp)
        val time : TextView = itemView.findViewById(R.id.time)
    }
}
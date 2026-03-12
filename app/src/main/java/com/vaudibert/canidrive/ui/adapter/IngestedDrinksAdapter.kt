package com.vaudibert.canidrive.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.data.IngestedDrinkEntity
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*

import com.vaudibert.canidrive.domain.drink.IngestionService

class IngestedDrinksAdapter(
    val context: Context,
    private val ingestionService: IngestionService<com.vaudibert.canidrive.data.PresetDrinkEntity, IngestedDrinkEntity>
) : RecyclerView.Adapter<IngestedDrinksAdapter.DrinkViewHolder>() {

    private var ingestedDrinkList: List<IngestedDrinkEntity> = emptyList()

    private val DAY_IN_MILLIS = 3600L * 1000 * 24
    private val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
    private val doubleFormat = java.text.NumberFormat.getInstance().apply {
        maximumFractionDigits = 1
    }

    inner class DrinkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val propertiesText: TextView = view.findViewById(R.id.textViewPresetDrinkProperties)
        val descriptionText: TextView = view.findViewById(R.id.textViewPresetDrinkDescription)
        val glassImage: ImageView = view.findViewById(R.id.imageViewPresetDrinkIcon)
        val deleteButton: ImageButton = view.findViewById(R.id.buttonRemovePastDrink)
        val timeText: TextView = view.findViewById(R.id.textViewPastDrinkTime)
        val daysText: TextView = view.findViewById(R.id.textViewPastDays)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_past_drink, parent, false)
        return DrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        val drink = ingestedDrinkList[position]

        holder.propertiesText.text = "${doubleFormat.format(drink.volume)} ml - ${drink.degree} %"
        holder.descriptionText.text = drink.name
        holder.glassImage.setImageResource(R.drawable.wine_glass)

        val days: Long = (drink.ingestionTime.time / DAY_IN_MILLIS) - (Date().time / DAY_IN_MILLIS)
        if (days == 0L)
            holder.daysText.visibility = View.GONE
        else {
            holder.daysText.visibility = View.VISIBLE
            holder.daysText.text = "$days${context.getString(R.string.day_unit)} "
        }

        holder.timeText.text = dateFormat.format(drink.ingestionTime)

        holder.deleteButton.setOnClickListener {
            ingestionService.remove(drink)
            com.google.android.material.snackbar.Snackbar.make(
                holder.itemView,
                R.string.snackbar_drink_deleted,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).setAction(R.string.snackbar_undo) {
                ingestionService.add(drink)
            }.show()
        }
    }

    override fun getItemCount(): Int {
        return ingestedDrinkList.size
    }

    fun setDrinkList(ingestedDrinks: List<IngestedDrinkEntity>) {
        ingestedDrinkList = ingestedDrinks
        notifyDataSetChanged()
    }
}
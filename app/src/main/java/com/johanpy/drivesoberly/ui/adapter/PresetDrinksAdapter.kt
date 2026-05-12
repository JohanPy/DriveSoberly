package com.johanpy.drivesoberly.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.johanpy.drivesoberly.R
import com.johanpy.drivesoberly.data.PresetDrinkEntity
import com.johanpy.drivesoberly.data.repository.DrinkRepository
import java.text.NumberFormat

class PresetDrinksAdapter(
    val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val goToAddPreset: () -> Unit,
    private val drinkRepository: DrinkRepository,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val presetService = drinkRepository.presetService
    private val doubleFormat =
        java.text.NumberFormat.getInstance().apply {
            maximumFractionDigits = 1
        }

    private var presetDrinks: List<PresetDrinkEntity> = emptyList()
    private var visiblePresetDrinks: List<PresetDrinkEntity> = emptyList()
    private var selectedPreset: PresetDrinkEntity? = null
    private var hideBuiltInPresets: Boolean = false

    companion object {
        const val TYPE_ADD_PRESET = 0
        const val TYPE_PRESET_ITEM = 1
    }

    init {
        drinkRepository.livePresetDrinks.observe(
            lifecycleOwner,
            Observer {
                presetDrinks = it
                refreshVisiblePresets()
            },
        )
        drinkRepository.liveSelectedPreset.observe(
            lifecycleOwner,
            Observer {
                selectedPreset = it
                notifyDataSetChanged()
            },
        )
    }

    fun setHideBuiltInPresets(hide: Boolean) {
        hideBuiltInPresets = hide
        refreshVisiblePresets()
    }

    private fun refreshVisiblePresets() {
        visiblePresetDrinks =
            if (hideBuiltInPresets) {
                presetDrinks.filter { !it.isBuiltIn }
            } else {
                presetDrinks
            }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD_PRESET else TYPE_PRESET_ITEM
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        if (viewType == TYPE_ADD_PRESET) {
            val view = inflater.inflate(R.layout.item_add_preset, parent, false)
            return AddPresetViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_preset_drink, parent, false)
            return PresetViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is AddPresetViewHolder) {
            holder.addDescriptionText.text = context.getString(R.string.add_preset_description)
            holder.itemView.setOnClickListener {
                presetService.selectedPreset = null
                goToAddPreset()
            }
        } else if (holder is PresetViewHolder) {
            val presetDrink = visiblePresetDrinks[position - 1]

            holder.propertiesText.text = "${doubleFormat.format(presetDrink.volume)} ml - ${presetDrink.degree} %"
            holder.descriptionText.text = presetDrink.name
            holder.emojiText.text = presetDrink.emoji

            updatePresetColor(presetDrink, holder.itemView, holder.deleteButton, selectedPreset)

            val clickListener = { _: View ->
                presetService.selectedPreset =
                    if (presetDrink == drinkRepository.liveSelectedPreset.value) {
                        null
                    } else {
                        presetDrink
                    }
            }
            val longClickListener =
                View.OnLongClickListener {
                    presetService.selectedPreset = presetDrink
                    goToAddPreset()
                    true
                }

            holder.propertiesText.setOnClickListener(clickListener)
            holder.propertiesText.setOnLongClickListener(longClickListener)

            holder.descriptionText.setOnClickListener(clickListener)
            holder.descriptionText.setOnLongClickListener(longClickListener)

            holder.emojiText.setOnClickListener(clickListener)
            holder.emojiText.setOnLongClickListener(longClickListener)

            holder.deleteButton.setOnClickListener {
                if (presetDrink != drinkRepository.liveSelectedPreset.value) return@setOnClickListener
                presetService.removePreset(presetDrink)
                com.google.android.material.snackbar.Snackbar.make(
                    holder.itemView,
                    R.string.snackbar_drink_deleted,
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG,
                ).setAction(R.string.snackbar_undo) {
                    presetService.addPreset(presetDrink)
                }.show()
            }
        }
    }

    override fun getItemCount(): Int = visiblePresetDrinks.size + 1

    private fun updatePresetColor(
        drink: PresetDrinkEntity,
        drinkView: View,
        deleteButton: ImageButton,
        selected: PresetDrinkEntity?,
    ) {
        if (selected != null && drink == selected) {
            drinkView.setBackgroundResource(R.drawable.background_color_primary)
            deleteButton.visibility = ImageButton.VISIBLE
        } else {
            drinkView.setBackgroundResource(R.drawable.background_color_none)
            deleteButton.visibility = ImageButton.GONE
        }
    }

    inner class AddPresetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addDescriptionText: TextView = view.findViewById(R.id.textViewAddPresetDescription)
    }

    inner class PresetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val propertiesText: TextView = view.findViewById(R.id.textViewPresetDrinkProperties)
        val descriptionText: TextView = view.findViewById(R.id.textViewPresetDrinkDescription)
        val emojiText: TextView = view.findViewById(R.id.imageViewPresetDrinkIcon)
        val deleteButton: ImageButton = view.findViewById(R.id.buttonRemovePresetDrink)
    }
}

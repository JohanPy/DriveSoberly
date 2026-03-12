package com.vaudibert.canidrive.domain.drink

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class PresetDrinkService<Preset : IPresetDrink>(
    private val presetMaker: (name: String, volume: Double, degree: Double) -> Preset,
) {
    private val _presetRemovedFlow = MutableSharedFlow<Preset>(extraBufferCapacity = 10)
    val presetRemovedFlow: SharedFlow<Preset> = _presetRemovedFlow.asSharedFlow()

    private val _presetAddedFlow = MutableSharedFlow<Preset>(extraBufferCapacity = 10)
    val presetAddedFlow: SharedFlow<Preset> = _presetAddedFlow.asSharedFlow()

    private val _presetUpdatedFlow = MutableSharedFlow<Preset>(extraBufferCapacity = 10)
    val presetUpdatedFlow: SharedFlow<Preset> = _presetUpdatedFlow.asSharedFlow()

    private val _presetsFlow = MutableStateFlow<List<Preset>>(emptyList())
    val presetsFlow: StateFlow<List<Preset>> = _presetsFlow.asStateFlow()

    private val _selectedPresetFlow = MutableStateFlow<Preset?>(null)
    val selectedPresetFlow: StateFlow<Preset?> = _selectedPresetFlow.asStateFlow()

    var ingestionService: IIngestCapable<Preset>? = null

    var selectedPreset: Preset?
        get() = _selectedPresetFlow.value
        set(value) {
            _selectedPresetFlow.value = value
        }

    private var presetDrinks: MutableList<Preset> = mutableListOf()

    fun populate(presets: List<Preset>) {
        presetDrinks.addAll(presets)
        sortAndCallbackPresets()
    }

    fun addNewPreset(
        name: String,
        volume: Double,
        degree: Double,
    ) {
        val newPreset = presetMaker(name, volume, degree)
        presetDrinks.add(newPreset)
        selectedPreset = newPreset
        sortAndCallbackPresets()
    }

    private fun sortAndCallbackPresets() {
        presetDrinks.sortByDescending { it.count }
        _presetsFlow.value = presetDrinks.toList()
    }

    fun removePreset(presetDrink: Preset) {
        presetDrinks.remove(presetDrink)
        if (selectedPreset == presetDrink) selectedPreset = null
        _presetRemovedFlow.tryEmit(presetDrink)
        sortAndCallbackPresets()
    }

    fun addPreset(presetDrink: Preset) {
        presetDrinks.add(presetDrink)
        _presetAddedFlow.tryEmit(presetDrink)
        sortAndCallbackPresets()
    }

    fun updateSelectedPreset(
        name: String,
        volume: Double,
        degree: Double,
    ) {
        val currentSelected = selectedPreset
        if (currentSelected == null) {
            addNewPreset(name, volume, degree)
        } else {
            currentSelected.name = name
            currentSelected.volume = volume
            currentSelected.degree = degree
            selectedPreset = currentSelected
            _presetUpdatedFlow.tryEmit(currentSelected)
            sortAndCallbackPresets()
        }
    }

    fun ingest(ingestionTime: Date) {
        val ingester = ingestionService
        val preset = selectedPreset
        if (ingester != null && preset != null) {
            preset.count++
            _presetUpdatedFlow.tryEmit(preset)
            sortAndCallbackPresets()
            ingester.ingest(preset, ingestionTime)
        }
    }
}

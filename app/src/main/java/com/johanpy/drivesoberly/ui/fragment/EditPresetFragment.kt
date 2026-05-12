package com.johanpy.drivesoberly.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.johanpy.drivesoberly.R
import com.johanpy.drivesoberly.databinding.FragmentAddPresetBinding
import com.johanpy.drivesoberly.domain.drink.IngestedDrink
import com.johanpy.drivesoberly.ui.util.KeyboardUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

class EditPresetFragment : Fragment() {
    private val viewModel: EditPresetViewModel by viewModel()

    private var _binding: FragmentAddPresetBinding? = null
    private val binding get() = _binding!!

    private var volume = 0.0
    private var degree = 0.0
    private var emoji = "🍺"

    private val doubleFormat: DecimalFormat = DecimalFormat("0.#")

    // Views from included layout (linear_content_add_drink_custom_pickers.xml)
    private lateinit var numberPickerVolume: NumberPicker
    private lateinit var numberPickerDegree: NumberPicker
    private lateinit var spinnerPresetEmoji: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddPresetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views from included layouts
        numberPickerVolume = view.findViewById(R.id.numberPickerVolume)
        numberPickerDegree = view.findViewById(R.id.numberPickerDegree)
        spinnerPresetEmoji = view.findViewById(R.id.spinnerPresetEmoji)

        val presetService = viewModel.drinkRepository.presetService
        val selectedPreset = presetService.selectedPreset

        if (selectedPreset != null) {
            volume = selectedPreset.volume
            degree = selectedPreset.degree
            emoji = selectedPreset.emoji
            binding.editTextNewPresetName.setText(selectedPreset.name)
        }

        val emojis = resources.getStringArray(R.array.preset_emoji_choices)
        val emojiAdapter =
            ArrayAdapter(requireContext(), R.layout.spinner_emoji_item, emojis).apply {
                setDropDownViewResource(R.layout.spinner_emoji_dropdown_item)
            }
        spinnerPresetEmoji.adapter = emojiAdapter
        val selectedEmojiIndex = emojis.indexOf(emoji).let { if (it >= 0) it else 0 }
        spinnerPresetEmoji.setSelection(selectedEmojiIndex)

        binding.buttonValidateNewPreset.setOnClickListener {
            if (binding.editTextNewPresetName.text.toString().isBlank()) return@setOnClickListener

            if (selectedPreset != null) {
                presetService.updateSelectedPreset(
                    binding.editTextNewPresetName.text.toString(),
                    volume,
                    degree,
                    emojis[spinnerPresetEmoji.selectedItemPosition],
                )
            } else {
                presetService.addNewPreset(
                    binding.editTextNewPresetName.text.toString(),
                    volume,
                    degree,
                    emojis[spinnerPresetEmoji.selectedItemPosition],
                )
            }

            KeyboardUtils.hideKeyboard(this.activity as Activity)

            findNavController().navigate(
                EditPresetFragmentDirections.actionAddPresetFragmentToAddDrinkFragment(),
            )
        }

        setVolumePicker()

        setDegreePicker()
    }

    private fun setDegreePicker() {
        val degreeLabels =
            IngestedDrink.degrees.map { deg ->
                "${doubleFormat.format(deg)} %"
            }.toTypedArray()
        numberPickerDegree.minValue = 0
        numberPickerDegree.maxValue = degreeLabels.size - 1
        numberPickerDegree.displayedValues = degreeLabels
        val indexOfDegree = IngestedDrink.degrees.toList().indexOf(degree)
        val startDegree =
            if (indexOfDegree < 0) {
                degreeLabels.size / 2
            } else {
                indexOfDegree
            }
        degree = IngestedDrink.degrees[startDegree]
        numberPickerDegree.value = startDegree
        numberPickerDegree.setOnValueChangedListener { _, _, newVal ->
            degree = IngestedDrink.degrees[newVal]
        }
    }

    private fun setVolumePicker() {
        val volumeLabels =
            IngestedDrink.volumes.map { vol ->
                if (vol < 1000.0) {
                    "${doubleFormat.format(vol / 10.0)} cL"
                } else {
                    "${doubleFormat.format(vol / 100.0)} cL"
                }
            }.toTypedArray()
        numberPickerVolume.minValue = 0
        numberPickerVolume.maxValue = volumeLabels.size - 1
        numberPickerVolume.displayedValues = volumeLabels
        val indexOfVolume = IngestedDrink.volumes.toList().indexOf(volume)
        val startVolume =
            if (indexOfVolume < 0) {
                volumeLabels.size / 2
            } else {
                indexOfVolume
            }
        numberPickerVolume.value = startVolume
        volume = IngestedDrink.volumes[startVolume]
        numberPickerVolume.setOnValueChangedListener { _, _, newVal ->
            volume = IngestedDrink.volumes[newVal]
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

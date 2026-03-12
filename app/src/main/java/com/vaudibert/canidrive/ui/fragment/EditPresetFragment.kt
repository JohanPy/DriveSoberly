package com.vaudibert.canidrive.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.databinding.FragmentAddPresetBinding
import com.vaudibert.canidrive.domain.drink.IngestedDrink
import com.vaudibert.canidrive.ui.util.KeyboardUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

class EditPresetFragment : Fragment() {
    private val viewModel: EditPresetViewModel by viewModel()

    private var _binding: FragmentAddPresetBinding? = null
    private val binding get() = _binding!!

    private var volume = 0.0
    private var degree = 0.0

    private val doubleFormat: DecimalFormat = DecimalFormat("0.#")

    // Views from included layout (linear_content_add_drink_custom_pickers.xml)
    private lateinit var numberPickerVolume: NumberPicker
    private lateinit var numberPickerDegree: NumberPicker

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

        val presetService = viewModel.drinkRepository.presetService
        val selectedPreset = presetService.selectedPreset

        if (selectedPreset != null) {
            volume = selectedPreset.volume
            degree = selectedPreset.degree
            binding.editTextNewPresetName.setText(selectedPreset.name)
        }

        binding.buttonValidateNewPreset.setOnClickListener {
            if (binding.editTextNewPresetName.text.toString().isBlank()) return@setOnClickListener

            if (selectedPreset != null) {
                presetService.updateSelectedPreset(
                    binding.editTextNewPresetName.text.toString(),
                    volume,
                    degree,
                )
            } else {
                presetService.addNewPreset(
                    binding.editTextNewPresetName.text.toString(),
                    volume,
                    degree,
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
                    "${doubleFormat.format(vol)} mL"
                } else {
                    "${doubleFormat.format(vol / 1000.0)} L"
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

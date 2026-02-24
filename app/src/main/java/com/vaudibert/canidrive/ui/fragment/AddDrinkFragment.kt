package com.vaudibert.canidrive.ui.fragment


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar
import com.vaudibert.canidrive.ui.util.KeyboardUtils
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.databinding.FragmentAddDrinkBinding
import com.vaudibert.canidrive.ui.CanIDrive
import com.vaudibert.canidrive.ui.adapter.PresetDrinksAdapter
import java.util.Date

/**
 * Fragment to add a drink.
 */
class AddDrinkFragment : Fragment() {

    private var _binding: FragmentAddDrinkBinding? = null
    private val binding get() = _binding!!

    private var delay: Long = 0

    // Views from included layouts
    private lateinit var listViewPresetDrinks: ListView
    private lateinit var textViewWhenText: TextView
    private lateinit var seekBarIngestionDelay: VerticalSeekBar
    private lateinit var buttonValidateNewDrink: com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDrinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views from included layouts
        listViewPresetDrinks = view.findViewById(R.id.listViewPresetDrinks)
        textViewWhenText = view.findViewById(R.id.textViewWhenText)
        seekBarIngestionDelay = view.findViewById(R.id.seekBarIngestionDelay)
        buttonValidateNewDrink = view.findViewById(R.id.buttonValidateNewDrink)

        val drinkRepository = CanIDrive.instance.mainRepository.drinkRepository
        val presetService = drinkRepository.presetService

        setDelaySeekBar()

        val presetDrinksAdapter =
            PresetDrinksAdapter(
                requireContext(),
                viewLifecycleOwner,
                {
                    findNavController().navigate(
                        AddDrinkFragmentDirections.actionAddDrinkFragmentToAddPresetFragment()
                    )
                },
                drinkRepository
            )
        listViewPresetDrinks.adapter = presetDrinksAdapter

        drinkRepository.liveSelectedPreset.observe(viewLifecycleOwner) {
            if (it == null)
                buttonValidateNewDrink.visibility = Button.INVISIBLE
            else
                buttonValidateNewDrink.visibility = Button.VISIBLE
        }

        drinkRepository.livePresetDrinks.observe(viewLifecycleOwner) {
            presetDrinksAdapter.notifyDataSetChanged()
        }

        buttonValidateNewDrink.setOnClickListener {
            val ingestionTime = Date(Date().time - (delay * 60000))
            presetService.ingest(ingestionTime)

            KeyboardUtils.hideKeyboard(this.activity as Activity)
            findNavController().navigate(
                AddDrinkFragmentDirections.actionAddDrinkFragmentToDriveFragment()
            )
        }

    }

    private fun setDelaySeekBar() {
        val delays = longArrayOf(0, 20, 40, 60, 90, 120, 180, 300, 480, 720, 1080, 1440)
        val delayLabels = arrayOf(
            getString(R.string.now),
            "20min",
            "40min",
            "1h",
            "1h30",
            "2h",
            "3h",
            "5h",
            "8h",
            "12h",
            "18h",
            "24h"
        )

        val levelCount = delays.size - 1
        seekBarIngestionDelay.max = levelCount
        textViewWhenText.text = delayLabels[0]

        seekBarIngestionDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewWhenText.text = delayLabels[progress]
                delay = delays[progress]
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        seekBarIngestionDelay.progress = 0
        delay = delays[0]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

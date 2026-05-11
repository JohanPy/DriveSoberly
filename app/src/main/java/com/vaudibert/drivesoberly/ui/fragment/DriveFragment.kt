package com.vaudibert.drivesoberly.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.vaudibert.drivesoberly.R
import com.vaudibert.drivesoberly.data.repository.DigestionRepository
import com.vaudibert.drivesoberly.data.repository.DrinkRepository
import com.vaudibert.drivesoberly.databinding.FragmentDriveStatusBinding
import com.vaudibert.drivesoberly.domain.DrinkerStatusService
import com.vaudibert.drivesoberly.domain.digestion.FoodState
import com.vaudibert.drivesoberly.ui.adapter.IngestedDrinksAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat

/**
 * The drive fragment that displays the drive status :
 *  - can the user drive ?
 *  - if not then when ?
 *  - what were the past drinks ?
 */
class DriveFragment : Fragment() {
    private val viewModel: DriveViewModel by viewModel()

    private var _binding: FragmentDriveStatusBinding? = null
    private val binding get() = _binding!!

    private lateinit var digestionRepository: DigestionRepository
    private lateinit var drinkRepository: DrinkRepository
    private lateinit var drinkerStatusService: DrinkerStatusService

    lateinit var mainHandler: Handler

    private lateinit var ingestedDrinksAdapter: IngestedDrinksAdapter

    // Views from included layout (constraint_content_drive_history.xml)
    private lateinit var textViewPastDrinks: TextView
    private lateinit var listViewPastDrinks: RecyclerView

    // Views from included layout (include_food_state_selector.xml)
    private lateinit var toggleGroupFoodState: MaterialButtonToggleGroup
    private lateinit var textViewFoodStateDesc: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDriveStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views from included layouts
        textViewPastDrinks = view.findViewById(R.id.textViewPastDrinks)
        listViewPastDrinks = view.findViewById(R.id.listViewPastDrinks)
        toggleGroupFoodState = view.findViewById(R.id.toggleGroupFoodState)
        textViewFoodStateDesc = view.findViewById(R.id.textViewFoodStateDesc)

        drinkerStatusService = viewModel.mainRepository.drinkerStatusService
        drinkRepository = viewModel.drinkRepository
        digestionRepository = viewModel.digestionRepository

        ingestedDrinksAdapter = IngestedDrinksAdapter(requireContext(), drinkRepository.ingestionService)
        listViewPastDrinks.adapter = ingestedDrinksAdapter

        // Restore the persisted food state selection.
        updateFoodStateToggle(digestionRepository.body.foodState)

        toggleGroupFoodState.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val state = when (checkedId) {
                R.id.buttonFoodLight -> FoodState.LIGHT_MEAL
                R.id.buttonFoodFull -> FoodState.FULL_MEAL
                else -> FoodState.EMPTY
            }
            viewModel.updateFoodState(state)
            textViewFoodStateDesc.text = getString(foodStateDescRes(state))
            updateDriveStatus()
        }

        drinkRepository.livePastDrinks.observe(viewLifecycleOwner) {
            textViewPastDrinks.visibility = if (it.isEmpty()) TextView.GONE else TextView.VISIBLE
            ingestedDrinksAdapter.setDrinkList(it.asReversed())
            updateDriveStatus()
        }

        // needed for periodic update of drinker status
        mainHandler = Handler(Looper.getMainLooper())

        binding.buttonToDrinker.setOnClickListener {
            findNavController().navigate(
                DriveFragmentDirections.actionDriveFragmentToDrinkerFragment(),
            )
        }

        binding.buttonAddDrink.setOnClickListener {
            findNavController().navigate(
                DriveFragmentDirections.actionDriveFragmentToAddDrinkFragment(),
            )
        }
    }

    private fun updateDriveStatus() {
        val drinkerStatus = drinkerStatusService.status()

        val label = binding.root.findViewById<TextView>(R.id.textViewDriveStatusLabel)
        val numberFormat = java.text.NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

        if (drinkerStatus.peakRate < 0.01) {
            binding.linearAlcoholRate.visibility = LinearLayout.GONE
            binding.linearProjectionSober.visibility = LinearLayout.GONE
            binding.linearProjectedPeak.visibility = LinearLayout.GONE
            binding.linearProjectionDrive.visibility = LinearLayout.GONE

            binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
            binding.imageDriveStatus.setImageResource(R.drawable.ic_check_white_24dp)
            binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))

            label.text = getString(R.string.safe_to_drive)
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveGreen))
        } else {
            binding.linearAlcoholRate.visibility = LinearLayout.VISIBLE

            val currentRateText = "${numberFormat.format(drinkerStatus.alcoholRate)} ${getString(R.string.bac_unit_gl)}"
            binding.textViewAlcoholRate.text = getString(R.string.drive_current_rate, currentRateText)

            binding.linearProjectionSober.visibility = LinearLayout.VISIBLE
            binding.textViewProjectionSober.text =
                getString(
                    R.string.drive_zero_at,
                    timeFormat.format(drinkerStatus.soberDate),
                )

            if (drinkerStatus.peakRate > drinkerStatus.alcoholRate + 0.01) {
                binding.linearProjectedPeak.visibility = LinearLayout.VISIBLE
                binding.textViewPeakRate.text =
                    getString(
                        R.string.drive_peak_rate,
                        "${numberFormat.format(drinkerStatus.peakRate)} ${getString(R.string.bac_unit_gl)}",
                        timeFormat.format(drinkerStatus.peakDate),
                    )
            } else {
                binding.linearProjectedPeak.visibility = LinearLayout.GONE
            }

            if (!drinkerStatus.exceedsLimitInProjection) {
                // Set status icons to drive-able
                binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
                binding.imageDriveStatus.setImageResource(R.drawable.ic_warning_white_24dp)
                binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveAmber))
                binding.textViewAlcoholRate.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveAmber))
                binding.linearProjectionDrive.visibility = LinearLayout.GONE

                label.text = getString(R.string.safe_to_drive)
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveAmber))
            } else {
                binding.linearProjectionDrive.visibility = LinearLayout.VISIBLE
                binding.textViewProjectionDrive.text =
                    getString(
                        R.string.drive_below_limit_at,
                        timeFormat.format(drinkerStatus.canDriveDate),
                    )

                // Set status icons to NOT drive-able
                binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveRed))
                binding.imageDriveStatus.setImageResource(R.drawable.ic_forbidden_white_24dp)
                binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveRed))
                binding.textViewAlcoholRate.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveRed))

                label.text = getString(R.string.do_not_drive)
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveRed))
            }
        }
        ingestedDrinksAdapter.notifyDataSetChanged()
    }

    /**
     * Helper task to update the drive status while app is running.
     */
    private val updateDriveStatusTask =
        object : Runnable {
            override fun run() {
                updateDriveStatus()
                mainHandler.postDelayed(this, 1000 * 60)
            }
        }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateDriveStatusTask)
    }

    override fun onResume() {
        super.onResume()
        updateDriveStatus()
        mainHandler.post(updateDriveStatusTask)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Food state helpers ────────────────────────────────────────────────────

    private fun updateFoodStateToggle(state: FoodState) {
        val buttonId = when (state) {
            FoodState.LIGHT_MEAL -> R.id.buttonFoodLight
            FoodState.FULL_MEAL -> R.id.buttonFoodFull
            FoodState.EMPTY -> R.id.buttonFoodEmpty
        }
        toggleGroupFoodState.check(buttonId)
        textViewFoodStateDesc.text = getString(foodStateDescRes(state))
    }

    private fun foodStateDescRes(state: FoodState): Int =
        when (state) {
            FoodState.EMPTY -> R.string.food_state_empty_desc
            FoodState.LIGHT_MEAL -> R.string.food_state_light_desc
            FoodState.FULL_MEAL -> R.string.food_state_full_desc
        }
}

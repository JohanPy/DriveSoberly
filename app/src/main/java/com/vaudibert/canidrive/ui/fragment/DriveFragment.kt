package com.vaudibert.canidrive.ui.fragment

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
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.data.repository.DigestionRepository
import com.vaudibert.canidrive.data.repository.DrinkRepository
import com.vaudibert.canidrive.databinding.FragmentDriveStatusBinding
import com.vaudibert.canidrive.domain.DrinkerStatusService
import com.vaudibert.canidrive.ui.adapter.IngestedDrinksAdapter
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

        drinkerStatusService = viewModel.mainRepository.drinkerStatusService
        drinkRepository = viewModel.drinkRepository
        digestionRepository = viewModel.digestionRepository

        ingestedDrinksAdapter = IngestedDrinksAdapter(requireContext(), drinkRepository.ingestionService)
        listViewPastDrinks.adapter = ingestedDrinksAdapter

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

        if (drinkerStatus.alcoholRate < 0.01) {
            binding.linearAlcoholRate.visibility = LinearLayout.GONE
            binding.linearWaitToSober.visibility = LinearLayout.GONE

            binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
            binding.imageDriveStatus.setImageResource(R.drawable.ic_check_white_24dp)
            binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
            binding.linearWaitToDrive.visibility = LinearLayout.GONE

            label.text = getString(R.string.safe_to_drive)
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveGreen))
        } else {
            binding.linearAlcoholRate.visibility = LinearLayout.VISIBLE
            binding.linearWaitToSober.visibility = LinearLayout.VISIBLE

            val numberFormat = java.text.NumberFormat.getInstance()
            numberFormat.maximumFractionDigits = 2
            numberFormat.minimumFractionDigits = 2
            binding.textViewAlcoholRate.text = "${numberFormat.format(drinkerStatus.alcoholRate)} ${getString(R.string.bac_unit_gl)}"

            binding.textViewTimeToSober.text =
                DateFormat
                    .getTimeInstance(DateFormat.SHORT)
                    .format(drinkerStatus.soberDate)

            if (drinkerStatus.canDrive) {
                // Set status icons to drive-able
                binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
                binding.imageDriveStatus.setImageResource(R.drawable.ic_warning_white_24dp)
                binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveAmber))
                binding.linearWaitToDrive.visibility = LinearLayout.GONE
                binding.textViewAlcoholRate.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveAmber))

                label.text = getString(R.string.safe_to_drive)
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveAmber))
            } else {
                binding.textViewTimeToDrive.text =
                    DateFormat
                        .getTimeInstance(DateFormat.SHORT)
                        .format(drinkerStatus.canDriveDate)

                // Set status icons to NOT drive-able
                binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveRed))
                binding.imageDriveStatus.setImageResource(R.drawable.ic_forbidden_white_24dp)
                binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveRed))
                binding.linearWaitToDrive.visibility = LinearLayout.VISIBLE
                binding.textViewAlcoholRate.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveRed))
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
}

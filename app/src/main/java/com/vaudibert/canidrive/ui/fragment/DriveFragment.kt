package com.vaudibert.canidrive.ui.fragment


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.databinding.FragmentDriveStatusBinding
import com.vaudibert.canidrive.domain.DrinkerStatusService
import com.vaudibert.canidrive.ui.CanIDrive
import com.vaudibert.canidrive.ui.adapter.IngestedDrinksAdapter
import com.vaudibert.canidrive.ui.repository.DigestionRepository
import com.vaudibert.canidrive.ui.repository.DrinkRepository
import java.text.DateFormat

/**
 * The drive fragment that displays the drive status :
 *  - can the user drive ?
 *  - if not then when ?
 *  - what were the past drinks ?
 */
class DriveFragment : Fragment() {

    private var _binding: FragmentDriveStatusBinding? = null
    private val binding get() = _binding!!

    private lateinit var digestionRepository: DigestionRepository
    private lateinit var drinkRepository: DrinkRepository
    private lateinit var drinkerStatusService: DrinkerStatusService

    lateinit var mainHandler: Handler

    private lateinit var ingestedDrinksAdapter: IngestedDrinksAdapter

    // Views from included layout (constraint_content_drive_history.xml)
    private lateinit var textViewPastDrinks: TextView
    private lateinit var listViewPastDrinks: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriveStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views from included layouts
        textViewPastDrinks = view.findViewById(R.id.textViewPastDrinks)
        listViewPastDrinks = view.findViewById(R.id.listViewPastDrinks)

        val mainRepository = CanIDrive.instance.mainRepository
        drinkerStatusService = mainRepository.drinkerStatusService
        drinkRepository = mainRepository.drinkRepository
        digestionRepository = mainRepository.digestionRepository

        ingestedDrinksAdapter = IngestedDrinksAdapter(requireContext())
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
                DriveFragmentDirections.actionDriveFragmentToDrinkerFragment()
            )
        }

        binding.buttonAddDrink.setOnClickListener {
            findNavController().navigate(
                DriveFragmentDirections.actionDriveFragmentToAddDrinkFragment()
            )
        }

    }

    private fun updateDriveStatus() {
        val drinkerStatus = drinkerStatusService.status()

        if (drinkerStatus.alcoholRate < 0.01) {
            binding.linearAlcoholRate.visibility = LinearLayout.GONE
            binding.linearWaitToSober.visibility = LinearLayout.GONE

            binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
            binding.imageDriveStatus.setImageResource(R.drawable.ic_check_white_24dp)
            binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
            binding.linearWaitToDrive.visibility = LinearLayout.GONE

        } else {
            binding.linearAlcoholRate.visibility = LinearLayout.VISIBLE
            binding.linearWaitToSober.visibility = LinearLayout.VISIBLE
            binding.textViewAlcoholRate.text =
                String.format("%.2f g/L", drinkerStatus.alcoholRate)

            binding.textViewTimeToSober.text = DateFormat
                .getTimeInstance(DateFormat.SHORT)
                .format(drinkerStatus.soberDate)

            if (drinkerStatus.canDrive) {
                // Set status icons to drive-able
                binding.imageCar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveGreen))
                binding.imageDriveStatus.setImageResource(R.drawable.ic_warning_white_24dp)
                binding.imageDriveStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.driveAmber))
                binding.linearWaitToDrive.visibility = LinearLayout.GONE
                binding.textViewAlcoholRate.setTextColor(ContextCompat.getColor(requireContext(), R.color.driveAmber))
            } else {
                binding.textViewTimeToDrive.text = DateFormat
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
    private val updateDriveStatusTask = object : Runnable {
        override fun run() {
            updateDriveStatus()
            mainHandler.postDelayed(this, 1000*60)
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

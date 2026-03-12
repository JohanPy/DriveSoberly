package com.vaudibert.canidrive.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.data.repository.DigestionRepository
import com.vaudibert.canidrive.data.repository.MainRepository
import com.vaudibert.canidrive.databinding.FragmentDrinkerBinding
import com.vaudibert.canidrive.domain.digestion.Sex
import com.vaudibert.canidrive.domain.drivelaw.DriveLaw
import com.vaudibert.canidrive.domain.drivelaw.DriveLawService
import com.vaudibert.canidrive.ui.util.KeyboardUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

/**
 * The drinker fragment to enter its details.
 */
// TODO : split into 2 fragments : body and drive law ?
class DrinkerFragment : Fragment() {
    private val viewModel: DrinkerViewModel by viewModel()

    private var _binding: FragmentDrinkerBinding? = null
    private val binding get() = _binding!!

    private var weight = 0.0
    private var sex = Sex.OTHER

    private lateinit var mainRepository: MainRepository
    private lateinit var digestionRepository: DigestionRepository
    private lateinit var driveLawService: DriveLawService

    // Views from included layouts (constraint_content_drinker_pickers.xml)
    private lateinit var editTextWeight: EditText
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var radioSexOther: RadioButton
    private lateinit var seekBarAlcoholTolerance: SeekBar
    private lateinit var textViewAlcoholToleranceTextValue: TextView

    // Views from included layouts (constraint_content_drinker_country.xml)
    private lateinit var textViewCurrentLimit: TextView
    private lateinit var editTextCurrentLimit: EditText
    private lateinit var spinnerCountry: Spinner
    private lateinit var checkboxYoungDriver: CheckBox
    private lateinit var checkboxProfessionalDriver: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDrinkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views from included layouts
        editTextWeight = view.findViewById(R.id.editTextWeight)
        radioMale = view.findViewById(R.id.radioMale)
        radioFemale = view.findViewById(R.id.radioFemale)
        radioSexOther = view.findViewById(R.id.radioSexOther)
        seekBarAlcoholTolerance = view.findViewById(R.id.seekBarAlcoholTolerance)
        textViewAlcoholToleranceTextValue = view.findViewById(R.id.textViewAlcoholToleranceTextValue)

        textViewCurrentLimit = view.findViewById(R.id.textViewCurrentLimit)
        editTextCurrentLimit = view.findViewById(R.id.editTextCurrentLimit)
        spinnerCountry = view.findViewById(R.id.spinnerCountry)
        checkboxYoungDriver = view.findViewById(R.id.checkboxYoungDriver)
        checkboxProfessionalDriver = view.findViewById(R.id.checkboxProfessionalDriver)

        mainRepository = viewModel.mainRepository
        digestionRepository = viewModel.digestionRepository

        val driveLawRepository = viewModel.driveLawRepository
        driveLawService = viewModel.driveLawService

        setupSpinnerCountry(
            driveLawService
                .getListOfCountriesWithFlags(),
        )

        setupWeightPicker()

        setupSexPicker(digestionRepository.body.sex)

        setupValidationButton(digestionRepository)

        driveLawRepository.liveDriveLaw.observe(viewLifecycleOwner) { driveLaw: DriveLaw ->
            // update the limit area (custom or not)
            if (driveLaw.isCustom()) {
                textViewCurrentLimit.visibility = TextView.GONE
                editTextCurrentLimit.visibility = TextView.VISIBLE
            } else {
                textViewCurrentLimit.visibility = TextView.VISIBLE
                // TODO : ugly structure for driveLimit call, move in driveLaw ?
                textViewCurrentLimit.text = driveLawService.driveLimit().toString()
                editTextCurrentLimit.visibility = TextView.GONE
                KeyboardUtils.hideKeyboard(requireActivity())
            }

            // Update for Young driver checkbox visibility (not value)
            if (driveLaw.youngLimit != null) {
                checkboxYoungDriver.visibility = CheckBox.VISIBLE
                val resId = resources.getIdentifier(driveLaw.youngLimit.explanationName, "string", requireActivity().packageName)
                if (resId != 0) {
                    checkboxYoungDriver.text = getString(resId)
                } else {
                    checkboxYoungDriver.text = driveLaw.youngLimit.explanationName
                }
            } else {
                checkboxYoungDriver.visibility = CheckBox.GONE
            }

            // Update for Professional drive checkbox visibility (not value)
            if (driveLaw.professionalLimit != null) {
                checkboxProfessionalDriver.visibility = CheckBox.VISIBLE
            } else {
                checkboxProfessionalDriver.visibility = CheckBox.GONE
            }
        }

        driveLawRepository.liveIsYoung.observe(viewLifecycleOwner) {
            checkboxYoungDriver.isChecked = it
        }
        driveLawRepository.liveIsProfessional.observe(viewLifecycleOwner) {
            checkboxProfessionalDriver.isChecked = it
        }

        setupCheckBoxes()

        setupAlcoholTolerance(digestionRepository)
    }

    override fun onResume() {
        super.onResume()
        updateCustomLimit(driveLawService.customCountryLimit)
    }

    private fun setupAlcoholTolerance(digestionRepository: DigestionRepository) {
        if (digestionRepository.toleranceLevels.isEmpty()) return

        val levelCount = digestionRepository.toleranceLevels.size - 1
        seekBarAlcoholTolerance.max = levelCount

        seekBarAlcoholTolerance.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    textViewAlcoholToleranceTextValue.text = digestionRepository.toleranceLevels[progress]
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            },
        )
        seekBarAlcoholTolerance.progress = (digestionRepository.body.alcoholTolerance * levelCount).roundToInt()
        textViewAlcoholToleranceTextValue.text = digestionRepository.toleranceLevels[seekBarAlcoholTolerance.progress]
    }

    private fun setupValidationButton(digestionRepository: DigestionRepository) {
        binding.buttonValidateDrinker.setOnClickListener {
            digestionRepository.body.sex =
                when {
                    radioMale.isChecked -> Sex.MALE
                    radioFemale.isChecked -> Sex.FEMALE
                    else -> Sex.OTHER
                }

            val levelCount = (digestionRepository.toleranceLevels.size - 1).coerceAtLeast(1)

            digestionRepository.body.alcoholTolerance =
                seekBarAlcoholTolerance.progress.toDouble() /
                levelCount.toDouble()

            driveLawService.customCountryLimit = editTextCurrentLimit.text.toString().toDoubleOrNull() ?: driveLawService.customCountryLimit

            var navOptions: NavOptions? = null

            if (!mainRepository.init) {
                mainRepository.init = true

                // Specific option needed for the init of the app, the drinker is the first fragment
                // and needs to be cleared (take over nav_graph definition).
                navOptions =
                    NavOptions.Builder()
                        .setPopUpTo(
                            R.id.drinkerFragment,
                            true,
                        ).build()
            }

            findNavController().navigate(
                DrinkerFragmentDirections.actionDrinkerFragmentToDriveFragment(),
                navOptions,
            )
        }
    }

    private fun setupCheckBoxes() {
        checkboxYoungDriver.setOnCheckedChangeListener { _, isChecked ->
            driveLawService.isYoung = isChecked
        }
        checkboxProfessionalDriver.setOnCheckedChangeListener { _, isChecked ->
            driveLawService.isProfessional = isChecked
        }
    }

    private fun setupSexPicker(recordedSex: Sex) {
        sex = recordedSex

        when (sex) {
            Sex.MALE -> radioMale.isChecked = true
            Sex.FEMALE -> radioFemale.isChecked = true
            else -> radioSexOther.isChecked = true
        }
    }

    private fun setupWeightPicker() {
        val currentWeight = digestionRepository.body.weight

        // Show as integer if it's a whole number, else decimal
        if (currentWeight == currentWeight.toInt().toDouble()) {
            editTextWeight.setText(currentWeight.toInt().toString())
        } else {
            editTextWeight.setText(currentWeight.toString())
        }

        editTextWeight.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    var newVal = s.toString().toDoubleOrNull()
                    if (newVal != null) {
                        // Prevent completely absurd weights if necessary, or just let it pass
                        if (newVal < 10.0) newVal = 10.0
                        if (newVal > 500.0) newVal = 500.0
                        digestionRepository.body.weight = newVal
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}
            },
        )
    }

    private fun setupSpinnerCountry(countries: List<String>) {
        spinnerCountry.adapter =
            ArrayAdapter(
                requireContext(),
                R.layout.item_country_spinner,
                countries,
            )
        val initialPosition = driveLawService.getIndexOfCurrent()
        spinnerCountry.setSelection(initialPosition)

        spinnerCountry.onItemSelectedListener =
            object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    if (position == 0) {
                        // handle the other country case
                        val customLimit = driveLawService.customCountryLimit
                        updateCustomLimit(customLimit)
                    }
                    driveLawService.select(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        editTextCurrentLimit.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    s.toString().toDoubleOrNull()?.let {
                        driveLawService.customCountryLimit = it
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                }
            },
        )
    }

    private fun updateCustomLimit(customLimit: Double) {
        editTextCurrentLimit.setText(
            ((customLimit * 100.0).roundToInt() / 100.0).toString(),
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

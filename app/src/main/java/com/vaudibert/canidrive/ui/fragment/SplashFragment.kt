package com.vaudibert.canidrive.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vaudibert.canidrive.BuildConfig
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.data.repository.MainRepository
import com.vaudibert.canidrive.databinding.FragmentSplashBinding
import org.koin.android.ext.android.inject

/**
 * The splash fragment, to display icon, version and do stuff in background.
 */
class SplashFragment : Fragment() {
    private val mainRepository: MainRepository by inject()

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.textViewVersionName.text = "v" + BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()

        val sharedPref =
            requireContext().getSharedPreferences(
                getString(R.string.user_preferences),
                Context.MODE_PRIVATE,
            )
        val disclaimerAccepted =
            sharedPref.getBoolean(
                getString(R.string.disclaimer_pref_key),
                false,
            )

        if (!disclaimerAccepted) {
            showDisclaimerDialog()
        } else {
            navigateToNext()
        }
    }

    private fun showDisclaimerDialog() {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.disclaimer_title)
            .setMessage(R.string.disclaimer_message)
            .setCancelable(false)
            .setPositiveButton(R.string.disclaimer_accept) { dialog, _ ->
                val sharedPref =
                    requireContext().getSharedPreferences(
                        getString(R.string.user_preferences),
                        Context.MODE_PRIVATE,
                    )
                sharedPref.edit()
                    .putBoolean(getString(R.string.disclaimer_pref_key), true)
                    .apply()
                dialog.dismiss()
                navigateToNext()
            }
            .show()
    }

    private fun navigateToNext() {
        if (!isAdded) return
        val init = mainRepository.init

        val action =
            if (init) {
                SplashFragmentDirections.actionSplashFragmentToDriveFragment()
            } else {
                SplashFragmentDirections.actionSplashFragmentToDrinkerFragment()
            }

        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

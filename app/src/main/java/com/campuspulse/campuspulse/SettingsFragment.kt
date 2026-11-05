package com.campuspulse.campuspulse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import coil.load
import com.campuspulse.campuspulse.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager

    // Image Picker Launcher (Requirement 3)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            preferenceManager.setProfileImageUri(it.toString())
            binding.ivProfilePicture.load(it) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_camera)
                error(android.R.drawable.ic_menu_camera)
            }
            Toast.makeText(requireContext(), getString(R.string.photo_updated), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())

        val userEmail = preferenceManager.getEmail() ?: "student@campuspulse.ac.za"
        val ssoProvider = preferenceManager.getSsoProvider() ?: "Email"
        binding.tvUserId.text = "${getString(R.string.logged_in_as)} $userEmail ($ssoProvider)"

        binding.etFullName.setText(preferenceManager.getFullName())
        binding.etProvince.setText(preferenceManager.getProvince())

        // Load persisted Profile Image
        val savedImageUri = preferenceManager.getProfileImageUri()
        if (!savedImageUri.isNullOrEmpty()) {
            binding.ivProfilePicture.load(savedImageUri) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_camera)
                error(android.R.drawable.ic_menu_camera)
            }
        }

        binding.btnUploadPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.switchNotifications.isChecked = preferenceManager.isNotificationsEnabled()
        binding.switchDarkMode.isChecked = preferenceManager.isDarkModeEnabled()

        val currentLang = preferenceManager.getLanguage()
        if (currentLang.equals("af", ignoreCase = true)) {
            binding.rbLangAf.isChecked = true
        } else {
            binding.rbLangEn.isChecked = true
        }

        // Persistent Dark Mode Toggle - Instant & Saved
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != preferenceManager.isDarkModeEnabled()) {
                preferenceManager.setDarkModeEnabled(isChecked)
                val targetMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(targetMode)
                requireActivity().recreate()
            }
        }

        binding.btnSaveSettings.setOnClickListener {
            val newName = binding.etFullName.text?.toString()?.trim() ?: ""
            val newProvince = binding.etProvince.text?.toString()?.trim() ?: ""

            preferenceManager.updateProfile(newName, newProvince)
            preferenceManager.setNotificationsEnabled(binding.switchNotifications.isChecked)

            val isDarkSelected = binding.switchDarkMode.isChecked
            preferenceManager.setDarkModeEnabled(isDarkSelected)

            val selectedLang = if (binding.rbLangAf.isChecked) "af" else "en"
            val isLangChanged = !selectedLang.equals(currentLang, ignoreCase = true)
            preferenceManager.setLanguage(selectedLang)

            if (isLangChanged) {
                LocaleHelper.setLocale(requireContext(), selectedLang)
                requireActivity().recreate()
            } else {
                Toast.makeText(requireContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSyncQueue.setOnClickListener {
            OfflineSyncWorker.enqueue(requireContext())
            Toast.makeText(requireContext(), getString(R.string.status_syncing), Toast.LENGTH_SHORT).show()
            NotificationHelper.showNotification(
                requireContext(),
                getString(R.string.push_notif_title),
                getString(R.string.sync_completed)
            )
        }

        binding.btnLogout.setOnClickListener {
            preferenceManager.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
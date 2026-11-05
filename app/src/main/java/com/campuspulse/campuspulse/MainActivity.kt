package com.campuspulse.campuspulse

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.campuspulse.campuspulse.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var preferenceManager: PreferenceManager
    private var wasOffline = false

    override fun attachBaseContext(newBase: Context) {
        val contextWithLocale = LocaleHelper.onAttach(newBase)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        if (!preferenceManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        // Apply saved theme preference
        if (preferenceManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setupNetworkMonitoring()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_events -> {
                    loadFragment(EventsFragment())
                    true
                }
                R.id.navigation_tickets -> {
                    loadFragment(TicketsFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.root.post {
                loadFragment(EventsFragment())
            }
        }
    }

    private fun setupNetworkMonitoring() {
        networkMonitor = NetworkMonitor(this)
        networkMonitor.startMonitoring()

        lifecycleScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                updateNetworkBadge(isOnline)
                if (isOnline && wasOffline) {
                    Toast.makeText(this@MainActivity, getString(R.string.sync_completed), Toast.LENGTH_SHORT).show()
                    OfflineSyncWorker.enqueue(this@MainActivity)
                }
                wasOffline = !isOnline
            }
        }
    }

    private fun updateNetworkBadge(isOnline: Boolean) {
        if (isOnline) {
            binding.tvStatusText.text = getString(R.string.status_online)
            val dotDrawable = ContextCompat.getDrawable(this, R.drawable.shape_status_dot)?.mutate()
            if (dotDrawable is GradientDrawable) {
                dotDrawable.setColor(ContextCompat.getColor(this, R.color.status_online))
            }
            binding.viewStatusDot.background = dotDrawable
            binding.cardStatusBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_container))
        } else {
            binding.tvStatusText.text = getString(R.string.status_offline)
            val dotDrawable = ContextCompat.getDrawable(this, R.drawable.shape_status_dot)?.mutate()
            if (dotDrawable is GradientDrawable) {
                dotDrawable.setColor(ContextCompat.getColor(this, R.color.status_offline))
            }
            binding.viewStatusDot.background = dotDrawable
            binding.cardStatusBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.accent_container))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val containerId = binding.fragmentContainer.id
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commitAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::networkMonitor.isInitialized) {
            networkMonitor.stopMonitoring()
        }
    }
}
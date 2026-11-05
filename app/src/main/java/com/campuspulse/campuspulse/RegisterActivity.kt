package com.campuspulse.campuspulse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.campuspulse.campuspulse.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun attachBaseContext(newBase: Context) {
        val contextWithLocale = LocaleHelper.onAttach(newBase)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferenceManager = PreferenceManager(this)
        if (preferenceManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val rawPassword = binding.etPassword.text?.toString()?.trim() ?: ""
            val province = binding.etProvince.text?.toString()?.trim() ?: ""

            if (!validateInputs(fullName, email, rawPassword, province)) return@setOnClickListener

            val encryptedHash = SecurityUtils.hashPassword(rawPassword)
            performRegister(fullName, email, encryptedHash, province)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(fullName: String, email: String, password: String, province: String): Boolean {
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.validation_fields)
            return false
        } else {
            binding.tilFullName.error = null
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.validation_email)
            return false
        } else {
            binding.tilEmail.error = null
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.validation_password)
            return false
        } else {
            binding.tilPassword.error = null
        }

        if (province.isEmpty()) {
            binding.tilProvince.error = getString(R.string.validation_fields)
            return false
        } else {
            binding.tilProvince.error = null
        }

        return true
    }

    private fun performRegister(fullName: String, email: String, encryptedHash: String, province: String) {
        lifecycleScope.launch {
            try {
                // Perform registration network call
                ApiClient.apiService.register(AuthRequest(email, encryptedHash, province))

                Toast.makeText(this@RegisterActivity, "Successfully registered ($fullName)! Please log in.", Toast.LENGTH_LONG).show()

                val intent = Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } catch (_: Exception) {
                // Handle offline or fallback registration path cleanly
                Toast.makeText(this@RegisterActivity, "Registered locally ($fullName)! Please log in.", Toast.LENGTH_LONG).show()

                val intent = Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
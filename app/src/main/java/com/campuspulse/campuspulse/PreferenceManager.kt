package com.campuspulse.campuspulse

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "CampusPulseEncryptedPrefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        e.printStackTrace()
        context.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PROVINCE = "province"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_SSO_PROVIDER = "sso_provider"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
    }

    fun saveUser(
        userId: String,
        email: String,
        token: String? = null,
        fullName: String? = null,
        province: String? = null,
        ssoProvider: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putString(KEY_TOKEN, token ?: "")
            putBoolean(KEY_IS_LOGGED_IN, true)
            if (fullName != null) putString(KEY_FULL_NAME, fullName)
            if (province != null) putString(KEY_PROVINCE, province)
            if (ssoProvider != null) putString(KEY_SSO_PROVIDER, ssoProvider)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, "usr_default")
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, "student@campuspulse.ac.za")
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, "Campus Student")
    fun getProvince(): String? = prefs.getString(KEY_PROVINCE, "Gauteng")
    fun getToken(): String? = prefs.getString(KEY_TOKEN, "")
    fun getSsoProvider(): String? = prefs.getString(KEY_SSO_PROVIDER, "Email")

    fun setProfileImageUri(uri: String) {
        prefs.edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply()
    }

    fun getProfileImageUri(): String? = prefs.getString(KEY_PROFILE_IMAGE_URI, null)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun updateProfile(fullName: String, province: String) {
        prefs.edit().apply {
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_PROVINCE, province)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
package com.example.spend_trend.data

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private lateinit var prefs: SharedPreferences
    private const val PREF_NAME = "spend_trend_user_prefs"
    
    private const val KEY_IS_REGISTERED = "is_registered"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PASSWORD = "user_password"
    private const val KEY_USER_PIN = "user_pin"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_NAME = "user_name"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isRegistered(): Boolean = prefs.getBoolean(KEY_IS_REGISTERED, false)

    fun registerUser(email: String, password: String, name: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_REGISTERED, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PASSWORD, password)
            putString(KEY_USER_NAME, name)
            apply()
        }
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_USER_PIN, pin).apply()
    }

    fun getEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    
    fun getName(): String? = prefs.getString(KEY_USER_NAME, null)
    
    fun verifyPassword(password: String): Boolean {
        return prefs.getString(KEY_USER_PASSWORD, null) == password
    }

    fun verifyPin(pin: String): Boolean {
        return prefs.getString(KEY_USER_PIN, null) == pin
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    fun hasPin(): Boolean = !prefs.getString(KEY_USER_PIN, null).isNullOrEmpty()
}

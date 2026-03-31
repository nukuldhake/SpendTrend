package com.example.spend_trend.data

import android.content.Context
import android.content.SharedPreferences
import com.example.spend_trend.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth

object UserPreferences {
    private lateinit var prefs: SharedPreferences
    private const val PREF_NAME = "spend_trend_user_prefs"
    
    private const val KEY_IS_REGISTERED = "is_registered"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PASSWORD = "user_password"
    private const val KEY_USER_PIN = "user_pin"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_MEMBER_SINCE = "member_since_millis"

    private val auth get() = SupabaseClient.client.auth

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
            putLong(KEY_MEMBER_SINCE, System.currentTimeMillis())
            apply()
        }
    }

    fun updateName(newName: String) {
        prefs.edit().putString(KEY_USER_NAME, newName).apply()
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_USER_PIN, pin).apply()
    }

    fun getEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    
    fun getName(): String? = prefs.getString(KEY_USER_NAME, null)
    
    fun getMemberSinceMillis(): Long = prefs.getLong(KEY_MEMBER_SINCE, System.currentTimeMillis())
    
    fun verifyPassword(password: String): Boolean {
        return prefs.getString(KEY_USER_PASSWORD, null) == password
    }

    fun verifyPin(pin: String): Boolean {
        return prefs.getString(KEY_USER_PIN, null) == pin
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }
    
    fun isLoggedIn(): Boolean = auth.currentUserOrNull() != null
    
    fun hasPin(): Boolean = !prefs.getString(KEY_USER_PIN, null).isNullOrEmpty()

    fun logout() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
        // Supabase signout is async, we handle it in the UI or let it be.
    }

    private const val KEY_SMS_SYNC_DONE = "sms_sync_done"

    fun isSmsSyncDone(): Boolean = prefs.getBoolean(KEY_SMS_SYNC_DONE, false)

    fun setSmsSyncDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_SMS_SYNC_DONE, done).apply()
    }
}

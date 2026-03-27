package com.example.spend_trend.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.spend_trend.data.UserPreferences

class AuthViewModel : ViewModel() {
    var email by mutableStateOf("")
    var name by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var pin by mutableStateOf("")
    var confirmPin by mutableStateOf("")
    
    var error by mutableStateOf<String?>(null)
    
    val isRegistered: Boolean get() = UserPreferences.isRegistered()
    val hasPin: Boolean get() = UserPreferences.hasPin()
    val registeredEmail: String? get() = UserPreferences.getEmail()
    val registeredName: String? get() = UserPreferences.getName()

    fun register(): Boolean {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            error = "All fields are required"
            return false
        }
        if (password != confirmPassword) {
            error = "Passwords do not match"
            return false
        }
        UserPreferences.registerUser(email, password, name)
        error = null
        return true
    }

    fun setPin(): Boolean {
        if (pin.length != 4) {
            error = "PIN must be 4 digits"
            return false
        }
        if (pin != confirmPin) {
            error = "PINs do not match"
            return false
        }
        UserPreferences.setPin(pin)
        error = null
        return true
    }

    fun loginWithPassword(): Boolean {
        if (UserPreferences.verifyPassword(password)) {
            UserPreferences.setLoggedIn(true)
            error = null
            return true
        } else {
            error = "Invalid Password"
            return false
        }
    }

    fun loginWithPin(): Boolean {
        if (UserPreferences.verifyPin(pin)) {
            UserPreferences.setLoggedIn(true)
            error = null
            return true
        } else {
            error = "Invalid PIN"
            return false
        }
    }
}

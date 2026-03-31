package com.example.spend_trend.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.UserPreferences
import com.example.spend_trend.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var email by mutableStateOf("")
    var name by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var pin by mutableStateOf("")
    var confirmPin by mutableStateOf("")
    
    var error by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    
    private val auth = SupabaseClient.client.auth
    
    val isRegistered: Boolean get() = UserPreferences.isRegistered()
    val hasPin: Boolean get() = UserPreferences.hasPin()
    val registeredEmail: String? get() = UserPreferences.getEmail()
    val registeredName: String? get() = UserPreferences.getName()

    fun register(onSuccess: () -> Unit) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            error = "All fields are required"
            return
        }
        if (password != confirmPassword) {
            error = "Passwords do not match"
            return
        }
        
        isLoading = true
        error = null
        
        viewModelScope.launch {
            try {
                auth.signUpWith(Email) {
                    email = this@AuthViewModel.email
                    password = this@AuthViewModel.password
                }
                UserPreferences.registerUser(email, password, name)
                com.example.spend_trend.ui.theme.ThemePreferences.updateUserName(name)
                isLoading = false
                onSuccess()
            } catch (e: Exception) {
                isLoading = false
                error = e.localizedMessage ?: "Registration failed"
            }
        }
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

    fun loginWithPassword(onSuccess: () -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            error = "Email and Password are required"
            return
        }

        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                auth.signInWith(Email) {
                    email = this@AuthViewModel.email
                    password = this@AuthViewModel.password
                }
                
                val user = auth.currentUserOrNull()
                
                // If login is successful but user isn't 'locally registered' (e.g. reinstall),
                // we restore their profile states.
                if (!UserPreferences.isRegistered() && user != null) {
                    UserPreferences.registerUser(
                        email = user.email ?: email,
                        password = password, 
                        name = "User" // Supabase metadata can also store name, but let's keep it simple
                    )
                }
                
                UserPreferences.setLoggedIn(true)
                isLoading = false
                onSuccess()
            } catch (e: Exception) {
                isLoading = false
                error = "Invalid Email or Password"
            }
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

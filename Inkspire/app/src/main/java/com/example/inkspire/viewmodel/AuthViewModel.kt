package com.example.inkspire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspire.model.UserProfile
import com.example.inkspire.repository.AuthRepository
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _messageEvent = MutableLiveData<String>()
    val messageEvent: LiveData<String> get() = _messageEvent

    fun signUp(email: String, password: String, username: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signUp(email, password, username)
            result.onSuccess {
                _messageEvent.postValue("Sign up completed successfully!")
                onSuccess()
            }.onFailure {
                val message = when {
                    it.message?.contains("Username already taken", ignoreCase = true) == true ->
                        "This username is already taken. Please choose another."
                    it.message?.contains("registered", ignoreCase = true) == true ->
                        "This email is already registered. Please choose another"
                    else ->
                        "Sign up failed: ${it.message}"
                }
                _messageEvent.postValue(message)
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.onSuccess {
                _messageEvent.postValue("Logged in successfully!")
                onSuccess()
            }.onFailure {
                _messageEvent.postValue("Login failed: ${it.message}")
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _messageEvent.postValue("You have been logged out.")
            onSuccess()
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()


    //Per uso diretto nei fragments
    fun currentUser(): UserInfo? = authRepository.currentUser()

    fun getCurrentUserProfile(onResult: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            val profile = authRepository.getCurrentUserProfile()
            onResult(profile)
        }
    }

//    fun getUsername(onResult: (String?) -> Unit) {
//        viewModelScope.launch {
//            val username = authRepository.getUsername()
//            onResult(username)
//        }
//    }

//    fun getUserProfileById(userId: String, onResult: (UserProfile?) -> Unit) {
//        viewModelScope.launch {
//            val profile = authRepository.getUserProfileByUserId(userId)
//            onResult(profile)
//        }
//    }
}
package com.example.inkspire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspire.model.UserProfileVW
import com.example.inkspire.repository.UserRepository
import kotlinx.coroutines.launch

class UserListViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<UserProfileVW>>()
    val users: LiveData<List<UserProfileVW>> = _users

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun getAllUsers() {
        viewModelScope.launch {
            _loading.value = true
            _users.value = userRepository.getAllUsers()
            _loading.value = false
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _loading.value = true
            _users.value = userRepository.searchUsers(query.trim())
            _loading.value = false
        }
    }
}
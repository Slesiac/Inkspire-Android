package com.example.inkspire.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.viewmodel.UserProfileViewModel

class UserProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val challengeRepository: ChallengeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(userRepository, challengeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
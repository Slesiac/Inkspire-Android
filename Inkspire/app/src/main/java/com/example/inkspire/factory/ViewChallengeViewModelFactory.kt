package com.example.inkspire.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.viewmodel.ViewChallengeViewModel

class ViewChallengeViewModelFactory(
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewChallengeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViewChallengeViewModel(challengeRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
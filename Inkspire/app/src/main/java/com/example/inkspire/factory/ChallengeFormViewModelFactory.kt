package com.example.inkspire.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.StorageRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.viewmodel.ChallengeFormViewModel

class ChallengeFormViewModelFactory(
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChallengeFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChallengeFormViewModel(challengeRepository, userRepository, storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
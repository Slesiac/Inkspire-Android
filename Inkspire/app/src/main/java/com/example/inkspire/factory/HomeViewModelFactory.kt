package com.example.inkspire.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.viewmodel.HomeViewModel

class HomeViewModelFactory( private val challengeRepository: ChallengeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(challengeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
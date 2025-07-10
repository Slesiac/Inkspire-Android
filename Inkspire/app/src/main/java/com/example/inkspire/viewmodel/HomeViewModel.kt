package com.example.inkspire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspire.model.ChallengeVW
import com.example.inkspire.repository.ChallengeRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _challenges = MutableLiveData<List<ChallengeVW>>()
    val challenges: LiveData<List<ChallengeVW>> = _challenges

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun getAllChallenges() {
        viewModelScope.launch {
            _loading.value = true
            _challenges.value = challengeRepository.getAllChallenges()
            _loading.value = false
        }
    }

    fun searchChallenges(query: String) {
        viewModelScope.launch {
            _loading.value = true
            _challenges.value = challengeRepository.searchChallenges(query.trim())
            _loading.value = false
        }
    }
}
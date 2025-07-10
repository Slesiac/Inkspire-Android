package com.example.inkspire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspire.model.ChallengeVW
import com.example.inkspire.model.UserProfile
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.UserRepository
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _challenges = MutableLiveData<List<ChallengeVW>>()
    val challenges: LiveData<List<ChallengeVW>> = _challenges

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _stats = MutableLiveData<Pair<Int, Int>>() // Pair(total, completed)
    val stats: LiveData<Pair<Int, Int>> = _stats

    fun getUserChallenges(userId: String) {
        viewModelScope.launch {
            _challenges.value = challengeRepository.getChallengesByUser(userId)
        }
    }

    fun getUserData(userId: String) {
        viewModelScope.launch {
            _profile.value = userRepository.getUserProfileById(userId)
            _stats.value = userRepository.getUserStats(userId)
        }
    }
}


/*
class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _userStats = MutableLiveData<Pair<Int, Int>>() // total, completed
    val userStats: LiveData<Pair<Int, Int>> = _userStats

    private val _userChallenges = MutableLiveData<List<Challenge>>()
    val userChallenges: LiveData<List<Challenge>> = _userChallenges

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            _userProfile.value = userRepository.getUserProfileById(userId)
            _userStats.value = userRepository.getUserStats(userId)
            _userChallenges.value = challengeRepository.getChallengesByUser(userId)
        }
    }
}

 */
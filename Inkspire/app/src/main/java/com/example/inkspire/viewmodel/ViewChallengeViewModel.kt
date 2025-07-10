package com.example.inkspire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspire.model.ChallengeVW
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.UserRepository
import kotlinx.coroutines.launch

class ViewChallengeViewModel(
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _challenge = MutableLiveData<ChallengeVW?>()
    val challenge: LiveData<ChallengeVW?> = _challenge

    private val _isCurrentUserAuthor = MutableLiveData<Boolean>()
    val isCurrentUserAuthor: LiveData<Boolean> = _isCurrentUserAuthor

    //Carica i dati completi della challenge e verifica se l'utente corrente è l'autore
    fun loadChallenge(challengeId: Int) {
        viewModelScope.launch {
            try {
                val selected = challengeRepository.getChallengeById(challengeId)

                _challenge.value = selected

                val currentUserId = userRepository.getCurrentUserId()
                _isCurrentUserAuthor.value = selected?.user_id == currentUserId
            } catch (e: Exception) {
                e.printStackTrace()
                _challenge.value = null
                _isCurrentUserAuthor.value = false
            }
        }
    }
}
package com.example.inkspire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspire.model.Challenge
import com.example.inkspire.model.ChallengeVW
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.StorageRepository
import com.example.inkspire.repository.UserRepository
import io.ktor.http.ContentType
import kotlinx.coroutines.launch

class ChallengeFormViewModel(
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _loadedChallenge = MutableLiveData<ChallengeVW?>()
    val loadedChallenge: LiveData<ChallengeVW?> get() = _loadedChallenge

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> get() = _saveResult

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _randomConcept = MutableLiveData<String?>()
    val randomConcept: LiveData<String?> = _randomConcept

    private val _randomArtConstraint = MutableLiveData<String?>()
    val randomArtConstraint: LiveData<String?> = _randomArtConstraint

    //Carica i dettagli completi di una challenge per l'edit
    fun loadChallenge(challengeId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val challenge = challengeRepository.getChallengeById(challengeId)
                _loadedChallenge.value = challenge
            } catch (e: Exception) {
                e.printStackTrace()
                _loadedChallenge.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    //Salva la challenge (Se isEdit = true fa update, altrimenti insert)
    fun saveChallenge(challenge: Challenge, isEdit: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            val success = if (isEdit) {
                challengeRepository.updateChallenge(challenge)
            } else {
                challengeRepository.insertChallenge(challenge)
            }
            _saveResult.value = success
            _loading.value = false
        }
    }

    //Cancella la challenge
    fun deleteChallenge(challengeId: Int) {
        viewModelScope.launch {
            _loading.value = true
            val success = challengeRepository.deleteChallenge(challengeId)
            _deleteResult.value = success
            _loading.value = false
        }
    }

    //Recupera l'ID dell'utente corrente (per assegnare l'autore)
    fun getCurrentUserId(): String? = userRepository.getCurrentUserId()

    //Suggerisci un concept random
    fun fetchRandomConcept() {
        viewModelScope.launch {
            _randomConcept.postValue(challengeRepository.getRandomConcept())
        }
    }

    //Suggerisci un art constraint random
    fun fetchRandomArtConstraint() {
        viewModelScope.launch {
            _randomArtConstraint.postValue(challengeRepository.getRandomArtConstraint())
        }
    }

    suspend fun uploadImage(
        bucket: String,
        filePath: String,
        byteArray: ByteArray,
        contentType: ContentType
    ): String? {
        return storageRepository.uploadImage(bucket, filePath, byteArray, contentType)
    }

}
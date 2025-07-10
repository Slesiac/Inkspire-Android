package com.example.inkspire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspire.model.UserProfile
import com.example.inkspire.repository.StorageRepository
import com.example.inkspire.repository.UserRepository
import io.ktor.http.ContentType
import kotlinx.coroutines.launch

class UserProfileFormViewModel(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> get() = _profile

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> get() = _saveResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    //Carica i dati del profilo corrente
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _profile.value = userRepository.getUserProfileById(userId)
            } catch (e: Exception) {
                e.printStackTrace()
                _profile.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    //Salva il profilo aggiornato
    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            _loading.value = true
            val success = userRepository.updateUserProfile(profile)
            _saveResult.value = success
            _loading.value = false
        }
    }

    //Restituisce l'ID utente corrente
    fun getCurrentUserId(): String? = userRepository.getCurrentUserId()

    //Esegue l'upload dell'immagine e restituisce l'URL pubblico
    suspend fun uploadImage(
        bucket: String,
        filePath: String,
        byteArray: ByteArray,
        contentType: ContentType
    ): String? {
        return storageRepository.uploadImage(bucket, filePath, byteArray, contentType)
    }
}
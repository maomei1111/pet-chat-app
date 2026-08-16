package com.maomei.petchatapp.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.model.PetProfile
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.ReplyLength
import com.maomei.petchatapp.data.repository.ChatRepository
import com.maomei.petchatapp.data.repository.PetRepository
import com.maomei.petchatapp.data.repository.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 10. ペット設定画面 のロジック。 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val petRepository: PetRepository,
    private val photoRepository: PhotoRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val pet: StateFlow<PetProfile?> = petRepository.observeActiveProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val photos: StateFlow<List<PetPhoto>> = pet
        .flatMapLatest { p -> if (p == null) flowOf(emptyList()) else photoRepository.observePhotosForPet(p.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _photoErrorMessage = MutableStateFlow<String?>(null)
    val photoErrorMessage: StateFlow<String?> = _photoErrorMessage.asStateFlow()

    private val _resetDone = MutableStateFlow(false)
    val resetDone: StateFlow<Boolean> = _resetDone.asStateFlow()

    private fun updateProfile(transform: (PetProfile) -> PetProfile) {
        val current = pet.value ?: return
        viewModelScope.launch {
            petRepository.update(transform(current).copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateName(name: String) = updateProfile { it.copy(name = name) }
    fun updatePersonality(personality: Personality) = updateProfile { it.copy(personality = personality) }
    fun updateOwnerCallName(value: String) = updateProfile { it.copy(ownerCallName = value) }
    fun updateFirstPerson(value: String) = updateProfile { it.copy(firstPerson = value) }
    fun updateReplyLength(value: ReplyLength) = updateProfile { it.copy(replyLength = value) }
    fun updateUseEmoji(value: Boolean) = updateProfile { it.copy(useEmoji = value) }

    fun addPhotos(uris: List<Uri>) {
        val petId = pet.value?.id ?: return
        viewModelScope.launch {
            val imported = photoRepository.importPhotos(petId, uris)
            if (pet.value?.mainPhotoId == null && imported.isNotEmpty()) {
                photoRepository.setMainPhoto(petId, imported.first().id)
                updateProfile { it.copy(mainPhotoId = imported.first().id) }
            }
            _photoErrorMessage.value = null
        }
    }

    fun deletePhoto(photo: PetPhoto) {
        val petId = pet.value?.id ?: return
        viewModelScope.launch {
            photoRepository.deletePhoto(photo)
            val remaining = photoRepository.getPhotosForPet(petId)
            if (remaining.isEmpty()) {
                updateProfile { it.copy(mainPhotoId = null) }
                _photoErrorMessage.value = "写真を1枚以上登録してください"
            } else if (photo.id == pet.value?.mainPhotoId) {
                photoRepository.setMainPhoto(petId, remaining.first().id)
                updateProfile { it.copy(mainPhotoId = remaining.first().id) }
            }
        }
    }

    fun setMainPhoto(photo: PetPhoto) {
        val petId = pet.value?.id ?: return
        viewModelScope.launch {
            photoRepository.setMainPhoto(petId, photo.id)
            updateProfile { it.copy(mainPhotoId = photo.id) }
        }
    }

    fun clearHistory() {
        val petId = pet.value?.id ?: return
        viewModelScope.launch { chatRepository.clearHistory(petId) }
    }

    /** 10章「ペット情報の初期化」。確認ダイアログでの承認後に呼ばれる想定。 */
    fun resetPet() {
        val petId = pet.value?.id ?: return
        viewModelScope.launch {
            chatRepository.clearHistory(petId)
            photoRepository.deleteAllForPet(petId)
            petRepository.resetAll()
            _resetDone.value = true
        }
    }
}

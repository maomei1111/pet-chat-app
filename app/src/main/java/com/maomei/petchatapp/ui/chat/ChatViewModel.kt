package com.maomei.petchatapp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maomei.petchatapp.data.model.ChatMessage
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.model.PetProfile
import com.maomei.petchatapp.data.model.Sender
import com.maomei.petchatapp.data.repository.ChatRepository
import com.maomei.petchatapp.data.repository.PetRepository
import com.maomei.petchatapp.data.repository.PhotoRepository
import com.maomei.petchatapp.reply.ReplyEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

/** 9. メイン会話画面 のロジック（5章 会話画面仕様に対応）。 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val petRepository: PetRepository,
    private val photoRepository: PhotoRepository,
    private val chatRepository: ChatRepository,
    private val replyEngine: ReplyEngine
) : ViewModel() {

    val pet: StateFlow<PetProfile?> = petRepository.observeActiveProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val photos: StateFlow<List<PetPhoto>> = pet
        .flatMapLatest { p -> if (p == null) flowOf(emptyList()) else photoRepository.observePhotosForPet(p.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<ChatMessage>> = pet
        .flatMapLatest { p -> if (p == null) flowOf(emptyList()) else chatRepository.observeMessagesForPet(p.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private var lastUsedPhotoId: String? = null

    fun updateInput(text: String) {
        _inputText.value = text
    }

    /** 5.2 送信処理。二重送信を防止する。 */
    fun send() {
        val text = _inputText.value.trim()
        val currentPet = pet.value
        if (text.isEmpty() || currentPet == null || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    petId = currentPet.id,
                    sender = Sender.USER,
                    text = text,
                    photoId = null,
                    category = null,
                    replySource = null,
                    createdAt = System.currentTimeMillis()
                )
                chatRepository.addMessage(userMessage)
                _inputText.value = ""

                // 即レスだと不自然なため、考えている間のような短い間を置く。
                delay(Random.nextLong(600L, 1400L))

                val generated = replyEngine.generateReply(currentPet, text)
                val photo = pickPhoto(photos.value)
                lastUsedPhotoId = photo?.id

                val petMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    petId = currentPet.id,
                    sender = Sender.PET,
                    text = generated.text,
                    photoId = photo?.id,
                    category = generated.category,
                    replySource = generated.source,
                    createdAt = System.currentTimeMillis()
                )
                chatRepository.addMessage(petMessage)
            } finally {
                _isSending.value = false
            }
        }
    }

    /** 5.3 写真選択ルール: ランダム選択・直前と同じ写真は可能な限り避ける・1枚のみなら固定。 */
    private fun pickPhoto(available: List<PetPhoto>): PetPhoto? {
        if (available.isEmpty()) return null
        if (available.size == 1) return available.first()
        val candidates = available.filter { it.id != lastUsedPhotoId }
        val pool = candidates.ifEmpty { available }
        return pool.random()
    }
}

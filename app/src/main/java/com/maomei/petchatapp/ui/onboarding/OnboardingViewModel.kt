package com.maomei.petchatapp.ui.onboarding

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.model.PetProfile
import com.maomei.petchatapp.data.model.PetSpecies
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.ReplyLength
import com.maomei.petchatapp.data.photo.GoogleOAuthService
import com.maomei.petchatapp.data.photo.GooglePhotosPickerService
import com.maomei.petchatapp.data.photo.GooglePickerSession
import com.maomei.petchatapp.data.repository.PetRepository
import com.maomei.petchatapp.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** 7章 Google Photos Picker API 連携フローの状態。 */
sealed interface GooglePickerUiState {
    data object Idle : GooglePickerUiState
    data object RequestingAuthorization : GooglePickerUiState
    data class NeedsUserConsent(val pendingIntent: PendingIntent) : GooglePickerUiState
    data class WaitingForSelection(val session: GooglePickerSession) : GooglePickerUiState
    data object Importing : GooglePickerUiState
    data class Failed(val message: String) : GooglePickerUiState
}

/** ペット登録フロー（4.1〜4.7）で入力中のドラフト状態。 */
data class OnboardingUiState(
    val petId: String = UUID.randomUUID().toString(),
    val species: PetSpecies = PetSpecies.DOG,
    val name: String = "",
    val ownerCallName: String = "",
    val firstPerson: String = "",
    val personality: Personality = Personality.default(PetSpecies.DOG),
    val replyLength: ReplyLength = ReplyLength.SHORT,
    val useEmoji: Boolean = true,
    val mainPhotoId: String? = null,
    val photoErrorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedProfileId: String? = null
)

/**
 * 2〜8章の登録フロー全体で共有されるドラフト。Navigation の入れ子グラフ
 * ("onboarding_graph") にスコープした単一インスタンスとして各画面から参照する。
 */
class OnboardingViewModel(
    private val petRepository: PetRepository,
    private val photoRepository: PhotoRepository,
    private val googleOAuthService: GoogleOAuthService,
    private val googlePhotosPickerService: GooglePhotosPickerService?
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val photos: StateFlow<List<PetPhoto>> = _uiState
        .value.let { photoRepository.observePhotosForPet(it.petId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Google Cloud Console 側のOAuthクライアント登録が完了していれば true。 */
    val isGooglePhotosAvailable: Boolean get() = googlePhotosPickerService != null

    private val _googlePickerState = MutableStateFlow<GooglePickerUiState>(GooglePickerUiState.Idle)
    val googlePickerState: StateFlow<GooglePickerUiState> = _googlePickerState.asStateFlow()

    private var pendingAccessToken: String? = null

    /** 「Googleフォトから写真を選ぶ」ボタン押下時のエントリポイント。 */
    fun startGooglePhotoPicker() {
        val service = googlePhotosPickerService ?: run {
            _googlePickerState.value = GooglePickerUiState.Failed("Googleフォト連携は準備中です")
            return
        }
        _googlePickerState.value = GooglePickerUiState.RequestingAuthorization
        viewModelScope.launch {
            runCatching { googleOAuthService.requestAuthorization() }
                .onSuccess { result ->
                    val token = result.accessToken
                    when {
                        token != null -> createSessionAndOpen(service, token)
                        result.hasResolution() && result.pendingIntent != null ->
                            _googlePickerState.value = GooglePickerUiState.NeedsUserConsent(result.pendingIntent!!)
                        else ->
                            _googlePickerState.value = GooglePickerUiState.Failed("Google認証を開始できませんでした")
                    }
                }
                .onFailure {
                    _googlePickerState.value = GooglePickerUiState.Failed("Google認証を開始できませんでした")
                }
        }
    }

    /** OAuth同意画面（`StartIntentSenderForResult`）から戻ってきた結果を処理する。 */
    fun onAuthorizationResolved(intent: Intent?) {
        val service = googlePhotosPickerService ?: return
        val result = intent?.let { googleOAuthService.resultFromIntent(it) }
        val token = result?.accessToken
        if (token == null) {
            _googlePickerState.value = GooglePickerUiState.Failed("写真を取得できませんでした")
            return
        }
        viewModelScope.launch { createSessionAndOpen(service, token) }
    }

    private suspend fun createSessionAndOpen(service: GooglePhotosPickerService, accessToken: String) {
        pendingAccessToken = accessToken
        runCatching { service.createSession(accessToken) }
            .onSuccess { session ->
                _googlePickerState.value = GooglePickerUiState.WaitingForSelection(session)
            }
            .onFailure {
                _googlePickerState.value = GooglePickerUiState.Failed("写真を取得できませんでした")
            }
    }

    /**
     * pickerUri を外部（Googleフォト/ブラウザ）で開いた後、UI側（LaunchedEffect）から呼び出す。
     * 選択完了をポーリングで検知し、完了していれば画像をダウンロードして端末内へ保存する（仕様 7.3, 7.5, 7.6）。
     */
    fun pollForSelection() {
        val service = googlePhotosPickerService ?: return
        val token = pendingAccessToken ?: return
        val session = (_googlePickerState.value as? GooglePickerUiState.WaitingForSelection)?.session ?: return

        viewModelScope.launch {
            val ready = runCatching { service.pollUntilReady(token, session) }.getOrDefault(false)
            if (!ready) {
                _googlePickerState.value = GooglePickerUiState.Failed("写真を取得できませんでした")
                return@launch
            }

            _googlePickerState.value = GooglePickerUiState.Importing
            val imported = runCatching { service.importSelectedPhotos(token, _uiState.value.petId, session.id) }
                .getOrDefault(emptyList())
            runCatching { service.deleteSession(token, session.id) }
            pendingAccessToken = null

            if (imported.isEmpty()) {
                _googlePickerState.value = GooglePickerUiState.Failed("写真を取得できませんでした")
                return@launch
            }

            photoRepository.saveImportedPhotos(imported)
            if (_uiState.value.mainPhotoId == null) {
                val first = imported.first()
                photoRepository.setMainPhoto(_uiState.value.petId, first.id)
                _uiState.update { it.copy(mainPhotoId = first.id, photoErrorMessage = null) }
            }
            _googlePickerState.value = GooglePickerUiState.Idle
        }
    }

    fun cancelGooglePicker() {
        val token = pendingAccessToken
        val session = (_googlePickerState.value as? GooglePickerUiState.WaitingForSelection)?.session
        pendingAccessToken = null
        _googlePickerState.value = GooglePickerUiState.Idle
        val service = googlePhotosPickerService
        if (service != null && token != null && session != null) {
            viewModelScope.launch { runCatching { service.deleteSession(token, session.id) } }
        }
    }

    fun dismissGooglePickerError() {
        _googlePickerState.value = GooglePickerUiState.Idle
    }

    fun selectSpecies(species: PetSpecies) {
        _uiState.update {
            it.copy(
                species = species,
                personality = Personality.default(species),
                firstPerson = it.firstPerson.ifBlank { defaultFirstPerson(species) }
            )
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateOwnerCallName(value: String) = _uiState.update { it.copy(ownerCallName = value) }
    fun updateFirstPerson(value: String) = _uiState.update { it.copy(firstPerson = value) }
    fun updatePersonality(value: Personality) = _uiState.update { it.copy(personality = value) }
    fun updateReplyLength(value: ReplyLength) = _uiState.update { it.copy(replyLength = value) }
    fun updateUseEmoji(value: Boolean) = _uiState.update { it.copy(useEmoji = value) }

    fun importPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val petId = _uiState.value.petId
            val imported = photoRepository.importPhotos(petId, uris)
            if (_uiState.value.mainPhotoId == null && imported.isNotEmpty()) {
                val first = imported.first()
                photoRepository.setMainPhoto(petId, first.id)
                _uiState.update { it.copy(mainPhotoId = first.id, photoErrorMessage = null) }
            } else {
                _uiState.update { it.copy(photoErrorMessage = null) }
            }
        }
    }

    fun deletePhoto(photo: PetPhoto) {
        viewModelScope.launch {
            photoRepository.deletePhoto(photo)
            val remaining = photoRepository.getPhotosForPet(_uiState.value.petId)
            if (remaining.isEmpty()) {
                _uiState.update { it.copy(mainPhotoId = null, photoErrorMessage = "写真を1枚以上登録してください") }
            } else if (photo.id == _uiState.value.mainPhotoId) {
                val newMain = remaining.first()
                photoRepository.setMainPhoto(_uiState.value.petId, newMain.id)
                _uiState.update { it.copy(mainPhotoId = newMain.id) }
            }
        }
    }

    fun setMainPhoto(photo: PetPhoto) {
        viewModelScope.launch {
            photoRepository.setMainPhoto(_uiState.value.petId, photo.id)
            _uiState.update { it.copy(mainPhotoId = photo.id) }
        }
    }

    /** 写真確認画面の「次へ」バリデーション。1枚も無ければ false。 */
    fun validatePhotosOrShowError(): Boolean {
        val hasPhotos = photos.value.isNotEmpty()
        if (!hasPhotos) {
            _uiState.update { it.copy(photoErrorMessage = "写真を1枚以上登録してください") }
        }
        return hasPhotos
    }

    /** 8章 登録内容確認画面 の「この設定で始める」。PetProfile を保存する。 */
    fun completeRegistration(onDone: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val now = System.currentTimeMillis()
            val effectiveName = state.name.ifBlank { "うちの子" }
            val effectiveOwnerCallName = state.ownerCallName.ifBlank { "きみ" }
            val effectiveFirstPerson = state.firstPerson.ifBlank { defaultFirstPerson(state.species) }

            val profile = PetProfile(
                id = state.petId,
                name = effectiveName,
                species = state.species,
                personality = state.personality,
                ownerCallName = effectiveOwnerCallName,
                firstPerson = effectiveFirstPerson,
                replyLength = state.replyLength,
                useEmoji = state.useEmoji,
                mainPhotoId = state.mainPhotoId,
                createdAt = now,
                updatedAt = now
            )
            petRepository.save(profile)
            _uiState.update { it.copy(isSaving = false, savedProfileId = profile.id) }
            onDone(profile.id)
        }
    }

    /** 2.1 「あとで設定する」: サンプル設定・写真無しでデフォルトの PetProfile を即時作成する。 */
    fun createDefaultProfileAndProceed(onDone: (String) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = UUID.randomUUID().toString()
            val profile = PetProfile(
                id = id,
                name = "うちの子",
                species = PetSpecies.DOG,
                personality = Personality.default(PetSpecies.DOG),
                ownerCallName = "きみ",
                firstPerson = defaultFirstPerson(PetSpecies.DOG),
                replyLength = ReplyLength.SHORT,
                useEmoji = true,
                mainPhotoId = null,
                createdAt = now,
                updatedAt = now
            )
            petRepository.save(profile)
            onDone(profile.id)
        }
    }

    companion object {
        fun defaultFirstPerson(species: PetSpecies): String =
            if (species == PetSpecies.DOG) "ぼく" else "わたし"
    }
}

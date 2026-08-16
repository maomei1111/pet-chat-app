package com.maomei.petchatapp.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maomei.petchatapp.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashDestination {
    data object Loading : SplashDestination
    data object Onboarding : SplashDestination
    data object Chat : SplashDestination
}

/** 1. 起動画面: 既存の PetProfile が Room にあるかどうかで遷移先を決める。 */
class SplashViewModel(private val petRepository: PetRepository) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = petRepository.getActiveProfile()
            _destination.value = if (profile == null) {
                SplashDestination.Onboarding
            } else {
                SplashDestination.Chat
            }
        }
    }
}

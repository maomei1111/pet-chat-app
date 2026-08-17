package com.maomei.petchatapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maomei.petchatapp.ui.chat.ChatViewModel
import com.maomei.petchatapp.ui.onboarding.OnboardingViewModel
import com.maomei.petchatapp.ui.settings.SettingsViewModel
import com.maomei.petchatapp.ui.splash.SplashViewModel

/**
 * Hilt を使わないため、[AppContainer] を手動で各 ViewModel に配線するファクトリ。
 */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel(container.petRepository) as T

            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(
                    petRepository = container.petRepository,
                    photoRepository = container.photoRepository,
                    googleOAuthService = container.googleOAuthService,
                    googlePhotosPickerService = container.googlePhotosPickerService
                ) as T

            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(
                    petRepository = container.petRepository,
                    photoRepository = container.photoRepository,
                    chatRepository = container.chatRepository,
                    replyEngine = container.replyEngine
                ) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(
                    petRepository = container.petRepository,
                    photoRepository = container.photoRepository,
                    chatRepository = container.chatRepository
                ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

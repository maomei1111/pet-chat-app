package com.maomei.petchatapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.maomei.petchatapp.di.AppViewModelFactory
import com.maomei.petchatapp.ui.chat.ChatScreen
import com.maomei.petchatapp.ui.onboarding.ConfirmationScreen
import com.maomei.petchatapp.ui.onboarding.OnboardingStartScreen
import com.maomei.petchatapp.ui.onboarding.OnboardingViewModel
import com.maomei.petchatapp.ui.onboarding.PersonalitySettingsScreen
import com.maomei.petchatapp.ui.onboarding.PetInfoInputScreen
import com.maomei.petchatapp.ui.onboarding.PhotoConfirmScreen
import com.maomei.petchatapp.ui.onboarding.PhotoPickScreen
import com.maomei.petchatapp.ui.onboarding.SpeciesSelectScreen
import com.maomei.petchatapp.ui.settings.SettingsScreen
import com.maomei.petchatapp.ui.splash.SplashScreen

@Composable
fun PetChatNavGraph(factory: AppViewModelFactory) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                factory = factory,
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING_GRAPH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        navigation(startDestination = Routes.ONBOARDING_START, route = Routes.ONBOARDING_GRAPH) {

            composable(Routes.ONBOARDING_START) { backStackEntry ->
                val onboardingViewModel = rememberOnboardingViewModel(navController, backStackEntry, factory)
                OnboardingStartScreen(
                    onStartRegistration = { navController.navigate(Routes.SPECIES_SELECT) },
                    onSkipForNow = {
                        onboardingViewModel.createDefaultProfileAndProceed {
                            navController.navigate(Routes.CHAT) {
                                popUpTo(Routes.ONBOARDING_GRAPH) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.SPECIES_SELECT) { backStackEntry ->
                val onboardingViewModel = rememberOnboardingViewModel(navController, backStackEntry, factory)
                val state by onboardingViewModel.uiState.collectAsState()
                SpeciesSelectScreen(
                    selected = state.species,
                    onSelect = onboardingViewModel::selectSpecies,
                    onNext = { navController.navigate(Routes.PET_INFO) }
                )
            }

            composable(Routes.PET_INFO) { backStackEntry ->
                val onboardingViewModel = rememberOnboardingViewModel(navController, backStackEntry, factory)
                val state by onboardingViewModel.uiState.collectAsState()
                PetInfoInputScreen(
                    name = state.name,
                    ownerCallName = state.ownerCallName,
                    firstPerson = state.firstPerson,
                    onNameChange = onboardingViewModel::updateName,
                    onOwnerCallNameChange = onboardingViewModel::updateOwnerCallName,
                    onFirstPersonChange = onboardingViewModel::updateFirstPerson,
                    onNext = { navController.navigate(Routes.PHOTO_PICK) }
                )
            }

            composable(Routes.PHOTO_PICK) { backStackEntry ->
                val onboardingViewModel = rememberOnboardingViewModel(navController, backStackEntry, factory)
                PhotoPickScreen(
                    photosFlow = onboardingViewModel.photos,
                    isGooglePhotosAvailable = onboardingViewModel.isGooglePhotosAvailable,
                    googlePickerState = onboardingViewModel.googlePickerState,
                    onStartGooglePicker = onboardingViewModel::startGooglePhotoPicker,
                    onAuthorizationResolved = onboardingViewModel::onAuthorizationResolved,
                    onPollForSelection = onboardingViewModel::pollForSelection,
                    onDismissGoogleError = onboardingViewModel::dismissGooglePickerError,
                    onPhotosPicked = onboardingViewModel::importPhotos,
                    onNext = { navController.navigate(Routes.PHOTO_CONFIRM) }
                )
            }

            composable(Routes.PHOTO_CONFIRM) { backStackEntry ->
                val onboardingViewModel = rememberOnboardingViewModel(navController, backStackEntry, factory)
                val state by onboardingViewModel.uiState.collectAsState()
                val photos by onboardingViewModel.photos.collectAsState()
                PhotoConfirmScreen(
                    photos = photos,
                    mainPhotoId = state.mainPhotoId,
                    errorMessage = state.photoErrorMessage,
                    onAddPhotos = onboardingViewModel::importPhotos,
                    onDeletePhoto = onboardingViewModel::deletePhoto,
                    onSetMainPhoto = onboardingViewModel::setMainPhoto,
                    onNext = {
                        if (onboardingViewModel.validatePhotosOrShowError()) {
                            navController.navigate(Routes.PERSONALITY)
                        }
                    }
                )
            }

            composable(Routes.PERSONALITY) { backStackEntry ->
                val onboardingViewModel = rememberOnboardingViewModel(navController, backStackEntry, factory)
                val state by onboardingViewModel.uiState.collectAsState()
                PersonalitySettingsScreen(
                    species = state.species,
                    personality = state.personality,
                    replyLength = state.replyLength,
                    useEmoji = state.useEmoji,
                    ownerCallName = state.ownerCallName,
                    firstPerson = state.firstPerson,
                    onPersonalityChange = onboardingViewModel::updatePersonality,
                    onReplyLengthChange = onboardingViewModel::updateReplyLength,
                    onUseEmojiChange = onboardingViewModel::updateUseEmoji,
                    onOwnerCallNameChange = onboardingViewModel::updateOwnerCallName,
                    onFirstPersonChange = onboardingViewModel::updateFirstPerson,
                    onNext = { navController.navigate(Routes.CONFIRM) }
                )
            }

            composable(Routes.CONFIRM) { backStackEntry ->
                val onboardingViewModel = rememberOnboardingViewModel(navController, backStackEntry, factory)
                val state by onboardingViewModel.uiState.collectAsState()
                val photos by onboardingViewModel.photos.collectAsState()
                val mainPhotoPath = photos.find { it.id == state.mainPhotoId }?.localPath
                    ?: photos.firstOrNull()?.localPath
                ConfirmationScreen(
                    mainPhotoPath = mainPhotoPath,
                    name = state.name,
                    species = state.species,
                    personality = state.personality,
                    replyLength = state.replyLength,
                    useEmoji = state.useEmoji,
                    ownerCallName = state.ownerCallName,
                    firstPerson = state.firstPerson,
                    isSaving = state.isSaving,
                    onConfirm = {
                        onboardingViewModel.completeRegistration {
                            navController.navigate(Routes.CHAT) {
                                popUpTo(Routes.ONBOARDING_GRAPH) { inclusive = true }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.CHAT) {
            ChatScreen(
                factory = factory,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                factory = factory,
                onBack = { navController.popBackStack() },
                onResetComplete = {
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

/** onboarding_graph 内のすべての画面で共有する [OnboardingViewModel] を取得する。 */
@Composable
private fun rememberOnboardingViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    factory: AppViewModelFactory
): OnboardingViewModel {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(Routes.ONBOARDING_GRAPH)
    }
    return viewModel(parentEntry, factory = factory)
}

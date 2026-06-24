package com.hearboost.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hearboost.ui.screens.*

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING_MIC = "onboarding_mic"
    const val ONBOARDING_HEADPHONES = "onboarding_headphones"
    const val HOME = "home"
    const val AUDIO_SETTINGS = "audio_settings"
    const val APP_SETTINGS = "app_settings"
    const val HEARING_PROFILES = "hearing_profiles"
    const val HEADPHONE_MANAGER = "headphone_manager"
}

@Composable
fun HearBoostNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.ONBOARDING_MIC) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING_MIC) {
            OnboardingMicScreen(
                onAllow = {
                    navController.navigate(Routes.ONBOARDING_HEADPHONES)
                },
                onSkip = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING_HEADPHONES) {
            OnboardingHeadphoneScreen(
                onContinue = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.APP_SETTINGS) },
                onNavigateToProfiles = { navController.navigate(Routes.HEARING_PROFILES) },
                onNavigateToHeadphones = { navController.navigate(Routes.HEADPHONE_MANAGER) },
                onNavigateToAudioSettings = { navController.navigate(Routes.AUDIO_SETTINGS) }
            )
        }

        composable(Routes.AUDIO_SETTINGS) {
            AudioSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.APP_SETTINGS) {
            AppSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HEARING_PROFILES) {
            HearingProfilesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HEADPHONE_MANAGER) {
            HeadphoneManagerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

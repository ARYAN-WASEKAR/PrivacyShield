package com.privacyshield.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.privacyshield.app.ui.PrivacyViewModel
import com.privacyshield.app.ui.analysis.AnalysisScreen
import com.privacyshield.app.ui.editor.EditorScreen
import com.privacyshield.app.ui.home.HomeScreen
import com.privacyshield.app.ui.preview.SafePreviewScreen
import com.privacyshield.app.ui.scanner.CameraScannerScreen
import com.privacyshield.app.ui.settings.SettingsScreen

@Composable
fun PrivacyNavGraph(
    navController: NavHostController,
    viewModel: PrivacyViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAnalyzing) {
        if (uiState.isAnalyzing && navController.currentDestination?.route != Screen.Analysis.route) {
            navController.navigate(Screen.Analysis.route)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) }
            )
        }

        composable(Screen.Scanner.route) {
            CameraScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAnalysis = {
                    navController.navigate(Screen.Analysis.route) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Analysis.route) {
            AnalysisScreen(
                viewModel = viewModel,
                onAnalysisFinished = {
                    navController.navigate(Screen.Editor.route) {
                        popUpTo(Screen.Analysis.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Editor.route) {
            EditorScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToPreview = { navController.navigate(Screen.Preview.route) }
            )
        }

        composable(Screen.Preview.route) {
            SafePreviewScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

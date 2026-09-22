package com.privacyshield.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.privacyshield.app.sharing.ShareReceiver
import com.privacyshield.app.ui.PrivacyViewModel
import com.privacyshield.app.ui.navigation.PrivacyNavGraph
import com.privacyshield.app.ui.navigation.Screen
import com.privacyshield.app.ui.theme.PrivacyShieldTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)

        setContent {
            PrivacyShieldTheme {
                val navController = rememberNavController()
                PrivacyNavGraph(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val imageUri = ShareReceiver.extractImageUri(intent)
        if (imageUri != null) {
            viewModel.processImageUri(imageUri) {
                // Navigated when UI starts
            }
        }
    }
}

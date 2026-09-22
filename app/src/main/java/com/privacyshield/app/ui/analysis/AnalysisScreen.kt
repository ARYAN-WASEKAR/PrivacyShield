package com.privacyshield.app.ui.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.app.ui.PrivacyViewModel
import com.privacyshield.app.ui.theme.DarkBackground
import com.privacyshield.app.ui.theme.DarkBorder
import com.privacyshield.app.ui.theme.DarkSurface
import com.privacyshield.app.ui.theme.EmeraldPrimary
import com.privacyshield.app.ui.theme.RoseAlert
import com.privacyshield.app.ui.theme.TextMuted
import com.privacyshield.app.ui.theme.TextPrimary
import com.privacyshield.app.ui.theme.TextSecondary

@Composable
fun AnalysisScreen(
    viewModel: PrivacyViewModel,
    onAnalysisFinished: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == PrivacyViewModel.AnalysisStep.COMPLETE) {
            onAnalysisFinished()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Radar Shield
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(radarScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                        .border(1.dp, EmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Analyzing locally…",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "100% on-device neural & heuristic privacy scan",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Step Checklist Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnalysisStepRow(
                        title = "OCR Text Recognition",
                        isCompleted = uiState.currentStep.ordinal > PrivacyViewModel.AnalysisStep.OCR.ordinal,
                        isActive = uiState.currentStep == PrivacyViewModel.AnalysisStep.OCR
                    )

                    AnalysisStepRow(
                        title = "Sensitive PII Scanning (Aadhaar / PAN / Phone)",
                        isCompleted = uiState.currentStep.ordinal > PrivacyViewModel.AnalysisStep.PII_DETECTION.ordinal,
                        isActive = uiState.currentStep == PrivacyViewModel.AnalysisStep.PII_DETECTION
                    )

                    AnalysisStepRow(
                        title = "Biometric Face Detection",
                        isCompleted = uiState.currentStep.ordinal > PrivacyViewModel.AnalysisStep.FACE_DETECTION.ordinal,
                        isActive = uiState.currentStep == PrivacyViewModel.AnalysisStep.FACE_DETECTION
                    )

                    AnalysisStepRow(
                        title = "EXIF Metadata Check",
                        isCompleted = uiState.currentStep.ordinal > PrivacyViewModel.AnalysisStep.METADATA_CLEAN.ordinal,
                        isActive = uiState.currentStep == PrivacyViewModel.AnalysisStep.METADATA_CLEAN
                    )
                }
            }

            // Error Notice
            AnimatedVisibility(visible = uiState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = RoseAlert
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = RoseAlert,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)
                    ) {
                        Text(text = "Go Back", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisStepRow(
    title: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    val indicatorColor by animateColorAsState(
        targetValue = when {
            isCompleted -> EmeraldPrimary
            isActive -> EmeraldPrimary
            else -> TextMuted.copy(alpha = 0.3f)
        },
        label = "stepColor"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isCompleted) EmeraldPrimary else Color.Transparent)
                .border(1.5.dp, indicatorColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = EmeraldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive || isCompleted) TextPrimary else TextMuted
            )
        )
    }
}

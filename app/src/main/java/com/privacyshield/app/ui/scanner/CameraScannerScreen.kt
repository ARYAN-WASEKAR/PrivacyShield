package com.privacyshield.app.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.privacyshield.app.camera.CameraManager
import com.privacyshield.app.camera.StabilityDetector
import com.privacyshield.app.sensors.MotionSensor
import com.privacyshield.app.ui.PrivacyViewModel
import com.privacyshield.app.ui.common.HoldSteadyPill
import com.privacyshield.app.ui.theme.DarkBackground
import com.privacyshield.app.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun CameraScannerScreen(
    viewModel: PrivacyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Camera Permission Needed",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PrivacyShield requires camera access to scan documents locally without cloud upload.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(text = "Grant Permission", color = Color.Black)
                }
            }
        }
        return
    }

    val motionSensor = remember { MotionSensor(context) }
    val stabilityDetector = remember { StabilityDetector(motionSensor, coroutineScope) }
    val stabilityState by stabilityDetector.state.collectAsState()

    val cameraManager = remember { CameraManager(context) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        motionSensor.startListening()
        onDispose {
            motionSensor.stopListening()
            cameraManager.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX Viewfinder
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { pv ->
                    previewView = pv
                    cameraManager.startCamera(lifecycleOwner, pv)
                }
            }
        )

        // Document Guide Frame Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameWidth = size.width * 0.86f
            val frameHeight = size.height * 0.62f
            val left = (size.width - frameWidth) / 2f
            val top = (size.height - frameHeight) / 2.3f

            // Frame border with corner accents
            drawRoundRect(
                color = if (stabilityState.isStable) Color(0xFF10B981) else Color(0x88FFFFFF),
                topLeft = Offset(left, top),
                size = Size(frameWidth, frameHeight),
                cornerRadius = CornerRadius(20.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 42.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = {
                        cameraManager.toggleFlash { state -> isFlashOn = state }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isFlashOn) EmeraldPrimary else Color(0x88000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) Color.Black else Color.White
                    )
                }

                IconButton(
                    onClick = {
                        previewView?.let { pv ->
                            cameraManager.toggleLensFacing(lifecycleOwner, pv)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Bar with Stability Pill and Shutter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Motion / Hold-Steady Feedback Pill
            HoldSteadyPill(
                isStable = stabilityState.isStable,
                statusText = stabilityState.statusText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Shutter Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(
                        3.dp,
                        if (stabilityState.isStable) EmeraldPrimary else Color.White.copy(alpha = 0.8f),
                        CircleShape
                    )
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (stabilityState.isStable) EmeraldPrimary else Color.White)
                    .clickable(enabled = !isCapturing) {
                        isCapturing = true
                        coroutineScope.launch {
                            try {
                                val capturedBitmap = cameraManager.capturePhoto()
                                viewModel.processCapturedBitmap(capturedBitmap, onNavigateToAnalysis)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isCapturing = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    Text(
                        text = "…",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

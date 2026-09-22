package com.privacyshield.app.ui.editor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.app.data.RedactionMode
import com.privacyshield.app.data.SensitiveRegion
import com.privacyshield.app.data.SensitiveType
import com.privacyshield.app.ui.PrivacyViewModel
import com.privacyshield.app.ui.common.PrivacyExplainerDialog
import com.privacyshield.app.ui.theme.CyanAccent
import com.privacyshield.app.ui.theme.DarkBackground
import com.privacyshield.app.ui.theme.DarkBorder
import com.privacyshield.app.ui.theme.DarkSurface
import com.privacyshield.app.ui.theme.DarkSurfaceElevated
import com.privacyshield.app.ui.theme.DarkSurfaceHighlight
import com.privacyshield.app.ui.theme.EmeraldPrimary
import com.privacyshield.app.ui.theme.RoseAlert
import com.privacyshield.app.ui.theme.TextMuted
import com.privacyshield.app.ui.theme.TextPrimary
import com.privacyshield.app.ui.theme.TextSecondary

@Composable
fun EditorScreen(
    viewModel: PrivacyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val detectionResult = uiState.detectionResult
    val originalBitmap = uiState.originalBitmap
    val redactedBitmap = uiState.redactedBitmap

    if (detectionResult == null || originalBitmap == null || redactedBitmap == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "No active document found", color = TextSecondary)
            }
        }
        return
    }

    // Explainer Dialog
    uiState.activeExplainingRegion?.let { region ->
        PrivacyExplainerDialog(
            region = region,
            onDismiss = { viewModel.showExplanation(null) }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                // Summary Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${detectionResult.totalCount} sensitive items detected",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Demo Status Indicators Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DemoStatusPill(icon = Icons.Default.CloudOff, text = "Processed locally")
                DemoStatusPill(icon = Icons.Default.Lock, text = "Original preserved")
                DemoStatusPill(icon = Icons.Default.Security, text = "Metadata cleaned")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Redaction Style Selector Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                RedactionModeTab(
                    title = "Blackout",
                    isSelected = uiState.globalRedactionMode == RedactionMode.BLACKOUT,
                    onClick = { viewModel.setGlobalRedactionMode(RedactionMode.BLACKOUT) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                RedactionModeTab(
                    title = "Pixelate",
                    isSelected = uiState.globalRedactionMode == RedactionMode.PIXELATE,
                    onClick = { viewModel.setGlobalRedactionMode(RedactionMode.PIXELATE) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                RedactionModeTab(
                    title = "Blur",
                    isSelected = uiState.globalRedactionMode == RedactionMode.BLUR,
                    onClick = { viewModel.setGlobalRedactionMode(RedactionMode.BLUR) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Image Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                RedactionCanvas(
                    redactedBitmap = redactedBitmap,
                    originalBitmap = originalBitmap,
                    regions = detectionResult.regions,
                    onRegionTapped = { region -> viewModel.showExplanation(region) },
                    onCustomRegionAdded = { rect -> viewModel.addCustomRegion(rect) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Factual Privacy Check Risk Summary Card
            PrivacyCheckSummaryCard(regions = detectionResult.regions)

            Spacer(modifier = Modifier.height(8.dp))

            // Detected Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(detectionResult.regions, key = { it.id }) { region ->
                    DetectionItemCard(
                        region = region,
                        onToggle = { viewModel.toggleRegion(region.id) },
                        onInfoClick = { viewModel.showExplanation(region) },
                        onDeleteCustom = if (region.type == SensitiveType.CUSTOM) {
                            { viewModel.removeCustomRegion(region.id) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action Bar: PROTECT Button
            Button(
                onClick = {
                    viewModel.generateSafeCopy {
                        onNavigateToPreview()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "PROTECT",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PrivacyCheckSummaryCard(regions: List<SensitiveRegion>) {
    val highRisk = regions.filter {
        it.type in listOf(SensitiveType.AADHAAR, SensitiveType.PAN, SensitiveType.BANK_ACCOUNT, SensitiveType.FACE)
    }.map { it.type.title }.distinct()

    val mediumRisk = regions.filter {
        it.type in listOf(SensitiveType.PHONE, SensitiveType.EMAIL, SensitiveType.UPI)
    }.map { it.type.title }.distinct()

    val lowRisk = regions.filter {
        it.type == SensitiveType.CUSTOM
    }.map { it.type.title }.distinct()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "PRIVACY CHECK",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = EmeraldPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (highRisk.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔴 HIGH RISK: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = RoseAlert
                    )
                    Text(
                        text = highRisk.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }

            if (mediumRisk.isNotEmpty()) {
                if (highRisk.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🟠 MEDIUM RISK: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF97316)
                    )
                    Text(
                        text = mediumRisk.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }

            if (lowRisk.isNotEmpty()) {
                if (highRisk.isNotEmpty() || mediumRisk.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🟡 LOW RISK: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = CyanAccent
                    )
                    Text(
                        text = lowRisk.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoStatusPill(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceHighlight)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun RedactionModeTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) EmeraldPrimary else DarkSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) EmeraldPrimary else DarkBorder,
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Color.Black else TextSecondary
        )
    }
}

@Composable
private fun DetectionItemCard(
    region: SensitiveRegion,
    onToggle: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteCustom: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (region.isEnabled) DarkSurface else DarkSurfaceElevated.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (region.isEnabled) DarkBorder else DarkBorder.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = region.isEnabled,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldPrimary,
                    uncheckedColor = TextMuted,
                    checkmarkColor = Color.Black
                )
            )

            Icon(
                imageVector = getIconForType(region.type),
                contentDescription = null,
                tint = if (region.isEnabled) EmeraldPrimary else TextMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = region.type.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (region.isEnabled) TextPrimary else TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(region.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent
                    )
                }
                Text(
                    text = region.maskedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    color = if (region.isEnabled) TextSecondary else TextMuted
                )
            }

            if (onDeleteCustom != null) {
                IconButton(onClick = onDeleteCustom, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = RoseAlert,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Why detected?",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun getIconForType(type: SensitiveType): ImageVector {
    return when (type) {
        SensitiveType.AADHAAR -> Icons.Default.Badge
        SensitiveType.PAN -> Icons.Default.CreditCard
        SensitiveType.PHONE -> Icons.Default.Phone
        SensitiveType.EMAIL -> Icons.Default.Email
        SensitiveType.UPI -> Icons.Default.AccountBalanceWallet
        SensitiveType.BANK_ACCOUNT -> Icons.Default.AccountBalance
        SensitiveType.FACE -> Icons.Default.Face
        SensitiveType.CUSTOM -> Icons.Default.CropFree
    }
}

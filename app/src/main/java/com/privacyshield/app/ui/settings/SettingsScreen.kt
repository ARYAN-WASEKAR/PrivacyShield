package com.privacyshield.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.app.ui.theme.DarkBackground
import com.privacyshield.app.ui.theme.DarkBorder
import com.privacyshield.app.ui.theme.DarkSurface
import com.privacyshield.app.ui.theme.DarkSurfaceElevated
import com.privacyshield.app.ui.theme.EmeraldPrimary
import com.privacyshield.app.ui.theme.TextMuted
import com.privacyshield.app.ui.theme.TextPrimary
import com.privacyshield.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Privacy & Guarantees",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GuaranteeCard(
                icon = Icons.Default.CloudOff,
                title = "100% On-Device Processing",
                description = "All OCR, PII detection, face recognition, and bitmap redactions run locally on your phone using Google ML Kit. Zero bytes of your photos are ever uploaded to any cloud server."
            )

            Spacer(modifier = Modifier.height(14.dp))

            GuaranteeCard(
                icon = Icons.Default.Lock,
                title = "Zero Modified Originals",
                description = "Your original images are never overwritten or deleted. PrivacyShield only creates a clean, sanitized copy in an app-private cache to share."
            )

            Spacer(modifier = Modifier.height(14.dp))

            GuaranteeCard(
                icon = Icons.Default.Security,
                title = "Metadata & EXIF Auto-Scrubbing",
                description = "GPS coordinates, device models, software fingerprints, serial numbers, and creation timestamps are automatically scrubbed from exported files."
            )

            Spacer(modifier = Modifier.height(14.dp))

            GuaranteeCard(
                icon = Icons.Default.Fingerprint,
                title = "Supported PII Detection Rules",
                description = "• Indian Aadhaar Numbers (12 digits, 4-4-4 spacing, Verhoeff checksum)\n• PAN Card Numbers (10 chars, entity status verification)\n• Indian & International Mobile Numbers\n• RFC-compliant Email Addresses\n• UPI Handles & Virtual Payment Addresses\n• Bank Account Numbers (with banking context cues)\n• Human Facial Geometry"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PrivacyShield v1.0.0 • Hackathon Prototype",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun GuaranteeCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

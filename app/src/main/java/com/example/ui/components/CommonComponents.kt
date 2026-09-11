package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AemsNotification
import com.example.model.M_USER
import com.example.model.TransactionStatus
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import java.text.NumberFormat
import java.util.Locale

fun formatIdr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.GERMAN)
    return "Rp ${formatter.format(amount)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AemsTopAppBar(
    currentScreen: AppScreen,
    currentUser: M_USER,
    unreadNotificationCount: Int,
    onOpenNavDrawer: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenRoleSelector: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AEMS PSI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { onOpenRoleSelector() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("role_pill_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentUser.role.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = AccentSky
                            )
                        }
                    }
                }
                Text(
                    text = "PT Prasad Seeds Indonesia • ${currentScreen.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenNavDrawer,
                modifier = Modifier.testTag("open_sidebar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Buka Menu",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenNotifications,
                modifier = Modifier.testTag("notification_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge(containerColor = AccentRose) {
                                Text("$unreadNotificationCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifikasi",
                        tint = TextPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkSurface,
            titleContentColor = TextPrimary
        )
    )
}

@Composable
fun StatusBadge(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        TransactionStatus.DRAFT -> Triple(StatusDraftBg, StatusDraftText, Icons.Default.Edit)
        TransactionStatus.PENDING_REVIEW -> Triple(StatusPendingBg, StatusPendingText, Icons.Default.HourglassEmpty)
        TransactionStatus.NEED_REVISION -> Triple(StatusRejectedBg, StatusRejectedText, Icons.Default.Replay)
        TransactionStatus.PENDING_APPROVAL -> Triple(Color(0xFF854D0E), Color(0xFFFEF08A), Icons.Default.Approval)
        TransactionStatus.APPROVED -> Triple(StatusApprovedBg, StatusApprovedText, Icons.Default.CheckCircle)
        TransactionStatus.REJECTED -> Triple(StatusRejectedBg, StatusRejectedText, Icons.Default.Cancel)
        TransactionStatus.PENDING_DIGITAL_SIGNATURE -> Triple(Color(0xFF581C87), Color(0xFFE9D5FF), Icons.Default.Draw)
        TransactionStatus.READY_FOR_FINANCE -> Triple(Color(0xFF1E3A8A), Color(0xFFBFDBFE), Icons.Default.Verified)
        TransactionStatus.RECEIVED_BY_FINANCE -> Triple(Color(0xFF1D4ED8), Color(0xFFDBEAFE), Icons.Default.AccountBalance)
        TransactionStatus.PROCESSING -> Triple(Color(0xFF0369A1), Color(0xFFBAE6FD), Icons.Default.Sync)
        TransactionStatus.PAID -> Triple(Color(0xFF047857), Color(0xFFA7F3D0), Icons.Default.Payments)
        TransactionStatus.SETTLEMENT_PENDING -> Triple(Color(0xFFB45309), Color(0xFFFDE68A), Icons.Default.ReceiptLong)
        TransactionStatus.SETTLEMENT_VERIFIED -> Triple(Color(0xFF065F46), Color(0xFF6EE7B7), Icons.Default.AssignmentTurnedIn)
        TransactionStatus.CLOSED -> Triple(StatusClosedBg, StatusClosedText, Icons.Default.Archive)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
    }
}

/**
 * Visual Progress Stepper for the 13-stage workflow
 */
@Composable
fun WorkflowProgressStepper(currentStatus: TransactionStatus, modifier: Modifier = Modifier) {
    val stages = listOf(
        "1. Request",
        "2. Lampiran",
        "3. System ID",
        "4. Review EA",
        "5. Approval",
        "6. Dokumen",
        "7. E-Sign",
        "8. Finance Gate",
        "9. Pembayaran",
        "10. Settlement",
        "11. Verifikasi",
        "12. Filing",
        "13. Closed"
    )

    val currentStepIndex = when (currentStatus) {
        TransactionStatus.DRAFT -> 0
        TransactionStatus.PENDING_REVIEW -> 3
        TransactionStatus.NEED_REVISION -> 3
        TransactionStatus.PENDING_APPROVAL -> 4
        TransactionStatus.APPROVED -> 5
        TransactionStatus.REJECTED -> 4
        TransactionStatus.PENDING_DIGITAL_SIGNATURE -> 6
        TransactionStatus.READY_FOR_FINANCE -> 7
        TransactionStatus.RECEIVED_BY_FINANCE -> 8
        TransactionStatus.PROCESSING -> 8
        TransactionStatus.PAID -> 8
        TransactionStatus.SETTLEMENT_PENDING -> 9
        TransactionStatus.SETTLEMENT_VERIFIED -> 10
        TransactionStatus.CLOSED -> 12
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROGRESS WORKFLOW 13 TAHAP",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldLight
                )
                Text(
                    text = "Tahap ${currentStepIndex + 1} dari ${stages.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable horizontal stepper line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEachIndexed { index, stageName ->
                    val isCompleted = index < currentStepIndex
                    val isCurrent = index == currentStepIndex
                    val isUpcoming = index > currentStepIndex

                    val circleColor = when {
                        isCompleted -> EmeraldPrimary
                        isCurrent -> AccentSky
                        else -> DarkBorder
                    }

                    val textColor = when {
                        isCompleted -> EmeraldLight
                        isCurrent -> Color.White
                        else -> TextMuted
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(82.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(circleColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCurrent) DarkBackground else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stageName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = textColor,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (index < stages.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(if (index < currentStepIndex) EmeraldPrimary else DarkBorder)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subValue: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            if (subValue != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun NotificationDialog(
    notifications: List<AemsNotification>,
    onDismiss: () -> Unit,
    onSelectTransaction: (String) -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = EmeraldPrimary)
            }
        },
        dismissButton = {
            if (notifications.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("Bersihkan Semua", color = TextMuted)
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = EmeraldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pemberitahuan Sistem", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada notifikasi baru.", color = TextMuted)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    notifications.forEach { notif ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onDismiss()
                                    onSelectTransaction(notif.transactionId)
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldLight
                                    )
                                    Text(
                                        text = notif.timestamp.takeLast(8),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ID: ${notif.transactionId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentSky
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

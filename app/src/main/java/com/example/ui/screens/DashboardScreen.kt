package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.T_TRANSACTION
import com.example.model.TransactionStatus
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatIdr
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.DashboardStats

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    recentTransactions: List<T_TRANSACTION>,
    onNavigate: (AppScreen) -> Unit,
    onSelectTransaction: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Corporate Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldDark.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AEMS PSI Enterprise",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Advance & Expense Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PT Prasad Seeds Indonesia • Sistem monitoring dan alur persetujuan terpadu",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = { onNavigate(AppScreen.NEW_REQUEST) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dashboard_new_request_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajukan", color = Color.White)
                    }
                }
            }
        }

        // Financial KPI Cards
        item {
            Text(
                text = "RINGKASAN KEUANGAN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Advance",
                    value = formatIdr(stats.totalAdvanceAmount),
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Actual Expense",
                    value = formatIdr(stats.totalActualExpense),
                    icon = Icons.Default.Receipt,
                    accentColor = AccentSky,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            StatCard(
                title = "Outstanding Settlement (Belum Terselesaikan)",
                value = formatIdr(stats.outstandingSettlementAmount),
                subValue = "${stats.settlementPendingCount} transaksi menunggu penyelesaian",
                icon = Icons.Default.PendingActions,
                accentColor = AccentAmber,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Status Workflow Cards Grid
        item {
            Text(
                text = "STATUS WORKFLOW TRANSAKSI",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total Transaksi",
                        value = "${stats.totalTransactions}",
                        icon = Icons.Default.ListAlt,
                        accentColor = AccentSky,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.TRANSACTIONS) }
                    )
                    StatCard(
                        title = "Pending Review",
                        value = "${stats.pendingReviewCount}",
                        icon = Icons.Default.RateReview,
                        accentColor = AccentAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.REVIEW) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Pending Approval",
                        value = "${stats.pendingApprovalCount}",
                        icon = Icons.Default.Approval,
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.APPROVAL) }
                    )
                    StatCard(
                        title = "Pending Signature",
                        value = "${stats.pendingSignatureCount}",
                        icon = Icons.Default.Draw,
                        accentColor = AccentPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.TRANSACTIONS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Ready for Finance",
                        value = "${stats.readyForFinanceCount}",
                        icon = Icons.Default.AccountBalance,
                        accentColor = EmeraldLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.FINANCE) }
                    )
                    StatCard(
                        title = "Processing / Paid",
                        value = "${stats.processingCount}",
                        icon = Icons.Default.Payments,
                        accentColor = AccentSky,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.FINANCE) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Settlement Pending",
                        value = "${stats.settlementPendingCount}",
                        icon = Icons.Default.AssignmentReturn,
                        accentColor = AccentAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.SETTLEMENT) }
                    )
                    StatCard(
                        title = "Closed (Selesai)",
                        value = "${stats.closedCount}",
                        icon = Icons.Default.Archive,
                        accentColor = StatusClosedText,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.DOCUMENTS) }
                    )
                }
            }
        }

        // Visual Department Distribution Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distribusi Pengajuan per Departemen",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val deptEntries = stats.deptDistribution.entries.toList()
                    if (deptEntries.isEmpty()) {
                        Text("Belum ada data distribusi.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    } else {
                        val maxCount = deptEntries.maxOfOrNull { it.value } ?: 1
                        deptEntries.forEach { (dept, count) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dept,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$count transaksi",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(DarkSurfaceElevated)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((count.toFloat() / maxCount.toFloat()).coerceIn(0.05f, 1f))
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(EmeraldPrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSAKSI TERBARU",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextSecondary
                )
                TextButton(onClick = { onNavigate(AppScreen.TRANSACTIONS) }) {
                    Text("Lihat Semua", color = EmeraldPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        items(recentTransactions.take(6)) { tx ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable { onSelectTransaction(tx.transactionId) }
                    .testTag("transaction_card_${tx.transactionId}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tx.transactionId,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentSky
                        )
                        StatusBadge(status = tx.currentStatus)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tx.purpose,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${tx.requester} • ${tx.department}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = tx.requestDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = formatIdr(tx.amount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldLight
                        )
                    }
                }
            }
        }
    }
}

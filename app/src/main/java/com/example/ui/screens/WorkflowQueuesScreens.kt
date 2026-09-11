package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.T_TRANSACTION
import com.example.model.TransactionStatus
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatIdr
import com.example.ui.theme.*

@Composable
fun ReviewScreen(
    transactions: List<T_TRANSACTION>,
    onSelectTransaction: (String) -> Unit
) {
    val reviewQueue = transactions.filter {
        it.currentStatus == TransactionStatus.PENDING_REVIEW || it.currentStatus == TransactionStatus.NEED_REVISION
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RateReview, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANTREAN TINJAUAN EA / PEMILIK PROSES",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Periksa kelengkapan data pengajuan, tujuan, nominal, departemen, cost center, dan dokumen pendukung sebelum diteruskan ke Approval Matrix.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "MENUNGGU TINJAUAN (${reviewQueue.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        if (reviewQueue.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Semua pengajuan telah ditinjau oleh EA.", color = TextMuted)
                }
            }
        } else {
            items(reviewQueue) { tx ->
                TransactionQueueCard(tx = tx, onClick = { onSelectTransaction(tx.transactionId) })
            }
        }
    }
}

@Composable
fun ApprovalScreen(
    transactions: List<T_TRANSACTION>,
    onSelectTransaction: (String) -> Unit
) {
    val approvalQueue = transactions.filter {
        it.currentStatus == TransactionStatus.PENDING_APPROVAL
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Approval, contentDescription = null, tint = AccentAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANTREAN PERSETUJUAN (APPROVAL MATRIX)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sistem menentukan approver berdasarkan departemen, tipe transaksi, dan rentang nominal pengajuan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "PERLU PERSETUJUAN (${approvalQueue.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        if (approvalQueue.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada pengajuan yang menunggu approval saat ini.", color = TextMuted)
                }
            }
        } else {
            items(approvalQueue) { tx ->
                TransactionQueueCard(tx = tx, onClick = { onSelectTransaction(tx.transactionId) })
            }
        }
    }
}

@Composable
fun FinanceScreen(
    transactions: List<T_TRANSACTION>,
    onSelectTransaction: (String) -> Unit
) {
    val financeQueue = transactions.filter {
        it.currentStatus == TransactionStatus.READY_FOR_FINANCE ||
                it.currentStatus == TransactionStatus.RECEIVED_BY_FINANCE ||
                it.currentStatus == TransactionStatus.PROCESSING
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FINANCE GATE & DISBURSEMENT",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transaksi yang telah lengkap, diapprove, dan ditandatangani secara digital siap diproses pembayarannya oleh Finance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "SIAP DIPROSES & DIBAYAR (${financeQueue.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        if (financeQueue.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Semua transaksi Finance telah diselesaikan.", color = TextMuted)
                }
            }
        } else {
            items(financeQueue) { tx ->
                TransactionQueueCard(tx = tx, onClick = { onSelectTransaction(tx.transactionId) })
            }
        }
    }
}

@Composable
fun SettlementScreen(
    transactions: List<T_TRANSACTION>,
    onSelectTransaction: (String) -> Unit
) {
    val settlementQueue = transactions.filter {
        it.currentStatus == TransactionStatus.PAID ||
                it.currentStatus == TransactionStatus.SETTLEMENT_PENDING ||
                it.currentStatus == TransactionStatus.SETTLEMENT_VERIFIED
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = AccentAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SETTLEMENT & REKONSILIASI BIAYA",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pemohon menginput actual expense & kwitansi. Sistem menghitung selisih (refund atau klaim tambahan). Finance melakukan verifikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "ANTREAN SETTLEMENT (${settlementQueue.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        if (settlementQueue.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada settlement aktif.", color = TextMuted)
                }
            }
        } else {
            items(settlementQueue) { tx ->
                TransactionQueueCard(tx = tx, onClick = { onSelectTransaction(tx.transactionId) })
            }
        }
    }
}

@Composable
fun TransactionQueueCard(tx: T_TRANSACTION, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("queue_card_${tx.transactionId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tx.transactionId,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentSky
                )
                StatusBadge(status = tx.currentStatus)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tx.purpose,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${tx.requester} • ${tx.department}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Dibutuhkan: ${tx.requiredDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Text(
                    text = formatIdr(tx.amount),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldLight
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkflowProgressStepper
import com.example.ui.components.formatIdr
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transaction: T_TRANSACTION,
    details: List<T_TRANSACTION_DETAIL>,
    approvalLogs: List<T_APPROVAL_LOG>,
    documents: List<T_DOCUMENT_LOG>,
    financeLog: T_FINANCE_LOG?,
    settlement: T_SETTLEMENT?,
    auditLogs: List<T_AUDIT_LOG>,
    currentUser: M_USER,
    onBack: () -> Unit,
    onReviewEA: (isPass: Boolean, comment: String) -> Unit,
    onProcessApproval: (isApproved: Boolean, comment: String) -> Unit,
    onSignDigital: () -> Unit,
    onUpdateFinance: (targetStatus: TransactionStatus, method: String?, ref: String?, remarks: String?) -> Unit,
    onSubmitSettlement: (actualExpense: Long, remarks: String, receiptName: String) -> Unit,
    onVerifySettlement: (isApproved: Boolean, comment: String) -> Unit,
    onCloseTransaction: () -> Unit
) {
    // Dialog states
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewIsPass by remember { mutableStateOf(true) }
    var reviewComment by remember { mutableStateOf("") }

    var showApprovalDialog by remember { mutableStateOf(false) }
    var approvalIsApproved by remember { mutableStateOf(true) }
    var approvalComment by remember { mutableStateOf("") }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("BANK TRANSFER MANDIRI") }
    var paymentRef by remember { mutableStateOf("TRF-PSI-2026-${(System.currentTimeMillis() % 10000)}") }
    var paymentRemarks by remember { mutableStateOf("Pembayaran diproses via Internet Banking Bisnis") }

    var showSettlementDialog by remember { mutableStateOf(false) }
    var actualExpenseInput by remember { mutableStateOf(transaction.amount.toString()) }
    var settlementRemarks by remember { mutableStateOf("") }
    var receiptName by remember { mutableStateOf("Kwitansi_Resmi_${transaction.transactionId}.pdf") }

    var showVerifySettlementDialog by remember { mutableStateOf(false) }
    var verifySettlementApproved by remember { mutableStateOf(true) }
    var verifySettlementComment by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.transactionId,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentSky
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = transaction.currentStatus)
                    }
                    Text(
                        text = "Dibuat: ${transaction.createdAt} • Update: ${transaction.updatedAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Visual 13-stage Workflow Progress
        item {
            WorkflowProgressStepper(currentStatus = transaction.currentStatus)
        }

        // Contextual Action Buttons depending on Status & Role
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AKSI WORKFLOW AKTIF (Peran: ${currentUser.role.label})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    when (transaction.currentStatus) {
                        TransactionStatus.DRAFT -> {
                            Button(
                                onClick = { onReviewEA(true, "Pengajuan diajukan dari status draft") },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SUBMIT KE EA / PROCESS OWNER")
                            }
                        }

                        TransactionStatus.PENDING_REVIEW -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        reviewIsPass = false
                                        showReviewDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRejectedText),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(StatusRejectedBg)
                                    )
                                ) {
                                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Minta Revisi")
                                }

                                Button(
                                    onClick = {
                                        reviewIsPass = true
                                        showReviewDialog = true
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Loloskan ke Approval")
                                }
                            }
                        }

                        TransactionStatus.NEED_REVISION -> {
                            Button(
                                onClick = { onReviewEA(true, "Pemohon telah merevisi data dan melampirkan berkas perbaikan.") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajukan Ulang Berkas Revisi")
                            }
                        }

                        TransactionStatus.PENDING_APPROVAL -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        approvalIsApproved = false
                                        showApprovalDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRejectedText),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(StatusRejectedBg)
                                    )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject (Tolak)")
                                }

                                Button(
                                    onClick = {
                                        approvalIsApproved = true
                                        showApprovalDialog = true
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve (Setujui)")
                                }
                            }
                        }

                        TransactionStatus.APPROVED -> {
                            Button(
                                onClick = onSignDigital,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Draw, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buat Dokumen & Lanjut ke Digital Signature")
                            }
                        }

                        TransactionStatus.PENDING_DIGITAL_SIGNATURE -> {
                            Button(
                                onClick = onSignDigital,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simulasikan Tanda Tangan Digital (E-Sign)")
                            }
                        }

                        TransactionStatus.READY_FOR_FINANCE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onUpdateFinance(TransactionStatus.RECEIVED_BY_FINANCE, null, null, "Finance menerima berkas fisik & digital") },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentSky),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Terima Transaksi")
                                }
                                Button(
                                    onClick = { onUpdateFinance(TransactionStatus.PROCESSING, "BANK TRANSFER MANDIRI", null, "Finance menyiapkan transfer") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Proses Pembayaran")
                                }
                            }
                        }

                        TransactionStatus.RECEIVED_BY_FINANCE, TransactionStatus.PROCESSING -> {
                            Button(
                                onClick = { showPaymentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Catat Pencairan Dana (Mark as PAID)")
                            }
                        }

                        TransactionStatus.PAID -> {
                            Button(
                                onClick = { showSettlementDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Input Settlement / Pengeluaran Riil")
                            }
                        }

                        TransactionStatus.SETTLEMENT_PENDING -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showSettlementDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Ubah Settlement")
                                }
                                Button(
                                    onClick = { showVerifySettlementDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verifikasi Finance")
                                }
                            }
                        }

                        TransactionStatus.SETTLEMENT_VERIFIED -> {
                            Button(
                                onClick = onCloseTransaction,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusClosedBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Document Filing & Tutup Transaksi (CLOSED)")
                            }
                        }

                        TransactionStatus.CLOSED -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Transaksi telah selesai dan diarsipkan secara permanen.", color = EmeraldLight, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        TransactionStatus.REJECTED -> {
                            Text(
                                text = "Pengajuan ditolak. Anda dapat membuat pengajuan baru jika diperlukan.",
                                color = StatusRejectedText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Section: Transaction & Requester Information
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INFORMASI TRANSAKSI & PEMOHON",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Transaction ID", transaction.transactionId, isHighlight = true)
                    InfoRow("Pemohon", "${transaction.requester} (${transaction.employeeId})")
                    InfoRow("Departemen", transaction.department)
                    InfoRow("Cost Center", transaction.costCenter)
                    InfoRow("Tipe Transaksi", transaction.transactionType)
                    InfoRow("Tanggal Pengajuan", transaction.requestDate)
                    InfoRow("Tanggal Dibutuhkan", transaction.requiredDate)
                    InfoRow("Tujuan (Purpose)", transaction.purpose)
                    InfoRow("Nominal Advance", formatIdr(transaction.amount), isPrice = true)
                }
            }
        }

        // Section: Expense Breakdown Details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RINCIAN ITEM BIAYA (EXPENSE INFORMATION)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (details.isEmpty()) {
                        Text(
                            text = "Rincian biaya utama: ${transaction.purpose}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        details.forEach { d ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = d.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Kategori: ${d.expenseCategory}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = formatIdr(d.estimatedAmount),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldLight
                                )
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = DarkBorder.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // Section: Supporting Documents & Filing Logs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DOKUMEN PENDUKUNG & FILING ARSIP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (documents.isEmpty()) {
                        Text("Belum ada dokumen yang terlampir.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    } else {
                        documents.forEach { doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (doc.documentType) {
                                        "SIGNATURE_CERT" -> Icons.Default.Verified
                                        "RECEIPT" -> Icons.Default.Receipt
                                        "PAYMENT_PROOF" -> Icons.Default.Payments
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.documentName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${doc.documentType} • Ref: ${doc.documentReference} • ${doc.uploadedAt.take(16)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = doc.documentStatus,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldLight
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Section: Finance Status Log
        if (financeLog != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentSky.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "STATUS KEUANGAN & PEMBAYARAN (FINANCE)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = AccentSky
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        InfoRow("Status Finance", financeLog.financeStatus, isHighlight = true)
                        financeLog.paymentDate?.let { InfoRow("Tanggal Pembayaran", it) }
                        financeLog.paymentMethod?.let { InfoRow("Metode Pembayaran", it) }
                        financeLog.paymentReference?.let { InfoRow("Nomor Referensi", it) }
                        financeLog.remarks?.let { InfoRow("Catatan Finance", it) }
                    }
                }
            }
        }

        // Section: Settlement Information & Reconciliation
        if (settlement != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SETTLEMENT & REKONSILIASI PENGELUARAN RIIL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = AccentAmber
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        InfoRow("Advance Amount (Uang Muka)", formatIdr(settlement.advanceAmount))
                        InfoRow("Actual Expense (Pengeluaran Riil)", formatIdr(settlement.actualExpense), isPrice = true)

                        val diff = settlement.difference
                        val diffTitle = if (diff >= 0) "Sisa Uang Muka (Refund ke Kas PSI)" else "Klaim Tambahan (Reimbursement)"
                        InfoRow(diffTitle, formatIdr(if (diff >= 0) diff else -diff), isHighlight = true)

                        InfoRow("Tanggal Settlement", settlement.settlementDate)
                        InfoRow("Status Verifikasi", settlement.verificationStatus)
                        InfoRow("Catatan Realisasi", settlement.remarks)
                    }
                }
            }
        }

        // Section: Approval & Review History
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RIWAYAT PERSETUJUAN (APPROVAL HISTORY)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (approvalLogs.isEmpty()) {
                        Text("Belum ada log approval formal.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    } else {
                        approvalLogs.forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (log.action == "APPROVED" || log.action == "REVIEW_PASS") EmeraldPrimary else AccentRose)
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${log.approver} • ${log.action}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = log.timestamp.take(16),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                    Text(
                                        text = log.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = DarkBorder.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }

        // Section: Audit Trail
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIT TRAIL LOG",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val txAudits = auditLogs.filter { it.transactionId == transaction.transactionId }
                    if (txAudits.isEmpty()) {
                        Text("Belum ada catatan audit log.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    } else {
                        txAudits.forEach { aud ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${aud.action} by ${aud.user}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = AccentSky
                                    )
                                    Text(
                                        text = aud.timestamp.take(16),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = "${aud.oldStatus} ➔ ${aud.newStatus}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldLight
                                )
                                Text(
                                    text = aud.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = DarkBorder.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog: Review EA
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showReviewDialog = false
                        onReviewEA(reviewIsPass, reviewComment)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (reviewIsPass) EmeraldPrimary else AccentRose
                    )
                ) {
                    Text(if (reviewIsPass) "Konfirmasi Loloskan" else "Kembalikan (Need Revision)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            title = {
                Text(if (reviewIsPass) "Tinjauan EA: Loloskan ke Approval" else "Tinjauan EA: Kembalikan untuk Revisi")
            },
            text = {
                Column {
                    Text(
                        text = if (reviewIsPass)
                            "Pastikan seluruh kelengkapan nominal, department, cost center, dan dokumen telah sesuai."
                        else
                            "Tuliskan catatan perbaikan atau dokumen pendukung yang kurang lengkap:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Catatan / Komentar EA") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // Dialog: Approval
    if (showApprovalDialog) {
        AlertDialog(
            onDismissRequest = { showApprovalDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showApprovalDialog = false
                        onProcessApproval(approvalIsApproved, approvalComment)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (approvalIsApproved) EmeraldPrimary else AccentRose
                    )
                ) {
                    Text(if (approvalIsApproved) "Konfirmasi Approve" else "Konfirmasi Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApprovalDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            title = {
                Text(if (approvalIsApproved) "Persetujuan Approver (Approve)" else "Tolak Pengajuan (Reject)")
            },
            text = {
                Column {
                    Text(
                        text = if (approvalIsApproved)
                            "Pengajuan akan disetujui dan sistem akan langsung membuat Form Standar Advance untuk tanda tangan digital."
                        else
                            "Alasan penolakan wajib disertakan untuk catatan pemohon:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = approvalComment,
                        onValueChange = { approvalComment = it },
                        label = { Text("Komentar Persetujuan / Alasan Penolakan") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // Dialog: Payment
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentDialog = false
                        onUpdateFinance(TransactionStatus.PAID, paymentMethod, paymentRef, paymentRemarks)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Konfirmasi Pembayaran Selesai")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            title = {
                Text("Pencairan & Pembayaran Finance")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Catat detail pembayaran pencairan dana Advance ke pemohon:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Metode Pembayaran") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paymentRef,
                        onValueChange = { paymentRef = it },
                        label = { Text("Nomor Referensi Bank / Voucher") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paymentRemarks,
                        onValueChange = { paymentRemarks = it },
                        label = { Text("Catatan Finance") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // Dialog: Settlement Input
    if (showSettlementDialog) {
        val parsedActual = actualExpenseInput.toLongOrNull() ?: 0L
        val diff = transaction.amount - parsedActual

        AlertDialog(
            onDismissRequest = { showSettlementDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showSettlementDialog = false
                        onSubmitSettlement(parsedActual, settlementRemarks, receiptName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Kirim Settlement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettlementDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            title = {
                Text("Input Realisasi Pengeluaran (Settlement)")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Advance Diterima: ${formatIdr(transaction.amount)}", fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = actualExpenseInput,
                        onValueChange = { actualExpenseInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Actual Expense (Pengeluaran Riil)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Difference Calculation card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (diff >= 0) EmeraldContainer else Color(0xFF7F1D1D)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (diff >= 0) "Pengembalian Sisa (Refund):" else "Klaim Tambahan Biaya:",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (diff >= 0) EmeraldLight else Color(0xFFFECDD3)
                            )
                            Text(
                                text = formatIdr(if (diff >= 0) diff else -diff),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    OutlinedTextField(
                        value = receiptName,
                        onValueChange = { receiptName = it },
                        label = { Text("Nama Lampiran Bukti / Kwitansi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = settlementRemarks,
                        onValueChange = { settlementRemarks = it },
                        label = { Text("Keterangan Realisasi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // Dialog: Verify Settlement
    if (showVerifySettlementDialog) {
        AlertDialog(
            onDismissRequest = { showVerifySettlementDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showVerifySettlementDialog = false
                        onVerifySettlement(verifySettlementApproved, verifySettlementComment)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (verifySettlementApproved) EmeraldPrimary else AccentRose
                    )
                ) {
                    Text(if (verifySettlementApproved) "Verifikasi Lolos" else "Minta Revisi Settlement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerifySettlementDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            title = {
                Text(if (verifySettlementApproved) "Verifikasi & Rekonsiliasi Finance" else "Kembalikan Settlement")
            },
            text = {
                Column {
                    Text(
                        text = "Finance memeriksa kesesuaian kuitansi, nominal pengeluaran riil, dan bukti setoran sisa kas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = verifySettlementComment,
                        onValueChange = { verifySettlementComment = it },
                        label = { Text("Catatan Verifikasi Finance") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isPrice: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isHighlight || isPrice) FontWeight.Bold else FontWeight.Normal
            ),
            color = when {
                isPrice -> EmeraldLight
                isHighlight -> AccentSky
                else -> TextPrimary
            },
            modifier = Modifier.weight(0.55f)
        )
    }
}

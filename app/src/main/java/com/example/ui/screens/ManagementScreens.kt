package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AemsRepository
import com.example.model.*
import com.example.ui.components.formatIdr
import com.example.ui.theme.*

// 8. Documents Screen
@Composable
fun DocumentsScreen(
    documentLogs: Map<String, List<T_DOCUMENT_LOG>>,
    onSelectTransaction: (String) -> Unit
) {
    var selectedDocForPreview by remember { mutableStateOf<T_DOCUMENT_LOG?>(null) }

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
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DOCUMENT FILING & ARSIP ELEKTRONIK",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seluruh dokumen transaksi (Form Standar Advance, Sertifikat Digital Signature, Kwitansi, dan Bukti Transfer) terhubung rapi ke Transaction ID.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        val allDocs = documentLogs.flatMap { it.value }
        item {
            Text(
                text = "BERKAS TERSIMPAN (${allDocs.size} DOKUMEN)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        if (allDocs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada dokumen tersimpan.", color = TextMuted)
                }
            }
        } else {
            items(allDocs) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { selectedDocForPreview = doc }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (doc.documentType) {
                                "SIGNATURE_CERT" -> Icons.Default.Verified
                                "RECEIPT" -> Icons.Default.Receipt
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.documentName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "ID: ${doc.transactionId} • ${doc.documentType} • Ref: ${doc.documentReference}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        IconButton(onClick = { onSelectTransaction(doc.transactionId) }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Buka Transaksi", tint = AccentSky)
                        }
                    }
                }
            }
        }
    }

    if (selectedDocForPreview != null) {
        val doc = selectedDocForPreview!!
        AlertDialog(
            onDismissRequest = { selectedDocForPreview = null },
            confirmButton = {
                TextButton(onClick = { selectedDocForPreview = null }) {
                    Text("Tutup", color = EmeraldPrimary)
                }
            },
            title = {
                Text("Pratinjau Dokumen Transaksi")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(doc.documentName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Transaction ID: ${doc.transactionId}", color = AccentSky)
                    Text("Tipe Dokumen: ${doc.documentType}", color = TextSecondary)
                    Text("Referensi Sistem: ${doc.documentReference}", color = TextSecondary)
                    Text("Waktu Unggah/Generate: ${doc.uploadedAt}", color = TextSecondary)
                    Text("Status: ${doc.documentStatus}", color = EmeraldLight, fontWeight = FontWeight.Bold)

                    Divider(modifier = Modifier.padding(vertical = 4.dp), color = DarkBorder)
                    Text(
                        text = "Catatan Sistem: File tersimpan pada metadata local storage prototype AEMS PSI PT Prasad Seeds Indonesia.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

// 9. Reports Screen
@Composable
fun ReportsScreen(
    transactions: List<T_TRANSACTION>,
    settlements: Map<String, T_SETTLEMENT>
) {
    val totalAdvance = transactions.sumOf { it.amount }
    val totalActual = settlements.values.sumOf { it.actualExpense }
    val settledCount = settlements.values.count { it.verificationStatus == "SETTLEMENT_VERIFIED" }

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
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LAPORAN & ANALISIS BIAYA PT PRASAD SEEDS INDONESIA",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ringkasan pergerakan dana advance, realisasi pengeluaran, dan tingkat penyelesaian rekonsiliasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

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
                        text = "REKAPITULASI FINANCIAL ADVANCE VS ACTUAL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("Total Advance Dicairkan", formatIdr(totalAdvance))
                    InfoRow("Total Realisasi Riil Dilaporkan", formatIdr(totalActual), isPrice = true)
                    InfoRow("Settlement Terverifikasi Selesai", "$settledCount Transaksi", isHighlight = true)
                }
            }
        }

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
                        text = "PENGELUARAN PER DEPARTEMEN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val deptAmounts = transactions.groupBy { it.department }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }

                    deptAmounts.forEach { (dept, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(dept, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Text(formatIdr(amount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AccentSky)
                        }
                        Divider(modifier = Modifier.padding(vertical = 2.dp), color = DarkBorder.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

// 10. Master Data Screen
@Composable
fun MasterDataScreen(repository: AemsRepository) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Users", "Dept", "Cost Center", "Tipe", "Kategori", "Approval Matrix", "Status")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = EmeraldPrimary,
            edgePadding = 8.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (selectedTab) {
                0 -> { // M_USERS
                    items(repository.users) { u ->
                        MasterItemCard(
                            title = "${u.name} (${u.userId})",
                            subtitle = "Email: ${u.email} • Role: ${u.role.label}",
                            extra = "Dept: ${u.department} • Cost Center: ${u.defaultCostCenter}"
                        )
                    }
                }
                1 -> { // M_DEPARTMENT
                    items(repository.departments) { d ->
                        MasterItemCard(
                            title = d.departmentName,
                            subtitle = "ID: ${d.departmentId}",
                            extra = "Head: ${d.departmentHead} • Status: ${d.status}"
                        )
                    }
                }
                2 -> { // M_COST_CENTER
                    items(repository.costCenters) { c ->
                        MasterItemCard(
                            title = "${c.costCenterId} - ${c.costCenterName}",
                            subtitle = "Department: ${c.department}",
                            extra = "Status: ${c.status}"
                        )
                    }
                }
                3 -> { // M_TRANSACTION_TYPE
                    items(repository.transactionTypes) { t ->
                        MasterItemCard(
                            title = t.transactionTypeName,
                            subtitle = "ID: ${t.transactionTypeId} • Modul: ${t.module}",
                            extra = t.description
                        )
                    }
                }
                4 -> { // M_EXPENSE_CATEGORY
                    items(repository.expenseCategories) { ec ->
                        MasterItemCard(
                            title = ec.categoryName,
                            subtitle = "ID: ${ec.expenseCategoryId}",
                            extra = ec.description
                        )
                    }
                }
                5 -> { // M_APPROVAL_MATRIX
                    items(repository.approvalMatrices) { m ->
                        MasterItemCard(
                            title = "${m.department} - ${m.approverRole}",
                            subtitle = "Rentang: ${formatIdr(m.minAmount)} s/d ${formatIdr(m.maxAmount)}",
                            extra = "Approver: ${m.approver} • Sequence: ${m.sequence}"
                        )
                    }
                }
                6 -> { // M_STATUS
                    items(repository.statuses) { s ->
                        MasterItemCard(
                            title = "${s.sequence}. ${s.statusName} (${s.statusId})",
                            subtitle = s.description,
                            extra = "Sequence: ${s.sequence}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MasterItemCard(title: String, subtitle: String, extra: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
            Spacer(modifier = Modifier.height(2.dp))
            Text(extra, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

// 11. Audit Log Screen
@Composable
fun AuditLogScreen(auditLogs: List<T_AUDIT_LOG>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icon(Icons.Default.History, contentDescription = null, tint = AccentSky)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM AUDIT TRAIL LOG",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Semua riwayat perubahan data dan status transaksi tercatat secara permanen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        items(auditLogs) { aud ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = aud.transactionId,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentSky
                        )
                        Text(
                            text = aud.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${aud.action} • User: ${aud.user}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${aud.oldStatus} ➔ ${aud.newStatus}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = aud.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// 12. Settings Screen
@Composable
fun SettingsScreen(
    currentUser: M_USER,
    availableUsers: List<M_USER>,
    onSelectUser: (M_USER) -> Unit,
    onResetDemoData: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Text(
                        text = "SIMULATOR PERAN PENGGUNA (ROLE SWITCHER)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pilih profil pengguna untuk menguji workflow dari perspektif peran berbeda (Requester, EA, Approver, Finance, Management, Admin).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        items(availableUsers) { user ->
            val isSelected = currentUser.userId == user.userId
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) EmeraldContainer.copy(alpha = 0.4f) else DarkSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isSelected) EmeraldPrimary else DarkBorder.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectUser(user) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) EmeraldPrimary else DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(2).uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Role: ${user.role.label} • Dept: ${user.department}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentSky
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Aktif", tint = EmeraldPrimary)
                    }
                }
            }
        }

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
                        text = "DATA & SISTEM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onResetDemoData,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_demo_data_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Seluruh Data Demo ke Awal")
                    }
                }
            }
        }

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
                        text = "TENTANG AEMS PSI",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Advance & Expense Management System\nPT Prasad Seeds Indonesia\nVersi 1.0.0 (Enterprise Prototype)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

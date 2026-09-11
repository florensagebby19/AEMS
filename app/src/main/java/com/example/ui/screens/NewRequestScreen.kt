package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.formatIdr
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    currentUser: M_USER,
    departments: List<M_DEPARTMENT>,
    costCenters: List<M_COST_CENTER>,
    transactionTypes: List<M_TRANSACTION_TYPE>,
    expenseCategories: List<M_EXPENSE_CATEGORY>,
    nextGeneratedId: String,
    onSubmit: (
        department: String,
        costCenter: String,
        transactionType: String,
        purpose: String,
        requiredDate: String,
        amount: Long,
        expenseCategory: String,
        description: String,
        isDraft: Boolean
    ) -> Unit,
    onCancel: () -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedDepartment by remember { mutableStateOf(currentUser.department) }
    var selectedCostCenter by remember { mutableStateOf(currentUser.defaultCostCenter) }
    var selectedType by remember { mutableStateOf(transactionTypes.firstOrNull()?.transactionTypeName ?: "Official Travel Advance") }
    var purpose by remember { mutableStateOf("") }
    var requiredDate by remember {
        // default 7 days from now
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time))
    }
    var amountInput by remember { mutableStateOf("5000000") }
    var selectedCategory by remember { mutableStateOf(expenseCategories.firstOrNull()?.categoryName ?: "Transport & Tiket") }
    var description by remember { mutableStateOf("") }
    var attachedDocName by remember { mutableStateOf("Proposal_RAB_Kegiatan.pdf") }

    // Dropdown expansion states
    var deptExpanded by remember { mutableStateOf(false) }
    var ccExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var catExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Info Box: Auto Transaction ID Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, tint = EmeraldLight)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AEMS Transaction ID (Dibuat Otomatis)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = nextGeneratedId,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldLight
                        )
                        Text(
                            text = "Format standar penomoran otomatis PT Prasad Seeds Indonesia",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Section 1: Requester Profile
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
                        text = "1. INFORMASI PEMOHON",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = currentUser.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Nama Pemohon") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = currentUser.userId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Employee ID") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Department Dropdown
                    ExposedDropdownMenuBox(
                        expanded = deptExpanded,
                        onExpandedChange = { deptExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedDepartment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Departemen Pemohon") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.departmentName, color = TextPrimary) },
                                    onClick = {
                                        selectedDepartment = dept.departmentName
                                        deptExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cost Center Dropdown
                    ExposedDropdownMenuBox(
                        expanded = ccExpanded,
                        onExpandedChange = { ccExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCostCenter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cost Center") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ccExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = ccExpanded,
                            onDismissRequest = { ccExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            costCenters.forEach { cc ->
                                DropdownMenuItem(
                                    text = { Text("${cc.costCenterId} - ${cc.costCenterName}", color = TextPrimary) },
                                    onClick = {
                                        selectedCostCenter = cc.costCenterId
                                        ccExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Transaction Details
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
                        text = "2. DETAIL PENGAJUAN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Transaction Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Transaction Type (Tipe Transaksi)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            transactionTypes.forEach { tt ->
                                DropdownMenuItem(
                                    text = { Text(tt.transactionTypeName, color = TextPrimary) },
                                    onClick = {
                                        selectedType = tt.transactionTypeName
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Purpose
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        label = { Text("Tujuan Pengajuan (Purpose)") },
                        placeholder = { Text("Contoh: Perjalanan dinas survei lapangan plasma Kediri") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_purpose"),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = today,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Request Date") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = requiredDate,
                            onValueChange = { requiredDate = it },
                            label = { Text("Required Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Advance Amount
                    val parsedAmount = amountInput.toLongOrNull() ?: 0L
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Advance Amount (Nominal Uang Muka)") },
                        supportingText = {
                            Text("Estimasi: ${formatIdr(parsedAmount)}", color = EmeraldLight)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, color = EmeraldLight) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_amount"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expense Category
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expense Category (Kategori Biaya)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            expenseCategories.forEach { ec ->
                                DropdownMenuItem(
                                    text = { Text(ec.categoryName, color = TextPrimary) },
                                    onClick = {
                                        selectedCategory = ec.categoryName
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Rincian Estimasi Biaya (Description)") },
                        placeholder = { Text("Tuliskan estimasi per item biaya atau lingkup kegiatan...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_description"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Section 3: Supporting Documents (Simulated Local Attachment)
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
                        text = "3. DOKUMEN PENDUKUNG (SUPPORTING DOCUMENTS)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = EmeraldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lampirkan proposal, invoice vendor, atau rincian anggaran. Dokumen terhubung langsung ke Transaction ID.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attachedDocName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "PDF Document • 420 KB • Siap Diunggah",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        IconButton(
                            onClick = {
                                attachedDocName = "RAB_Revisi_${System.currentTimeMillis() % 1000}.pdf"
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Ganti file", tint = AccentSky)
                        }
                    }
                }
            }
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusRejectedBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusRejectedText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage!!, color = StatusRejectedText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Action Buttons: SAVE DRAFT & SUBMIT REQUEST
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val amount = amountInput.toLongOrNull() ?: 0L
                        if (purpose.isBlank()) {
                            errorMessage = "Mohon isi tujuan pengajuan terlebih dahulu."
                            return@OutlinedButton
                        }
                        errorMessage = null
                        onSubmit(
                            selectedDepartment,
                            selectedCostCenter,
                            selectedType,
                            purpose,
                            requiredDate,
                            amount,
                            selectedCategory,
                            description.ifBlank { "Draft pengajuan awal" },
                            true // isDraft
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_draft_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE DRAFT")
                }

                Button(
                    onClick = {
                        val amount = amountInput.toLongOrNull() ?: 0L
                        if (purpose.isBlank()) {
                            errorMessage = "Tujuan pengajuan wajib diisi!"
                            return@Button
                        }
                        if (amount <= 0L) {
                            errorMessage = "Nominal pengajuan harus lebih dari Rp 0!"
                            return@Button
                        }
                        errorMessage = null
                        onSubmit(
                            selectedDepartment,
                            selectedCostCenter,
                            selectedType,
                            purpose,
                            requiredDate,
                            amount,
                            selectedCategory,
                            description.ifBlank { "Pengajuan resmi Advance PT Prasad Seeds Indonesia" },
                            false // isDraft -> SUBMIT REQUEST
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("submit_request_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SUBMIT REQUEST", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

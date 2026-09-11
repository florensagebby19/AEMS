package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.M_USER
import com.example.model.UserRole
import com.example.ui.components.AemsTopAppBar
import com.example.ui.components.NotificationDialog
import com.example.ui.components.SidebarContent
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AemsViewModel
import com.example.viewmodel.AppScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AemsApp()
            }
        }
    }
}

@Composable
fun AemsApp(viewModel: AemsViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State collections
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedTransactionId by viewModel.selectedTransactionId.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val detailsMap by viewModel.transactionDetails.collectAsStateWithLifecycle()
    val approvalLogsMap by viewModel.approvalLogs.collectAsStateWithLifecycle()
    val documentLogsMap by viewModel.documentLogs.collectAsStateWithLifecycle()
    val financeLogsMap by viewModel.financeLogs.collectAsStateWithLifecycle()
    val settlementsMap by viewModel.settlements.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDeptFilter by viewModel.selectedDepartmentFilter.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsStateWithLifecycle()

    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    // Dialog flags
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showRoleSelectorDialog by remember { mutableStateOf(false) }

    // Display snackbars
    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Back handling
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.DASHBOARD) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen == AppScreen.TRANSACTION_DETAIL) {
            viewModel.navigateTo(AppScreen.TRANSACTIONS)
        } else {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                currentScreen = currentScreen,
                currentUser = currentUser,
                pendingReviewCount = dashboardStats.pendingReviewCount,
                pendingApprovalCount = dashboardStats.pendingApprovalCount,
                readyFinanceCount = dashboardStats.readyForFinanceCount,
                settlementPendingCount = dashboardStats.settlementPendingCount,
                onNavigate = { screen ->
                    viewModel.navigateTo(screen)
                },
                onOpenRoleSelector = { showRoleSelectorDialog = true },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AemsTopAppBar(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    unreadNotificationCount = notifications.count { !it.isRead },
                    onOpenNavDrawer = { coroutineScope.launch { drawerState.open() } },
                    onOpenNotifications = { showNotificationDialog = true },
                    onOpenRoleSelector = { showRoleSelectorDialog = true }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = DarkBackground,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            stats = dashboardStats,
                            recentTransactions = transactions,
                            onNavigate = { screen -> viewModel.navigateTo(screen) },
                            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) }
                        )
                    }

                    AppScreen.NEW_REQUEST -> {
                        NewRequestScreen(
                            currentUser = currentUser,
                            departments = viewModel.repository.departments,
                            costCenters = viewModel.repository.costCenters,
                            transactionTypes = viewModel.repository.transactionTypes,
                            expenseCategories = viewModel.repository.expenseCategories,
                            nextGeneratedId = "AEMS-2026-${String.format("%04d", transactions.size + 1)}",
                            onSubmit = { dept, cc, type, purpose, reqDate, amount, cat, desc, isDraft ->
                                viewModel.submitNewRequest(
                                    department = dept,
                                    costCenter = cc,
                                    transactionType = type,
                                    purpose = purpose,
                                    requiredDate = reqDate,
                                    amount = amount,
                                    expenseCategory = cat,
                                    description = desc,
                                    isDraft = isDraft
                                )
                            },
                            onCancel = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                        )
                    }

                    AppScreen.TRANSACTIONS -> {
                        TransactionsListScreen(
                            transactions = filteredTransactions,
                            departments = viewModel.repository.departments,
                            transactionTypes = viewModel.repository.transactionTypes,
                            searchQuery = searchQuery,
                            selectedDept = selectedDeptFilter,
                            selectedType = selectedTypeFilter,
                            selectedStatus = selectedStatusFilter,
                            selectedSort = selectedSortOrder,
                            onSearchChange = { viewModel.searchQuery.value = it },
                            onDeptChange = { viewModel.selectedDepartmentFilter.value = it },
                            onTypeChange = { viewModel.selectedTypeFilter.value = it },
                            onStatusChange = { viewModel.selectedStatusFilter.value = it },
                            onSortChange = { viewModel.selectedSortOrder.value = it },
                            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) },
                            onAddNewRequest = { viewModel.navigateTo(AppScreen.NEW_REQUEST) }
                        )
                    }

                    AppScreen.TRANSACTION_DETAIL -> {
                        val selectedTx = transactions.find { it.transactionId == selectedTransactionId }
                            ?: transactions.firstOrNull()

                        if (selectedTx != null) {
                            TransactionDetailScreen(
                                transaction = selectedTx,
                                details = detailsMap[selectedTx.transactionId] ?: emptyList(),
                                approvalLogs = approvalLogsMap[selectedTx.transactionId] ?: emptyList(),
                                documents = documentLogsMap[selectedTx.transactionId] ?: emptyList(),
                                financeLog = financeLogsMap[selectedTx.transactionId],
                                settlement = settlementsMap[selectedTx.transactionId],
                                auditLogs = auditLogs,
                                currentUser = currentUser,
                                onBack = { viewModel.navigateTo(AppScreen.TRANSACTIONS) },
                                onReviewEA = { isPass, comment ->
                                    viewModel.reviewByEA(selectedTx.transactionId, isPass, comment)
                                },
                                onProcessApproval = { isApproved, comment ->
                                    viewModel.processApproval(selectedTx.transactionId, isApproved, comment)
                                },
                                onSignDigital = {
                                    viewModel.simulateDigitalSignature(selectedTx.transactionId)
                                },
                                onUpdateFinance = { status, method, ref, remarks ->
                                    viewModel.updateFinanceStatus(selectedTx.transactionId, status, method, ref, remarks)
                                },
                                onSubmitSettlement = { actual, remarks, receipt ->
                                    viewModel.submitSettlement(selectedTx.transactionId, actual, remarks, receipt)
                                },
                                onVerifySettlement = { isApproved, comment ->
                                    viewModel.verifySettlement(selectedTx.transactionId, isApproved, comment)
                                },
                                onCloseTransaction = {
                                    viewModel.closeTransaction(selectedTx.transactionId)
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Transaksi tidak ditemukan.", color = TextMuted)
                            }
                        }
                    }

                    AppScreen.REVIEW -> {
                        ReviewScreen(
                            transactions = transactions,
                            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) }
                        )
                    }

                    AppScreen.APPROVAL -> {
                        ApprovalScreen(
                            transactions = transactions,
                            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) }
                        )
                    }

                    AppScreen.FINANCE -> {
                        FinanceScreen(
                            transactions = transactions,
                            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) }
                        )
                    }

                    AppScreen.SETTLEMENT -> {
                        SettlementScreen(
                            transactions = transactions,
                            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) }
                        )
                    }

                    AppScreen.DOCUMENTS -> {
                        DocumentsScreen(
                            documentLogs = documentLogsMap,
                            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) }
                        )
                    }

                    AppScreen.REPORTS -> {
                        ReportsScreen(
                            transactions = transactions,
                            settlements = settlementsMap
                        )
                    }

                    AppScreen.MASTER_DATA -> {
                        MasterDataScreen(repository = viewModel.repository)
                    }

                    AppScreen.AUDIT_LOG -> {
                        AuditLogScreen(auditLogs = auditLogs)
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            currentUser = currentUser,
                            availableUsers = viewModel.repository.users,
                            onSelectUser = { user -> viewModel.switchUser(user) },
                            onResetDemoData = { viewModel.resetDemoData() }
                        )
                    }
                }
            }
        }
    }

    // Notification Dialog
    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showNotificationDialog = false },
            onSelectTransaction = { txId -> viewModel.openTransactionDetail(txId) },
            onClearAll = { viewModel.clearNotifications() }
        )
    }

    // Role / User Switcher Dialog
    if (showRoleSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSelectorDialog = false },
            confirmButton = {
                TextButton(onClick = { showRoleSelectorDialog = false }) {
                    Text("Tutup", color = EmeraldPrimary)
                }
            },
            title = {
                Text("Ganti Profil & Peran Pengguna")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Simulasikan pengalaman peran yang berbeda pada sistem AEMS PSI:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    viewModel.repository.users.forEach { user ->
                        val isSelected = currentUser.userId == user.userId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) EmeraldContainer else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUser(user)
                                    showRoleSelectorDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) EmeraldPrimary else DarkSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.take(2).uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${user.role.label} • ${user.department}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else TextSecondary
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
}

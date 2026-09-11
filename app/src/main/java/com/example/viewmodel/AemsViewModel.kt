package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AemsRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    NEW_REQUEST("New Request"),
    TRANSACTIONS("My Transactions"),
    TRANSACTION_DETAIL("Detail Transaksi"),
    REVIEW("Review EA"),
    APPROVAL("Approval Matrix"),
    FINANCE("Finance & Pembayaran"),
    SETTLEMENT("Settlement"),
    DOCUMENTS("Document Filing"),
    REPORTS("Laporan & Analisis"),
    MASTER_DATA("Master Data"),
    AUDIT_LOG("Audit Log"),
    SETTINGS("Pengaturan & Role")
}

data class DashboardStats(
    val totalTransactions: Int = 0,
    val pendingReviewCount: Int = 0,
    val pendingApprovalCount: Int = 0,
    val pendingSignatureCount: Int = 0,
    val readyForFinanceCount: Int = 0,
    val processingCount: Int = 0,
    val settlementPendingCount: Int = 0,
    val closedCount: Int = 0,
    val totalAdvanceAmount: Long = 0L,
    val totalActualExpense: Long = 0L,
    val outstandingSettlementAmount: Long = 0L,
    val deptDistribution: Map<String, Int> = emptyMap(),
    val typeDistribution: Map<String, Int> = emptyMap()
)

data class TransactionFilter(
    val query: String,
    val dept: String,
    val type: String,
    val status: String,
    val sort: String
)

class AemsViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AemsRepository(application.applicationContext)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedTransactionId = MutableStateFlow<String?>(null)
    val selectedTransactionId: StateFlow<String?> = _selectedTransactionId.asStateFlow()

    // Transient feedback snackbar
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedDepartmentFilter = MutableStateFlow("Semua Departemen")
    val selectedTypeFilter = MutableStateFlow("Semua Tipe")
    val selectedStatusFilter = MutableStateFlow("Semua Status")
    val selectedSortOrder = MutableStateFlow("Terbaru")

    // Master & Transaction States from Repo
    val currentUser = repository.currentUser
    val transactions = repository.transactions
    val transactionDetails = repository.transactionDetails
    val approvalLogs = repository.approvalLogs
    val documentLogs = repository.documentLogs
    val financeLogs = repository.financeLogs
    val settlements = repository.settlements
    val auditLogs = repository.auditLogs
    val notifications = repository.notifications

    private val filterCriteria: Flow<TransactionFilter> = combine(
        searchQuery,
        selectedDepartmentFilter,
        selectedTypeFilter,
        selectedStatusFilter,
        selectedSortOrder
    ) { query, dept, type, status, sort ->
        TransactionFilter(query, dept, type, status, sort)
    }

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<T_TRANSACTION>> = combine(
        transactions,
        filterCriteria
    ) { txList, filter ->
        txList.filter { tx ->
            val matchQuery = filter.query.isBlank() ||
                    tx.transactionId.contains(filter.query, ignoreCase = true) ||
                    tx.requester.contains(filter.query, ignoreCase = true) ||
                    tx.purpose.contains(filter.query, ignoreCase = true)

            val matchDept = filter.dept == "Semua Departemen" || tx.department.equals(filter.dept, ignoreCase = true)
            val matchType = filter.type == "Semua Tipe" || tx.transactionType.equals(filter.type, ignoreCase = true)
            val matchStatus = filter.status == "Semua Status" || tx.currentStatus.label.equals(filter.status, ignoreCase = true)

            matchQuery && matchDept && matchType && matchStatus
        }.let { list ->
            when (filter.sort) {
                "Terbaru" -> list.sortedByDescending { it.createdAt }
                "Terlama" -> list.sortedBy { it.createdAt }
                "Nominal Tertinggi" -> list.sortedByDescending { it.amount }
                "Nominal Terendah" -> list.sortedBy { it.amount }
                else -> list
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics
    val dashboardStats: StateFlow<DashboardStats> = combine(
        transactions,
        settlements
    ) { txList, setMap ->
        val totalAdvance = txList.sumOf { it.amount }
        val totalActual = setMap.values.sumOf { it.actualExpense }

        // Outstanding settlement is Advance Amount for transactions in PAID or SETTLEMENT_PENDING without verified settlement
        val outstanding = txList.filter {
            it.currentStatus == TransactionStatus.PAID || it.currentStatus == TransactionStatus.SETTLEMENT_PENDING
        }.sumOf { it.amount }

        val deptDist = txList.groupingBy { it.department }.eachCount()
        val typeDist = txList.groupingBy { it.transactionType }.eachCount()

        DashboardStats(
            totalTransactions = txList.size,
            pendingReviewCount = txList.count { it.currentStatus == TransactionStatus.PENDING_REVIEW },
            pendingApprovalCount = txList.count { it.currentStatus == TransactionStatus.PENDING_APPROVAL },
            pendingSignatureCount = txList.count { it.currentStatus == TransactionStatus.PENDING_DIGITAL_SIGNATURE },
            readyForFinanceCount = txList.count { it.currentStatus == TransactionStatus.READY_FOR_FINANCE },
            processingCount = txList.count { it.currentStatus == TransactionStatus.PROCESSING || it.currentStatus == TransactionStatus.RECEIVED_BY_FINANCE },
            settlementPendingCount = txList.count { it.currentStatus == TransactionStatus.SETTLEMENT_PENDING || it.currentStatus == TransactionStatus.PAID },
            closedCount = txList.count { it.currentStatus == TransactionStatus.CLOSED },
            totalAdvanceAmount = totalAdvance,
            totalActualExpense = totalActual,
            outstandingSettlementAmount = outstanding,
            deptDistribution = deptDist,
            typeDistribution = typeDist
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun navigateTo(screen: AppScreen, transactionId: String? = null) {
        if (transactionId != null) {
            _selectedTransactionId.value = transactionId
        }
        _currentScreen.value = screen
    }

    fun openTransactionDetail(transactionId: String) {
        _selectedTransactionId.value = transactionId
        _currentScreen.value = AppScreen.TRANSACTION_DETAIL
    }

    fun showToast(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun clearNotifications() {
        repository.clearAllNotifications()
    }

    fun switchUser(user: M_USER) {
        repository.switchUser(user)
        showToast("Beralih profil ke ${user.name} (${user.role.label})")
    }

    fun switchRole(role: UserRole) {
        repository.switchRole(role)
        showToast("Mode peran beralih ke: ${role.label}")
    }

    // Workflow actions wrappers
    fun submitNewRequest(
        department: String,
        costCenter: String,
        transactionType: String,
        purpose: String,
        requiredDate: String,
        amount: Long,
        expenseCategory: String,
        description: String,
        isDraft: Boolean
    ): String {
        val id = repository.createTransaction(
            department = department,
            costCenter = costCenter,
            transactionType = transactionType,
            purpose = purpose,
            requiredDate = requiredDate,
            amount = amount,
            expenseCategory = expenseCategory,
            description = description,
            isDraft = isDraft
        )
        showToast(if (isDraft) "Draft pengajuan $id tersimpan" else "Pengajuan $id berhasil diajukan ke EA!")
        openTransactionDetail(id)
        return id
    }

    fun reviewByEA(transactionId: String, isPass: Boolean, comment: String) {
        repository.reviewTransactionByEA(transactionId, isPass, comment)
        showToast(if (isPass) "Tinjauan EA lolos: pengajuan diteruskan ke Approval Matrix" else "Pengajuan dikembalikan dengan status Need Revision")
    }

    fun processApproval(transactionId: String, isApproved: Boolean, comment: String) {
        repository.processApproval(transactionId, isApproved, comment)
        showToast(if (isApproved) "Pengajuan disetujui! Dokumen standar dibuat untuk tanda tangan digital." else "Pengajuan ditolak oleh Approver.")
    }

    fun simulateDigitalSignature(transactionId: String) {
        val signer = currentUser.value.name
        repository.simulateDigitalSignature(transactionId, signer)
        showToast("Tanda tangan digital berhasil diverifikasi! Transaksi sekarang Ready for Finance.")
    }

    fun updateFinanceStatus(
        transactionId: String,
        targetStatus: TransactionStatus,
        paymentMethod: String? = null,
        paymentRef: String? = null,
        remarks: String? = null
    ) {
        repository.updateFinanceStatus(transactionId, targetStatus, paymentMethod, paymentRef, remarks)
        showToast("Status Finance diperbarui: ${targetStatus.label}")
    }

    fun submitSettlement(
        transactionId: String,
        actualExpense: Long,
        remarks: String,
        receiptName: String
    ) {
        repository.submitSettlement(transactionId, actualExpense, remarks, receiptName)
        showToast("Settlement pengeluaran riil berhasil dikirim ke Finance untuk verifikasi.")
    }

    fun verifySettlement(transactionId: String, isApproved: Boolean, comment: String) {
        repository.verifySettlementByFinance(transactionId, isApproved, comment)
        showToast(if (isApproved) "Settlement berhasil diverifikasi oleh Finance!" else "Settlement ditolak / perlu revisi.")
    }

    fun closeTransaction(transactionId: String) {
        repository.closeTransaction(transactionId)
        showToast("Transaksi $transactionId resmi ditutup dan diarsipkan.")
    }

    fun resetDemoData() {
        repository.resetDemoData()
        showToast("Data demo AEMS PSI berhasil di-reset ke status awal.")
    }
}

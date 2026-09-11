package com.example.model

/**
 * Data architecture models for AEMS PSI (PT Prasad Seeds Indonesia)
 * Advance & Expense Management System
 */

enum class UserRole(val label: String, val description: String) {
    REQUESTER("Requester", "Pemohon uang muka & pelaporan settlement"),
    EA("EA / Process Owner", "Pemeriksa kelengkapan & pemilik proses"),
    APPROVER("Approver", "Pemberi persetujuan berjenjang"),
    FINANCE("Finance", "Verifikasi, pembayaran & rekonsiliasi"),
    MANAGEMENT("Management", "Monitoring & ringkasan eksekutif"),
    ADMIN("Admin", "Pengelola master data & konfigurasi")
}

enum class TransactionStatus(val label: String, val badgeColorKey: String) {
    DRAFT("Draft", "draft"),
    PENDING_REVIEW("Pending Review", "pending"),
    NEED_REVISION("Need Revision", "rejected"),
    PENDING_APPROVAL("Pending Approval", "pending"),
    APPROVED("Approved", "approved"),
    REJECTED("Rejected", "rejected"),
    PENDING_DIGITAL_SIGNATURE("Pending Digital Signature", "pending"),
    READY_FOR_FINANCE("Ready for Finance", "finance"),
    RECEIVED_BY_FINANCE("Received by Finance", "finance"),
    PROCESSING("Processing", "finance"),
    PAID("Paid", "approved"),
    SETTLEMENT_PENDING("Settlement Pending", "pending"),
    SETTLEMENT_VERIFIED("Settlement Verified", "approved"),
    CLOSED("Closed", "closed")
}

// Master Data Entities (Prefix M_)

data class M_USER(
    val userId: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val department: String,
    val defaultCostCenter: String,
    val status: String = "ACTIVE"
)

data class M_DEPARTMENT(
    val departmentId: String,
    val departmentName: String,
    val departmentHead: String,
    val status: String = "ACTIVE"
)

data class M_COST_CENTER(
    val costCenterId: String,
    val costCenterName: String,
    val department: String,
    val status: String = "ACTIVE"
)

data class M_TRANSACTION_TYPE(
    val transactionTypeId: String,
    val transactionTypeName: String,
    val module: String, // e.g. "ADVANCE", "EXPENSE"
    val description: String,
    val status: String = "ACTIVE"
)

data class M_EXPENSE_CATEGORY(
    val expenseCategoryId: String,
    val categoryName: String,
    val description: String,
    val status: String = "ACTIVE"
)

data class M_APPROVAL_MATRIX(
    val approvalId: String,
    val department: String,
    val transactionType: String,
    val minAmount: Long,
    val maxAmount: Long,
    val approverRole: String,
    val approver: String,
    val sequence: Int,
    val status: String = "ACTIVE"
)

data class M_STATUS(
    val statusId: String,
    val statusName: String,
    val sequence: Int,
    val description: String
)

// Transaction Entities (Prefix T_)

data class T_TRANSACTION(
    val transactionId: String,
    val requester: String,
    val employeeId: String,
    val department: String,
    val costCenter: String,
    val transactionType: String,
    val purpose: String,
    val requestDate: String,
    val requiredDate: String,
    val amount: Long,
    val currentStatus: TransactionStatus,
    val createdAt: String,
    val updatedAt: String
)

data class T_TRANSACTION_DETAIL(
    val detailId: String,
    val transactionId: String,
    val expenseCategory: String,
    val description: String,
    val estimatedAmount: Long,
    val actualAmount: Long = 0L
)

data class T_APPROVAL_LOG(
    val approvalLogId: String,
    val transactionId: String,
    val approver: String,
    val action: String, // "SUBMIT", "REVIEW_PASS", "NEED_REVISION", "APPROVED", "REJECTED"
    val comment: String,
    val timestamp: String
)

data class T_DOCUMENT_LOG(
    val documentId: String,
    val transactionId: String,
    val documentName: String,
    val documentType: String, // "PROPOSAL", "INVOICE", "ADVANCE_FORM", "SIGNATURE_CERT", "RECEIPT", "PAYMENT_PROOF"
    val documentStatus: String, // "ATTACHED", "VERIFIED", "GENERATED", "SIGNED"
    val documentReference: String,
    val uploadedAt: String
)

data class T_FINANCE_LOG(
    val financeLogId: String,
    val transactionId: String,
    val receivedDate: String? = null,
    val processingDate: String? = null,
    val paymentDate: String? = null,
    val paymentReference: String? = null,
    val paymentMethod: String? = null, // "BANK TRANSFER BCA", "BANK TRANSFER MANDIRI", "PETTY CASH"
    val financeStatus: String, // "PENDING", "RECEIVED", "PROCESSING", "PAID"
    val remarks: String? = null
)

data class T_SETTLEMENT(
    val settlementId: String,
    val transactionId: String,
    val advanceAmount: Long,
    val actualExpense: Long,
    val difference: Long, // advance - actual
    val settlementDate: String,
    val verificationStatus: String, // "PENDING_SUBMISSION", "PENDING_VERIFICATION", "SETTLEMENT_VERIFIED"
    val remarks: String
)

data class T_AUDIT_LOG(
    val auditId: String,
    val transactionId: String,
    val user: String,
    val action: String,
    val oldStatus: String,
    val newStatus: String,
    val timestamp: String,
    val description: String
)

// App Notification Item
data class AemsNotification(
    val id: String,
    val title: String,
    val message: String,
    val transactionId: String,
    val timestamp: String,
    val isRead: Boolean = false
)

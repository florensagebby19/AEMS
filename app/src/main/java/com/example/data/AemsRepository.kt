package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * In-memory and SharedPreferences backed repository for AEMS PSI Prototype.
 * PT Prasad Seeds Indonesia.
 * NOTE: For production, replace local storage implementations with secure REST/GraphQL API
 * and backend database endpoints.
 */
class AemsRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("aems_psi_prefs", Context.MODE_PRIVATE)

    // Master Data States
    val users = listOf(
        M_USER("USR-001", "Budi Santoso", "budi.santoso@prasadseeds.co.id", UserRole.REQUESTER, "EA", "CC-EA-01"),
        M_USER("USR-002", "Ratna Sari", "ratna.sari@prasadseeds.co.id", UserRole.EA, "EA", "CC-EA-01"),
        M_USER("USR-003", "Ir. Suryanto, M.M.", "suryanto@prasadseeds.co.id", UserRole.APPROVER, "Operations Advance", "CC-OPS-01"),
        M_USER("USR-004", "Linda Kartika, S.E.", "linda.kartika@prasadseeds.co.id", UserRole.FINANCE, "Other Departmental Advance", "CC-GEN-01"),
        M_USER("USR-005", "Dr. Michael Prasad", "michael.prasad@prasadseeds.co.id", UserRole.MANAGEMENT, "EA", "CC-EA-01"),
        M_USER("USR-006", "Dian Pratama", "dian.pratama@prasadseeds.co.id", UserRole.ADMIN, "Other Departmental Advance", "CC-GEN-01")
    )

    val departments = listOf(
        M_DEPARTMENT("DEP-01", "EA", "Ratna Sari"),
        M_DEPARTMENT("DEP-02", "Procurement Advance", "Anton Subagyo"),
        M_DEPARTMENT("DEP-03", "HR Advance", "Maria Natalia"),
        M_DEPARTMENT("DEP-04", "Operations Advance", "Ir. Suryanto, M.M."),
        M_DEPARTMENT("DEP-05", "Project / Engineering Advance", "Bambang Irawan, S.T."),
        M_DEPARTMENT("DEP-06", "Other Departmental Advance", "Dian Pratama")
    )

    val costCenters = listOf(
        M_COST_CENTER("CC-EA-01", "Executive Affairs & Administration", "EA"),
        M_COST_CENTER("CC-PROC-01", "Seed Supply & Procurement", "Procurement Advance"),
        M_COST_CENTER("CC-HR-01", "Human Capital & Training", "HR Advance"),
        M_COST_CENTER("CC-OPS-01", "Seed Processing & Field Trial", "Operations Advance"),
        M_COST_CENTER("CC-ENG-01", "Engineering & Plant Machinery", "Project / Engineering Advance"),
        M_COST_CENTER("CC-GEN-01", "Corporate Finance & General", "Other Departmental Advance")
    )

    val transactionTypes = listOf(
        M_TRANSACTION_TYPE("TT-01", "Official Travel Advance", "ADVANCE", "Uang muka biaya perjalanan dinas & monitoring"),
        M_TRANSACTION_TYPE("TT-02", "Operational Field Advance", "ADVANCE", "Uang muka kebutuhan riset lahan & uji benih"),
        M_TRANSACTION_TYPE("TT-03", "Procurement Advance", "ADVANCE", "Uang muka pengadaan bahan baku pembibitan"),
        M_TRANSACTION_TYPE("TT-04", "Project & Engineering Advance", "ADVANCE", "Uang muka instalasi mesin seleksi biji"),
        M_TRANSACTION_TYPE("TT-05", "Emergency Advance", "ADVANCE", "Uang muka kebutuhan tanggap darurat operasional"),
        M_TRANSACTION_TYPE("TT-06", "Direct Expense Reimbursement", "EXPENSE", "Penggantian biaya aktual operasional")
    )

    val expenseCategories = listOf(
        M_EXPENSE_CATEGORY("EC-01", "Transport & Tiket", "Biaya tiket penerbangan, kereta, BBM, & tol"),
        M_EXPENSE_CATEGORY("EC-02", "Akomodasi & Hotel", "Penginapan selama perjalanan dinas"),
        M_EXPENSE_CATEGORY("EC-03", "Uang Harian & Konsumsi", "Per diem makan dan tunjangan dinas luar"),
        M_EXPENSE_CATEGORY("EC-04", "Bibit, Pupuk & Nutrisi", "Pengadaan input biologis uji varietas"),
        M_EXPENSE_CATEGORY("EC-05", "Sewa Alat & Mesin", "Sewa traktor dan mesin pengering benih"),
        M_EXPENSE_CATEGORY("EC-06", "Suku Cadang & Fabrikasi", "Bahan perbaikan mesin packing benih"),
        M_EXPENSE_CATEGORY("EC-07", "Perizinan & Sertifikasi", "Biaya sertifikasi benih BPSB")
    )

    val approvalMatrices = listOf(
        M_APPROVAL_MATRIX("AP-01", "EA", "Official Travel Advance", 0, 10000000, "EA Lead", "Ratna Sari", 1),
        M_APPROVAL_MATRIX("AP-02", "Operations Advance", "Operational Field Advance", 0, 20000000, "Operation Manager", "Ir. Suryanto, M.M.", 1),
        M_APPROVAL_MATRIX("AP-03", "Procurement Advance", "Procurement Advance", 0, 50000000, "Procurement Head", "Anton Subagyo", 1),
        M_APPROVAL_MATRIX("AP-04", "Project / Engineering Advance", "Project & Engineering Advance", 0, 100000000, "Director", "Dr. Michael Prasad", 2)
    )

    val statuses = listOf(
        M_STATUS("ST-01", "Draft", 1, "Pengajuan baru disimpan sementara"),
        M_STATUS("ST-02", "Pending Review", 2, "Menunggu tinjauan kelengkapan oleh EA"),
        M_STATUS("ST-03", "Need Revision", 3, "Dikembalikan oleh EA untuk dilengkapi"),
        M_STATUS("ST-04", "Pending Approval", 4, "Menunggu persetujuan berjenjang"),
        M_STATUS("ST-05", "Approved", 5, "Disetujui oleh Approver"),
        M_STATUS("ST-06", "Rejected", 6, "Ditolak dengan catatan alasan"),
        M_STATUS("ST-07", "Pending Digital Signature", 7, "Menunggu penandatanganan dokumen elektronik"),
        M_STATUS("ST-08", "Ready for Finance", 8, "Siap diproses oleh tim Finance"),
        M_STATUS("ST-09", "Received by Finance", 9, "Diterima tim Finance"),
        M_STATUS("ST-10", "Processing", 10, "Sedang dipersiapkan pencairan dana"),
        M_STATUS("ST-11", "Paid", 11, "Dana telah dicairkan ke pemohon"),
        M_STATUS("ST-12", "Settlement Pending", 12, "Menunggu input realisasi pengeluaran"),
        M_STATUS("ST-13", "Settlement Verified", 13, "Settlement telah diverifikasi Finance"),
        M_STATUS("ST-14", "Closed", 14, "Transaksi selesai dan diarsipkan")
    )

    // Reactive State Holders
    private val _currentUser = MutableStateFlow(users[0])
    val currentUser: StateFlow<M_USER> = _currentUser.asStateFlow()

    private val _transactions = MutableStateFlow<List<T_TRANSACTION>>(emptyList())
    val transactions: StateFlow<List<T_TRANSACTION>> = _transactions.asStateFlow()

    private val _transactionDetails = MutableStateFlow<Map<String, List<T_TRANSACTION_DETAIL>>>(emptyMap())
    val transactionDetails: StateFlow<Map<String, List<T_TRANSACTION_DETAIL>>> = _transactionDetails.asStateFlow()

    private val _approvalLogs = MutableStateFlow<Map<String, List<T_APPROVAL_LOG>>>(emptyMap())
    val approvalLogs: StateFlow<Map<String, List<T_APPROVAL_LOG>>> = _approvalLogs.asStateFlow()

    private val _documentLogs = MutableStateFlow<Map<String, List<T_DOCUMENT_LOG>>>(emptyMap())
    val documentLogs: StateFlow<Map<String, List<T_DOCUMENT_LOG>>> = _documentLogs.asStateFlow()

    private val _financeLogs = MutableStateFlow<Map<String, T_FINANCE_LOG>>(emptyMap())
    val financeLogs: StateFlow<Map<String, T_FINANCE_LOG>> = _financeLogs.asStateFlow()

    private val _settlements = MutableStateFlow<Map<String, T_SETTLEMENT>>(emptyMap())
    val settlements: StateFlow<Map<String, T_SETTLEMENT>> = _settlements.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<T_AUDIT_LOG>>(emptyList())
    val auditLogs: StateFlow<List<T_AUDIT_LOG>> = _auditLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<AemsNotification>>(emptyList())
    val notifications: StateFlow<List<AemsNotification>> = _notifications.asStateFlow()

    init {
        loadInitialData()
    }

    private fun getCurrentDateTimeString(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun switchUser(user: M_USER) {
        _currentUser.value = user
    }

    fun switchRole(role: UserRole) {
        val user = users.find { it.role == role } ?: users[0]
        _currentUser.value = user
    }

    fun generateTransactionId(): String {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val count = _transactions.value.size + 1
        return "AEMS-$year-${String.format(Locale.US, "%04d", count)}"
    }

    /**
     * Initializes rich dummy data across 8+ different states and departments
     */
    fun loadInitialData() {
        val initialTx = listOf(
            T_TRANSACTION(
                transactionId = "AEMS-2026-0001",
                requester = "Budi Santoso",
                employeeId = "PSI-EA-010",
                department = "EA",
                costCenter = "CC-EA-01",
                transactionType = "Official Travel Advance",
                purpose = "Perjalanan dinas koordinasi sertifikasi benih ke Balai Benih Jawa Timur",
                requestDate = "2026-09-01",
                requiredDate = "2026-09-15",
                amount = 4500000L,
                currentStatus = TransactionStatus.PENDING_REVIEW,
                createdAt = "2026-09-01 09:15:00",
                updatedAt = "2026-09-01 09:15:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0002",
                requester = "Siti Rahma",
                employeeId = "PSI-PRC-024",
                department = "Procurement Advance",
                costCenter = "CC-PROC-01",
                transactionType = "Procurement Advance",
                purpose = "Uang muka pengadaan kantong kemasan benih hibrida 5kg (10.000 pcs)",
                requestDate = "2026-09-02",
                requiredDate = "2026-09-18",
                amount = 18500000L,
                currentStatus = TransactionStatus.NEED_REVISION,
                createdAt = "2026-09-02 10:30:00",
                updatedAt = "2026-09-03 14:20:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0003",
                requester = "Joko Prasetyo",
                employeeId = "PSI-HR-007",
                department = "HR Advance",
                costCenter = "CC-HR-01",
                transactionType = "Official Travel Advance",
                purpose = "Pelatihan Agronomi dan Manajemen Mutu Benih untuk 12 staf lapangan",
                requestDate = "2026-09-03",
                requiredDate = "2026-09-20",
                amount = 9200000L,
                currentStatus = TransactionStatus.PENDING_APPROVAL,
                createdAt = "2026-09-03 11:00:00",
                updatedAt = "2026-09-04 10:15:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0004",
                requester = "Dewi Lestari",
                employeeId = "PSI-OPS-031",
                department = "Operations Advance",
                costCenter = "CC-OPS-01",
                transactionType = "Operational Field Advance",
                purpose = "Sewa alsintan traktor dan pembersihan gulma lahan plasma Kediri",
                requestDate = "2026-09-04",
                requiredDate = "2026-09-16",
                amount = 12800000L,
                currentStatus = TransactionStatus.APPROVED,
                createdAt = "2026-09-04 08:45:00",
                updatedAt = "2026-09-05 16:30:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0005",
                requester = "Andi Wijaya",
                employeeId = "PSI-ENG-019",
                department = "Project / Engineering Advance",
                costCenter = "CC-ENG-01",
                transactionType = "Project & Engineering Advance",
                purpose = "Fabrikasi sensor suhu & kelembaban otomatis di gudang penyimpanan benih",
                requestDate = "2026-09-05",
                requiredDate = "2026-09-22",
                amount = 35000000L,
                currentStatus = TransactionStatus.PENDING_DIGITAL_SIGNATURE,
                createdAt = "2026-09-05 13:20:00",
                updatedAt = "2026-09-06 11:10:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0006",
                requester = "Rahmat Hidayat",
                employeeId = "PSI-OPS-012",
                department = "Operations Advance",
                costCenter = "CC-OPS-01",
                transactionType = "Operational Field Advance",
                purpose = "Pengadaan pupuk hayati dan pestisida ramah lingkungan musim tanam kedua",
                requestDate = "2026-09-06",
                requiredDate = "2026-09-17",
                amount = 22000000L,
                currentStatus = TransactionStatus.READY_FOR_FINANCE,
                createdAt = "2026-09-06 09:00:00",
                updatedAt = "2026-09-07 15:45:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0007",
                requester = "Hendra Kusuma",
                employeeId = "PSI-PRC-005",
                department = "Procurement Advance",
                costCenter = "CC-PROC-01",
                transactionType = "Procurement Advance",
                purpose = "Pembelian palet plastik industri khusus standar ruang dingin (cold storage)",
                requestDate = "2026-09-07",
                requiredDate = "2026-09-19",
                amount = 14500000L,
                currentStatus = TransactionStatus.PROCESSING,
                createdAt = "2026-09-07 10:40:00",
                updatedAt = "2026-09-08 09:30:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0008",
                requester = "Maya Indah",
                employeeId = "PSI-ENG-022",
                department = "Project / Engineering Advance",
                costCenter = "CC-ENG-01",
                transactionType = "Project & Engineering Advance",
                purpose = "Penggantian conveyor belt mesin sortir benih jagung unit 3",
                requestDate = "2026-08-25",
                requiredDate = "2026-09-01",
                amount = 16000000L,
                currentStatus = TransactionStatus.SETTLEMENT_PENDING,
                createdAt = "2026-08-25 14:00:00",
                updatedAt = "2026-09-02 11:20:00"
            ),
            T_TRANSACTION(
                transactionId = "AEMS-2026-0009",
                requester = "Aris Munandar",
                employeeId = "PSI-OTH-004",
                department = "Other Departmental Advance",
                costCenter = "CC-GEN-01",
                transactionType = "Direct Expense Reimbursement",
                purpose = "Perpanjangan lisensi perangkat lunak pemetaan GIS & sertifikasi ISO 9001",
                requestDate = "2026-08-15",
                requiredDate = "2026-08-20",
                amount = 8750000L,
                currentStatus = TransactionStatus.CLOSED,
                createdAt = "2026-08-15 08:30:00",
                updatedAt = "2026-08-28 16:50:00"
            )
        )
        _transactions.value = initialTx

        // Details mapping
        val detailsMap = mutableMapOf<String, List<T_TRANSACTION_DETAIL>>()
        detailsMap["AEMS-2026-0001"] = listOf(
            T_TRANSACTION_DETAIL("DET-01", "AEMS-2026-0001", "Transport & Tiket", "Tiket KA Eksekutif Surabaya PP", 1800000L, 0L),
            T_TRANSACTION_DETAIL("DET-02", "AEMS-2026-0001", "Akomodasi & Hotel", "Hotel 2 malam dekat Balai Benih", 1500000L, 0L),
            T_TRANSACTION_DETAIL("DET-03", "AEMS-2026-0001", "Uang Harian & Konsumsi", "Per diem 3 hari", 1200000L, 0L)
        )
        detailsMap["AEMS-2026-0006"] = listOf(
            T_TRANSACTION_DETAIL("DET-04", "AEMS-2026-0006", "Bibit, Pupuk & Nutrisi", "Pupuk Hayati 500 kg", 15000000L, 0L),
            T_TRANSACTION_DETAIL("DET-05", "AEMS-2026-0006", "Bibit, Pupuk & Nutrisi", "Pestisida Ramah Lingkungan", 7000000L, 0L)
        )
        detailsMap["AEMS-2026-0008"] = listOf(
            T_TRANSACTION_DETAIL("DET-06", "AEMS-2026-0008", "Suku Cadang & Fabrikasi", "Rubber Conveyor Belt Food Grade", 14500000L, 14200000L),
            T_TRANSACTION_DETAIL("DET-07", "AEMS-2026-0008", "Suku Cadang & Fabrikasi", "Jasa Teknisi Pemasangan", 1500000L, 1500000L)
        )
        _transactionDetails.value = detailsMap

        // Documents mapping
        val docsMap = mutableMapOf<String, List<T_DOCUMENT_LOG>>()
        docsMap["AEMS-2026-0001"] = listOf(
            T_DOCUMENT_LOG("DOC-01", "AEMS-2026-0001", "Surat_Tugas_Balai_Benih_Jatim.pdf", "PROPOSAL", "VERIFIED", "REF-DOC-991", "2026-09-01 09:16:00"),
            T_DOCUMENT_LOG("DOC-02", "AEMS-2026-0001", "Estimasi_Biaya_Dinas.xlsx", "ESTIMATION", "ATTACHED", "REF-DOC-992", "2026-09-01 09:17:00")
        )
        docsMap["AEMS-2026-0006"] = listOf(
            T_DOCUMENT_LOG("DOC-03", "AEMS-2026-0006", "Form_Pengajuan_Advance_PSI.pdf", "ADVANCE_FORM", "GENERATED", "GEN-AEMS-0006", "2026-09-07 14:00:00"),
            T_DOCUMENT_LOG("DOC-04", "AEMS-2026-0006", "Sertifikat_Digital_Signature.pdf", "SIGNATURE_CERT", "SIGNED", "DS-PSI-2026-881", "2026-09-07 15:45:00"),
            T_DOCUMENT_LOG("DOC-05", "AEMS-2026-0006", "Invoice_Vendor_Pupuk_Hayati.pdf", "INVOICE", "VERIFIED", "INV-VND-441", "2026-09-06 09:05:00")
        )
        docsMap["AEMS-2026-0008"] = listOf(
            T_DOCUMENT_LOG("DOC-06", "AEMS-2026-0008", "Kwitansi_Pembelian_Conveyor.pdf", "RECEIPT", "ATTACHED", "RCP-88912", "2026-09-02 10:00:00"),
            T_DOCUMENT_LOG("DOC-07", "AEMS-2026-0008", "Bukti_Transfer_Advance_Finance.pdf", "PAYMENT_PROOF", "VERIFIED", "TRF-PSI-0901-88", "2026-09-01 11:00:00")
        )
        _documentLogs.value = docsMap

        // Finance logs mapping
        val finMap = mutableMapOf<String, T_FINANCE_LOG>()
        finMap["AEMS-2026-0007"] = T_FINANCE_LOG(
            financeLogId = "FIN-01",
            transactionId = "AEMS-2026-0007",
            receivedDate = "2026-09-07 16:00:00",
            processingDate = "2026-09-08 09:30:00",
            paymentMethod = "BANK TRANSFER MANDIRI",
            financeStatus = "PROCESSING",
            remarks = "Menunggu verifikasi batch transfer ke vendor palet"
        )
        finMap["AEMS-2026-0008"] = T_FINANCE_LOG(
            financeLogId = "FIN-02",
            transactionId = "AEMS-2026-0008",
            receivedDate = "2026-08-30 10:00:00",
            processingDate = "2026-08-31 09:00:00",
            paymentDate = "2026-09-01 11:00:00",
            paymentReference = "TRF-PSI-2026-90412",
            paymentMethod = "BANK TRANSFER BCA",
            financeStatus = "PAID",
            remarks = "Pencairan advance telah ditransfer ke rekening Maya Indah"
        )
        finMap["AEMS-2026-0009"] = T_FINANCE_LOG(
            financeLogId = "FIN-03",
            transactionId = "AEMS-2026-0009",
            receivedDate = "2026-08-18 10:00:00",
            processingDate = "2026-08-19 14:00:00",
            paymentDate = "2026-08-20 09:15:00",
            paymentReference = "TRF-PSI-2026-78192",
            paymentMethod = "BANK TRANSFER MANDIRI",
            financeStatus = "PAID",
            remarks = "Pembayaran reimbursement lisensi selesai"
        )
        _financeLogs.value = finMap

        // Settlements mapping
        val setMap = mutableMapOf<String, T_SETTLEMENT>()
        setMap["AEMS-2026-0008"] = T_SETTLEMENT(
            settlementId = "SET-01",
            transactionId = "AEMS-2026-0008",
            advanceAmount = 16000000L,
            actualExpense = 15700000L,
            difference = 300000L, // sisa uang muka dikembalikan (refund)
            settlementDate = "2026-09-02",
            verificationStatus = "PENDING_VERIFICATION",
            remarks = "Sisa dana Rp 300.000 telah disetor kembali ke kas kecil PT Prasad Seeds Indonesia"
        )
        setMap["AEMS-2026-0009"] = T_SETTLEMENT(
            settlementId = "SET-02",
            transactionId = "AEMS-2026-0009",
            advanceAmount = 8750000L,
            actualExpense = 8750000L,
            difference = 0L,
            settlementDate = "2026-08-25",
            verificationStatus = "SETTLEMENT_VERIFIED",
            remarks = "Kuitansi dan invoice resmi ISO terlampir lengkap"
        )
        _settlements.value = setMap

        // Audit trails
        val audits = listOf(
            T_AUDIT_LOG("AUD-01", "AEMS-2026-0001", "Budi Santoso", "SUBMIT_REQUEST", "DRAFT", "PENDING_REVIEW", "2026-09-01 09:15:00", "Requester submitted initial request"),
            T_AUDIT_LOG("AUD-02", "AEMS-2026-0002", "Siti Rahma", "SUBMIT_REQUEST", "DRAFT", "PENDING_REVIEW", "2026-09-02 10:30:00", "Requester submitted initial request"),
            T_AUDIT_LOG("AUD-03", "AEMS-2026-0002", "Ratna Sari", "REQUEST_REVISION", "PENDING_REVIEW", "NEED_REVISION", "2026-09-03 14:20:00", "EA requested revision: Mohon lengkapi perbandingan 3 penawaran harga vendor"),
            T_AUDIT_LOG("AUD-04", "AEMS-2026-0003", "Ratna Sari", "PASS_REVIEW", "PENDING_REVIEW", "PENDING_APPROVAL", "2026-09-04 10:15:00", "EA reviewed and validated completeness"),
            T_AUDIT_LOG("AUD-05", "AEMS-2026-0004", "Ir. Suryanto, M.M.", "APPROVE_REQUEST", "PENDING_APPROVAL", "APPROVED", "2026-09-05 16:30:00", "Approver approved advance request for field trial Kediri"),
            T_AUDIT_LOG("AUD-06", "AEMS-2026-0005", "System", "GENERATE_DOC", "APPROVED", "PENDING_DIGITAL_SIGNATURE", "2026-09-06 11:10:00", "System generated standard Advance form"),
            T_AUDIT_LOG("AUD-07", "AEMS-2026-0006", "Rahmat Hidayat", "DIGITAL_SIGN", "PENDING_DIGITAL_SIGNATURE", "READY_FOR_FINANCE", "2026-09-07 15:45:00", "Simulated digital signature completed with hash certification"),
            T_AUDIT_LOG("AUD-08", "AEMS-2026-0007", "Linda Kartika, S.E.", "PROCESS_PAYMENT", "RECEIVED_BY_FINANCE", "PROCESSING", "2026-09-08 09:30:00", "Finance initiated disbursement batch transfer"),
            T_AUDIT_LOG("AUD-09", "AEMS-2026-0008", "Linda Kartika, S.E.", "DISBURSE_PAYMENT", "PROCESSING", "PAID", "2026-09-01 11:00:00", "Payment transferred via BCA ref TRF-PSI-2026-90412"),
            T_AUDIT_LOG("AUD-10", "AEMS-2026-0008", "Maya Indah", "SUBMIT_SETTLEMENT", "PAID", "SETTLEMENT_PENDING", "2026-09-02 11:20:00", "Requester submitted settlement with refund calculation Rp 300.000"),
            T_AUDIT_LOG("AUD-11", "AEMS-2026-0009", "Linda Kartika, S.E.", "CLOSE_TRANSACTION", "SETTLEMENT_VERIFIED", "CLOSED", "2026-08-28 16:50:00", "Finance completed verification & transaction archived")
        )
        _auditLogs.value = audits

        // Initial Notifications
        _notifications.value = listOf(
            AemsNotification("NOTIF-1", "Pengajuan Baru Masuk", "Pengajuan AEMS-2026-0001 menunggu review EA.", "AEMS-2026-0001", "2026-09-01 09:15:00"),
            AemsNotification("NOTIF-2", "Revisi Pengajuan", "Pengajuan AEMS-2026-0002 membutuhkan revisi penawaran vendor.", "AEMS-2026-0002", "2026-09-03 14:20:00"),
            AemsNotification("NOTIF-3", "Perlu Persetujuan", "Pengajuan AEMS-2026-0003 memerlukan persetujuan Manajer HR.", "AEMS-2026-0003", "2026-09-04 10:15:00"),
            AemsNotification("NOTIF-4", "Siap Diproses Finance", "Transaksi AEMS-2026-0006 telah Ready for Finance.", "AEMS-2026-0006", "2026-09-07 15:45:00"),
            AemsNotification("NOTIF-5", "Settlement Menunggu Verifikasi", "Settlement AEMS-2026-0008 diajukan dengan selisih refund Rp 300.000.", "AEMS-2026-0008", "2026-09-02 11:20:00")
        )
    }

    // Workflow Actions

    /**
     * 1 & 2. Pemohon membuat pengajuan & pendaftaran sistem (auto ID)
     */
    fun createTransaction(
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
        val newId = generateTransactionId()
        val now = getCurrentDateTimeString()
        val today = getCurrentDateString()
        val status = if (isDraft) TransactionStatus.DRAFT else TransactionStatus.PENDING_REVIEW

        val tx = T_TRANSACTION(
            transactionId = newId,
            requester = _currentUser.value.name,
            employeeId = _currentUser.value.userId,
            department = department,
            costCenter = costCenter,
            transactionType = transactionType,
            purpose = purpose,
            requestDate = today,
            requiredDate = requiredDate,
            amount = amount,
            currentStatus = status,
            createdAt = now,
            updatedAt = now
        )

        val detail = T_TRANSACTION_DETAIL(
            detailId = "DET-${System.currentTimeMillis() % 100000}",
            transactionId = newId,
            expenseCategory = expenseCategory,
            description = description,
            estimatedAmount = amount,
            actualAmount = 0L
        )

        val doc = T_DOCUMENT_LOG(
            documentId = "DOC-${System.currentTimeMillis() % 100000}",
            transactionId = newId,
            documentName = "Lampiran_Pengajuan_${newId}.pdf",
            documentType = "PROPOSAL",
            documentStatus = "ATTACHED",
            documentReference = "REF-AUTO-${System.currentTimeMillis() % 10000}",
            uploadedAt = now
        )

        val audit = T_AUDIT_LOG(
            auditId = "AUD-${System.currentTimeMillis()}",
            transactionId = newId,
            user = _currentUser.value.name,
            action = if (isDraft) "SAVE_DRAFT" else "SUBMIT_REQUEST",
            oldStatus = "NONE",
            newStatus = status.label,
            timestamp = now,
            description = if (isDraft) "Pengajuan disimpan sebagai Draft" else "Pemohon berhasil mengirimkan pengajuan ke EA"
        )

        _transactions.value = listOf(tx) + _transactions.value
        _transactionDetails.value = _transactionDetails.value + (newId to listOf(detail))
        _documentLogs.value = _documentLogs.value + (newId to listOf(doc))
        _auditLogs.value = listOf(audit) + _auditLogs.value

        addNotification(
            title = if (isDraft) "Draft Tersimpan" else "Pengajuan Dibuat",
            message = "Pengajuan $newId berhasil dibuat dengan status ${status.label}.",
            transactionId = newId
        )

        return newId
    }

    /**
     * 4. Tinjauan EA / Process Owner
     */
    fun reviewTransactionByEA(transactionId: String, isApproved: Boolean, comment: String) {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()
        val nextStatus = if (isApproved) TransactionStatus.PENDING_APPROVAL else TransactionStatus.NEED_REVISION

        updateTransactionStatus(transactionId, nextStatus)

        val approvalLog = T_APPROVAL_LOG(
            approvalLogId = "APL-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            approver = "${_currentUser.value.name} (EA)",
            action = if (isApproved) "REVIEW_PASS" else "NEED_REVISION",
            comment = comment.ifBlank { if (isApproved) "Data & kelengkapan dokumen telah ditinjau lengkap" else "Mohon perbaiki data/dokumen pendukung" },
            timestamp = now
        )

        val currentLogs = _approvalLogs.value[transactionId] ?: emptyList()
        _approvalLogs.value = _approvalLogs.value + (transactionId to (currentLogs + approvalLog))

        logAudit(
            transactionId = transactionId,
            action = if (isApproved) "EA_REVIEW_PASS" else "EA_NEED_REVISION",
            oldStatus = currentTx.currentStatus.label,
            newStatus = nextStatus.label,
            description = "EA: ${approvalLog.comment}"
        )

        addNotification(
            title = if (isApproved) "Lolos Tinjauan EA" else "Pengajuan Perlu Revisi",
            message = "Pengajuan $transactionId: ${approvalLog.comment}",
            transactionId = transactionId
        )
    }

    /**
     * 5. Persetujuan Approver (Approval / Rejection)
     */
    fun processApproval(transactionId: String, isApproved: Boolean, comment: String) {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()
        val nextStatus = if (isApproved) TransactionStatus.APPROVED else TransactionStatus.REJECTED

        updateTransactionStatus(transactionId, nextStatus)

        val approvalLog = T_APPROVAL_LOG(
            approvalLogId = "APL-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            approver = "${_currentUser.value.name} (Approver)",
            action = if (isApproved) "APPROVED" else "REJECTED",
            comment = comment.ifBlank { if (isApproved) "Persetujuan disetujui sesuai matrix otoritas" else "Ditolak oleh Approver" },
            timestamp = now
        )

        val currentLogs = _approvalLogs.value[transactionId] ?: emptyList()
        _approvalLogs.value = _approvalLogs.value + (transactionId to (currentLogs + approvalLog))

        logAudit(
            transactionId = transactionId,
            action = if (isApproved) "APPROVER_APPROVED" else "APPROVER_REJECTED",
            oldStatus = currentTx.currentStatus.label,
            newStatus = nextStatus.label,
            description = "Approver: ${approvalLog.comment}"
        )

        addNotification(
            title = if (isApproved) "Transaksi Disetujui" else "Pengajuan Ditolak",
            message = "Transaksi $transactionId: ${approvalLog.comment}",
            transactionId = transactionId
        )

        // 6. Jika approved, siapkan dokumen standar dan lanjutkan ke PENDING DIGITAL SIGNATURE
        if (isApproved) {
            prepareDocumentForSignature(transactionId)
        }
    }

    /**
     * 6. Pembuatan Dokumen Standar Advance
     */
    fun prepareDocumentForSignature(transactionId: String) {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()
        val nextStatus = TransactionStatus.PENDING_DIGITAL_SIGNATURE

        updateTransactionStatus(transactionId, nextStatus)

        val doc = T_DOCUMENT_LOG(
            documentId = "DOC-STD-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            documentName = "Form_Advance_Standar_${transactionId}.pdf",
            documentType = "ADVANCE_FORM",
            documentStatus = "GENERATED",
            documentReference = "GEN-PSI-${System.currentTimeMillis() % 10000}",
            uploadedAt = now
        )

        val currentDocs = _documentLogs.value[transactionId] ?: emptyList()
        _documentLogs.value = _documentLogs.value + (transactionId to (currentDocs + doc))

        logAudit(
            transactionId = transactionId,
            action = "DOCUMENT_GENERATED",
            oldStatus = currentTx.currentStatus.label,
            newStatus = nextStatus.label,
            description = "Sistem telah membuat Form Dokumen Standar Advance $transactionId"
        )
    }

    /**
     * 7. Tanda Tangan Digital (Simulasi E-Signature)
     * CATATAN: Simulasi e-signature prototype (belum integrasi live Privy API).
     */
    fun simulateDigitalSignature(transactionId: String, signerName: String) {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()
        val nextStatus = TransactionStatus.READY_FOR_FINANCE

        updateTransactionStatus(transactionId, nextStatus)

        val certDoc = T_DOCUMENT_LOG(
            documentId = "DOC-SIG-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            documentName = "Sertifikat_Digital_Signature_${transactionId}.pdf",
            documentType = "SIGNATURE_CERT",
            documentStatus = "SIGNED",
            documentReference = "DS-CERT-${(System.currentTimeMillis() * 31).toString(16).take(8).uppercase()}",
            uploadedAt = now
        )

        val currentDocs = _documentLogs.value[transactionId] ?: emptyList()
        _documentLogs.value = _documentLogs.value + (transactionId to (currentDocs + certDoc))

        logAudit(
            transactionId = transactionId,
            action = "DIGITAL_SIGNATURE_COMPLETED",
            oldStatus = currentTx.currentStatus.label,
            newStatus = nextStatus.label,
            description = "Dokumen ditandatangani secara digital oleh $signerName (Simulasi Hash Certified)"
        )

        addNotification(
            title = "Transaksi Ready for Finance",
            message = "Transaksi $transactionId telah ditandatangani dan siap diproses oleh Finance.",
            transactionId = transactionId
        )
    }

    /**
     * 8 & 9. Proses Keuangan & Pembayaran
     */
    fun updateFinanceStatus(
        transactionId: String,
        targetStatus: TransactionStatus,
        paymentMethod: String? = null,
        paymentRef: String? = null,
        remarks: String? = null
    ) {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()

        updateTransactionStatus(transactionId, targetStatus)

        val currentFin = _financeLogs.value[transactionId]
        val updatedFin = currentFin?.copy(
            processingDate = if (targetStatus == TransactionStatus.PROCESSING) now else currentFin.processingDate,
            paymentDate = if (targetStatus == TransactionStatus.PAID) now else currentFin.paymentDate,
            paymentReference = paymentRef ?: currentFin.paymentReference,
            paymentMethod = paymentMethod ?: currentFin.paymentMethod,
            financeStatus = targetStatus.label,
            remarks = remarks ?: currentFin.remarks
        ) ?: T_FINANCE_LOG(
            financeLogId = "FIN-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            receivedDate = now,
            processingDate = if (targetStatus == TransactionStatus.PROCESSING) now else null,
            paymentDate = if (targetStatus == TransactionStatus.PAID) now else null,
            paymentReference = paymentRef,
            paymentMethod = paymentMethod ?: "BANK TRANSFER MANDIRI",
            financeStatus = targetStatus.label,
            remarks = remarks
        )

        _financeLogs.value = _financeLogs.value + (transactionId to updatedFin)

        logAudit(
            transactionId = transactionId,
            action = "FINANCE_${targetStatus.name}",
            oldStatus = currentTx.currentStatus.label,
            newStatus = targetStatus.label,
            description = "Finance: ${remarks ?: targetStatus.label}"
        )

        addNotification(
            title = "Update Status Keuangan",
            message = "Transaksi $transactionId status telah diperbarui ke ${targetStatus.label}.",
            transactionId = transactionId
        )
    }

    /**
     * 10. Settlement (Input pengeluaran riil & bukti kuitansi)
     */
    fun submitSettlement(
        transactionId: String,
        actualExpense: Long,
        remarks: String,
        receiptName: String
    ) {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()
        val today = getCurrentDateString()
        val difference = currentTx.amount - actualExpense

        val nextStatus = TransactionStatus.SETTLEMENT_PENDING
        updateTransactionStatus(transactionId, nextStatus)

        val settlement = T_SETTLEMENT(
            settlementId = "SET-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            advanceAmount = currentTx.amount,
            actualExpense = actualExpense,
            difference = difference,
            settlementDate = today,
            verificationStatus = "PENDING_VERIFICATION",
            remarks = remarks
        )
        _settlements.value = _settlements.value + (transactionId to settlement)

        // Attach receipt document
        val receiptDoc = T_DOCUMENT_LOG(
            documentId = "DOC-RCP-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            documentName = receiptName.ifBlank { "Kwitansi_Realisasi_${transactionId}.pdf" },
            documentType = "RECEIPT",
            documentStatus = "ATTACHED",
            documentReference = "RCP-REF-${System.currentTimeMillis() % 10000}",
            uploadedAt = now
        )
        val currentDocs = _documentLogs.value[transactionId] ?: emptyList()
        _documentLogs.value = _documentLogs.value + (transactionId to (currentDocs + receiptDoc))

        val diffLabel = if (difference >= 0) "Pengembalian sisa: Rp %,d".format(Locale.GERMAN, difference)
        else "Klaim tambahan: Rp %,d".format(Locale.GERMAN, -difference)

        logAudit(
            transactionId = transactionId,
            action = "SETTLEMENT_SUBMITTED",
            oldStatus = currentTx.currentStatus.label,
            newStatus = nextStatus.label,
            description = "Pemohon mengirim settlement: Realisasi Rp %,d ($diffLabel)".format(Locale.GERMAN, actualExpense)
        )

        addNotification(
            title = "Settlement Diajukan",
            message = "Settlement $transactionId diajukan oleh pemohon ($diffLabel).",
            transactionId = transactionId
        )
    }

    /**
     * 11. Verifikasi & Rekonsiliasi oleh Finance
     */
    fun verifySettlementByFinance(transactionId: String, isApproved: Boolean, comment: String) {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()
        val nextStatus = if (isApproved) TransactionStatus.SETTLEMENT_VERIFIED else TransactionStatus.NEED_REVISION

        updateTransactionStatus(transactionId, nextStatus)

        val currentSet = _settlements.value[transactionId]
        if (currentSet != null) {
            val updatedSet = currentSet.copy(
                verificationStatus = if (isApproved) "SETTLEMENT_VERIFIED" else "REVISION_REQUESTED",
                remarks = "${currentSet.remarks} | Finance: $comment"
            )
            _settlements.value = _settlements.value + (transactionId to updatedSet)
        }

        logAudit(
            transactionId = transactionId,
            action = if (isApproved) "SETTLEMENT_VERIFIED" else "SETTLEMENT_REJECTED",
            oldStatus = currentTx.currentStatus.label,
            newStatus = nextStatus.label,
            description = "Finance settlement verifikasi: $comment"
        )

        addNotification(
            title = if (isApproved) "Settlement Diverifikasi" else "Settlement Perlu Perbaikan",
            message = "Transaksi $transactionId: $comment",
            transactionId = transactionId
        )
    }

    /**
     * 12 & 13. Document Filing & Tutup Transaksi (CLOSED)
     */
    fun closeTransaction(transactionId: String, filingArchiveFolder: String = "ARSIP-AEMS-2026") {
        val currentTx = _transactions.value.find { it.transactionId == transactionId } ?: return
        val now = getCurrentDateTimeString()
        val nextStatus = TransactionStatus.CLOSED

        updateTransactionStatus(transactionId, nextStatus)

        // Generate filing summary document
        val filingDoc = T_DOCUMENT_LOG(
            documentId = "DOC-FILE-${System.currentTimeMillis() % 100000}",
            transactionId = transactionId,
            documentName = "Berkas_Filing_Lengkap_${transactionId}.zip",
            documentType = "FILING_ARCHIVE",
            documentStatus = "FILED",
            documentReference = "$filingArchiveFolder/$transactionId",
            uploadedAt = now
        )

        val currentDocs = _documentLogs.value[transactionId] ?: emptyList()
        _documentLogs.value = _documentLogs.value + (transactionId to (currentDocs + filingDoc))

        logAudit(
            transactionId = transactionId,
            action = "TRANSACTION_CLOSED",
            oldStatus = currentTx.currentStatus.label,
            newStatus = nextStatus.label,
            description = "Dokumen transaksi diarsipkan ke $filingArchiveFolder. Transaksi ditutup resmi."
        )

        addNotification(
            title = "Transaksi Telah Ditutup",
            message = "Transaksi $transactionId berhasil diselesaikan dan diarsipkan.",
            transactionId = transactionId
        )
    }

    private fun updateTransactionStatus(transactionId: String, newStatus: TransactionStatus) {
        val now = getCurrentDateTimeString()
        _transactions.value = _transactions.value.map { tx ->
            if (tx.transactionId == transactionId) {
                tx.copy(currentStatus = newStatus, updatedAt = now)
            } else {
                tx
            }
        }
    }

    private fun logAudit(
        transactionId: String,
        action: String,
        oldStatus: String,
        newStatus: String,
        description: String
    ) {
        val audit = T_AUDIT_LOG(
            auditId = "AUD-${System.currentTimeMillis()}",
            transactionId = transactionId,
            user = _currentUser.value.name,
            action = action,
            oldStatus = oldStatus,
            newStatus = newStatus,
            timestamp = getCurrentDateTimeString(),
            description = description
        )
        _auditLogs.value = listOf(audit) + _auditLogs.value
    }

    private fun addNotification(title: String, message: String, transactionId: String) {
        val notif = AemsNotification(
            id = "NOTIF-${System.currentTimeMillis()}",
            title = title,
            message = message,
            transactionId = transactionId,
            timestamp = getCurrentDateTimeString(),
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun resetDemoData() {
        loadInitialData()
    }
}

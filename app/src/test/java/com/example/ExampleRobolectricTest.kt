package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AEMS PSI", appName)
  }

  @Test
  fun `verify full 13 stage workflow transitions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.AemsRepository(context)

    // Stage 1-3: Create new transaction
    val txId = repo.createTransaction(
      department = "Operations Advance",
      costCenter = "CC-OPS-01",
      transactionType = "Operational Field Advance",
      purpose = "Pengadaan palet kayu benih jagung plasma",
      requiredDate = "2026-09-15",
      amount = 8500000L,
      expenseCategory = "Operasional Lapangan & Perlengkapan",
      description = "Biaya 200 unit palet kayu",
      isDraft = false
    )

    var tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.PENDING_REVIEW, tx?.currentStatus)

    // Stage 4: Review by EA
    repo.reviewTransactionByEA(txId, isApproved = true, comment = "Dokumen dan nominal terverifikasi")
    tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.PENDING_APPROVAL, tx?.currentStatus)

    // Stage 5-6: Approval
    repo.processApproval(txId, isApproved = true, comment = "Disetujui untuk operasional")
    tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.PENDING_DIGITAL_SIGNATURE, tx?.currentStatus)

    // Stage 7: E-Signature
    repo.simulateDigitalSignature(txId, signerName = "Budi Santoso")
    tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.READY_FOR_FINANCE, tx?.currentStatus)

    // Stage 8-9: Finance Processing & Paid
    repo.updateFinanceStatus(txId, com.example.model.TransactionStatus.PAID, "BANK TRANSFER MANDIRI", "TRF-PSI-TEST", "Dana dicairkan")
    tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.PAID, tx?.currentStatus)

    // Stage 10: Settlement
    repo.submitSettlement(txId, actualExpense = 8200000L, remarks = "Sisa kas 300rb disetor ke Finance", receiptName = "Kwitansi_Palet.pdf")
    tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.SETTLEMENT_PENDING, tx?.currentStatus)

    val settlement = repo.settlements.value[txId]
    assertEquals(300000L, settlement?.difference)

    // Stage 11: Verification
    repo.verifySettlementByFinance(txId, isApproved = true, comment = "Kuitansi dan refund 300rb klop")
    tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.SETTLEMENT_VERIFIED, tx?.currentStatus)

    // Stage 12-13: Close & Document Filing
    repo.closeTransaction(txId)
    tx = repo.transactions.value.find { it.transactionId == txId }
    assertEquals(com.example.model.TransactionStatus.CLOSED, tx?.currentStatus)
  }
}

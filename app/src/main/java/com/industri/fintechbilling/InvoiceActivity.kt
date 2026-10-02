package com.industri.fintechbilling

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.text.NumberFormat
import java.util.Locale

class InvoiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val jsonPayload = loadMockInvoiceJson()
        val invoice: InvoiceResponse = Gson().fromJson(jsonPayload, InvoiceResponse::class.java)

        // 1. Binding Header
        val tvInvoiceNo = findViewById<TextView>(R.id.tvInvoiceNo)
        tvInvoiceNo.text = invoice.invoiceNumber
        findViewById<TextView>(R.id.tvStatusBadge).text = invoice.paymentStatus
        findViewById<TextView>(R.id.tvTransactionDate).text = invoice.transactionDate.toReadableIndonesianDate()
        findViewById<TextView>(R.id.tvPaymentMethod).text = "Metode Pembayaran: ${invoice.paymentMethod}"

        // 2. Binding Merchant
        findViewById<TextView>(R.id.tvMerchantName).text = invoice.merchant.merchantName
        findViewById<TextView>(R.id.tvMerchantCity).text = invoice.merchant.cityLocation
        val isVerifiedText = if (invoice.merchant.isVerified) "✓ Terverifikasi: RESMI (Ya)" else "Terverifikasi: Tidak"
        findViewById<TextView>(R.id.tvMerchantVerified).text = isVerifiedText

        // 3. Binding Customer
        findViewById<TextView>(R.id.tvCustomerName).text = invoice.customer.fullName
        findViewById<TextView>(R.id.tvCustomerPhone).text = invoice.customer.phone

        // 4. Binding RecyclerView Items
        val rvItems = findViewById<RecyclerView>(R.id.rvInvoiceItems)
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = InvoiceItemAdapter(invoice.items)

        // 5. Binding Summary & Voucher (Defensive Null Safety)
        val summary = invoice.summary
        if (summary != null) {
            findViewById<TextView>(R.id.tvSubtotalAmt).text = rupiahFormat.format(summary.subtotalAmount)
            findViewById<TextView>(R.id.tvDiscountAmt).text = "- ${rupiahFormat.format(summary.discountAmount)}"
            findViewById<TextView>(R.id.tvTaxAmt).text = rupiahFormat.format(summary.taxPpn11)
            findViewById<TextView>(R.id.tvFeeAmt).text = rupiahFormat.format(summary.serviceFee)
            findViewById<TextView>(R.id.tvTotalPaid).text = rupiahFormat.format(summary.totalPaid)

            val tvVoucherInfo = findViewById<TextView>(R.id.tvVoucherInfo)
            val tvVoucherCode = findViewById<TextView>(R.id.tvVoucherCode)
            val tvVoucherDiscount = findViewById<TextView>(R.id.tvVoucherDiscount)

            if (!summary.voucherCode.isNullOrBlank()) {
                tvVoucherInfo.text = "🎟 PROMO DIGUNAKAN"
                tvVoucherCode.visibility = View.VISIBLE
                tvVoucherCode.text = "Kode Voucher: ${summary.voucherCode}"

                if (summary.voucherDiscountPercent != null) {
                    tvVoucherDiscount.visibility = View.VISIBLE
                    tvVoucherDiscount.text = "Diskon voucher: ${summary.voucherDiscountPercent}%"
                } else {
                    tvVoucherDiscount.visibility = View.GONE
                }
            } else {
                tvVoucherInfo.text = "🏷 Tidak menggunakan promo"
                tvVoucherCode.visibility = View.GONE
                tvVoucherDiscount.visibility = View.GONE
            }
        }

        // 6. Inovasi: Collapsible Sections
        setupCollapsibleSections()

        // 7. Inovasi: Copy Invoice Number
        val copyAction = {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Invoice Number", invoice.invoiceNumber)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nomor invoice berhasil disalin: ${invoice.invoiceNumber}", Toast.LENGTH_SHORT).show()
        }
        tvInvoiceNo.setOnClickListener { copyAction() }
        findViewById<Button>(R.id.btnCopyInvoice).setOnClickListener { copyAction() }

        // 8. Inovasi: Share Invoice
        findViewById<Button>(R.id.btnShareInvoice).setOnClickListener {
            val totalFormatted = summary?.totalPaid?.let { rupiahFormat.format(it) } ?: "Rp0"
            val shareText = """
                Fintech Billing Invoice

                Nomor Invoice: ${invoice.invoiceNumber}
                Status: ${invoice.paymentStatus}
                Tanggal: ${invoice.transactionDate.toReadableIndonesianDate()}
                Metode Pembayaran: ${invoice.paymentMethod}

                Merchant: ${invoice.merchant.merchantName} (${invoice.merchant.cityLocation})
                Penerima: ${invoice.customer.fullName} (${invoice.customer.phone})

                Total Pembayaran: $totalFormatted
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber}")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Bagikan Invoice Via"))
        }
    }

    private fun setupCollapsibleSections() {
        val btnToggleMerchant = findViewById<View>(R.id.btnToggleMerchant)
        val layoutMerchantDetails = findViewById<View>(R.id.layoutMerchantDetails)
        val tvMerchantChevron = findViewById<TextView>(R.id.tvMerchantToggleChevron)

        btnToggleMerchant.setOnClickListener {
            if (layoutMerchantDetails.visibility == View.VISIBLE) {
                layoutMerchantDetails.visibility = View.GONE
                tvMerchantChevron.text = "▸"
            } else {
                layoutMerchantDetails.visibility = View.VISIBLE
                tvMerchantChevron.text = "▾"
            }
        }

        val btnToggleCustomer = findViewById<View>(R.id.btnToggleCustomer)
        val layoutCustomerDetails = findViewById<View>(R.id.layoutCustomerDetails)
        val tvCustomerChevron = findViewById<TextView>(R.id.tvCustomerToggleChevron)

        btnToggleCustomer.setOnClickListener {
            if (layoutCustomerDetails.visibility == View.VISIBLE) {
                layoutCustomerDetails.visibility = View.GONE
                tvCustomerChevron.text = "▸"
            } else {
                layoutCustomerDetails.visibility = View.VISIBLE
                tvCustomerChevron.text = "▾"
            }
        }

        val btnToggleSummary = findViewById<View>(R.id.btnToggleSummary)
        val layoutSummaryDetails = findViewById<View>(R.id.layoutSummaryDetails)
        val tvSummaryChevron = findViewById<TextView>(R.id.tvSummaryToggleChevron)

        btnToggleSummary.setOnClickListener {
            if (layoutSummaryDetails.visibility == View.VISIBLE) {
                layoutSummaryDetails.visibility = View.GONE
                tvSummaryChevron.text = "▸"
            } else {
                layoutSummaryDetails.visibility = View.VISIBLE
                tvSummaryChevron.text = "▾"
            }
        }
    }

    private fun loadMockInvoiceJson(): String {
        return """
        {
          "invoice_number": "INV-2026-FT9012",
          "transaction_date": "2026-09-23 10:15:00",
          "payment_status": "PAID_SETTLED",
          "payment_method": "QRIS_BCA",

          "merchant": {
            "merchant_id": "MCH-99201",
            "merchant_name": "Mega Elektrindo Ritel",
            "city_location": "Bandung",
            "is_verified": true
          },

          "customer": {
            "customer_id": "CUST-4412",
            "full_name": "Ahmad Fauzi",
            "phone": "081298765432"
          },

          "items": [
            {
              "item_id": "ITM-01",
              "item_name": "Kabel Type-C 65W Fast Charging",
              "qty": 2,
              "unit_price": 75000.0,
              "subtotal": 150000.0
            },
            {
              "item_id": "ITM-02",
              "item_name": "Adaptor GaN Charger 3-Port 100W",
              "qty": 1,
              "unit_price": 320000.0,
              "subtotal": 320000.0
            },
            {
              "item_id": "ITM-03",
              "item_name": "Mouse Wireless Silent Click Ergonomis",
              "qty": 1,
              "unit_price": 130000.0,
              "subtotal": 130000.0
            }
          ],

          "summary": {
            "subtotal_amount": 600000.0,
            "discount_amount": 50000.0,
            "tax_ppn_11": 60500.0,
            "service_fee": 2500.0,
            "total_paid": 613000.0,

            "voucher_code": null,
            "voucher_discount_percent": null
          }
        }
        """.trimIndent()
    }
}

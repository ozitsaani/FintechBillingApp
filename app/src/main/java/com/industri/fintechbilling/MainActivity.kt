package com.industri.fintechbilling

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnParseBasic = findViewById<Button>(R.id.btnParseBasic)
        val tvOutputBasic = findViewById<TextView>(R.id.tvOutputBasic)
        val btnViewInvoice = findViewById<Button>(R.id.btnViewInvoice)

        btnParseBasic.setOnClickListener {
            val merchantJson = """
                {
                    "merchant_id": "MCH-99201",
                    "merchant_name": "Mega Elektrindo Ritel",
                    "city_location": "Bandung",
                    "is_verified": true
                }
            """.trimIndent()

            val merchant = Gson().fromJson(merchantJson, Merchant::class.java)
            val isVerifiedText = if (merchant.isVerified) "RESMI (Ya)" else "Belum (Tidak)"

            val resultText = """
                BERHASIL DI-PARSING:

                ID Mitra     : ${merchant.merchantId}
                Nama Toko    : ${merchant.merchantName}
                Kota         : ${merchant.cityLocation}
                Terverifikasi: $isVerifiedText
            """.trimIndent()

            tvOutputBasic.text = resultText
            tvOutputBasic.visibility = View.VISIBLE
        }

        btnViewInvoice.setOnClickListener {
            val intent = Intent(this, InvoiceActivity::class.java)
            startActivity(intent)
        }
    }
}

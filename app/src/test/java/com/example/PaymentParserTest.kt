package com.example

import com.example.util.PaymentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentParserTest {

    @Test
    fun parsesGopayRupiahAmountAndSender() {
        val title = "Pembayaran Berhasil"
        val text = "Anda menerima pembayaran sebesar Rp150.000 dari AHMAD BUDI"

        assertEquals(150000, PaymentParser.parseAmount(title, text))
        assertEquals("AHMAD BUDI", PaymentParser.parseSender(title, text))
    }

    @Test
    fun parsesDanaCommaFormattedAmount() {
        val text = "Transfer masuk IDR 12,000.00 dari SITI NUR"

        assertEquals(12000, PaymentParser.parseAmount(text))
        assertEquals("SITI NUR", PaymentParser.parseSender(text))
    }

    @Test
    fun parsesShopeePayNumberBeforeRupiah() {
        val text = "Kamu menerima 25.500 rupiah dari RUDI"

        assertEquals(25500, PaymentParser.parseAmount(text))
        assertEquals("RUDI", PaymentParser.parseSender(text))
    }

    @Test
    fun ignoresStandaloneReferenceNumberWithoutAmountContext() {
        val text = "Transaksi diproses. No referensi 987654321012. Simpan bukti ini."

        assertNull(PaymentParser.parseAmount(text))
    }

    @Test
    fun parsesStandaloneAmountWhenNearbyPaymentContextExists() {
        val text = "Pembayaran masuk sebesar 15000 dari pelanggan"

        assertEquals(15000, PaymentParser.parseAmount(text))
    }
}

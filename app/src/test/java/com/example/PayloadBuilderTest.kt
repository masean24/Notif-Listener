package com.example

import com.example.data.model.NotificationLog
import com.example.data.model.WebhookTarget
import com.example.util.PayloadBuilder
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PayloadBuilderTest {

    @Test
    fun simplePayloadShapeStaysCompatible() {
        val payload = PayloadBuilder.buildPayload(
            target = WebhookTarget(name = "Server", url = "https://example.test", secret = ""),
            log = sampleLog(),
            deviceId = "device-1"
        )

        val json = JSONObject(payload)
        assertEquals("Anda menerima pembayaran sebesar Rp150.000 dari AHMAD BUDI", json.getString("message"))
        assertEquals(1, json.length())
    }

    @Test
    fun extendedPayloadKeepsExistingFields() {
        val payload = PayloadBuilder.buildPayload(
            target = WebhookTarget(name = "Server", url = "https://example.test", secret = "", payloadMode = "extended"),
            log = sampleLog(),
            deviceId = "device-1"
        )

        val json = JSONObject(payload)
        assertEquals("payment_notification", json.getString("event"))
        assertEquals(150000, json.getInt("amount"))
        assertEquals("AHMAD BUDI", json.getString("sender"))
        assertEquals("device-1", json.getString("device_id"))
    }

    private fun sampleLog(): NotificationLog {
        return NotificationLog(
            packageName = "com.gojek.app",
            appName = "GoPay",
            title = "Pembayaran Berhasil",
            text = "Anda menerima pembayaran sebesar Rp150.000 dari AHMAD BUDI",
            bigText = null,
            timestamp = 1_700_000_000_000,
            amount = 150000,
            dedupeKey = "abc",
            sender = "AHMAD BUDI"
        )
    }
}

package com.example

import com.example.util.TtsPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsPolicyTest {

    @Test
    fun globalModeFollowsGlobalSpeakerSwitch() {
        assertTrue(TtsPolicy.shouldSpeak("global", legacyEnabled = false, globalEnabled = true))
        assertFalse(TtsPolicy.shouldSpeak("global", legacyEnabled = false, globalEnabled = false))
    }

    @Test
    fun forceOnModeOverridesGlobalOff() {
        assertTrue(TtsPolicy.shouldSpeak("force_on", legacyEnabled = false, globalEnabled = false))
    }

    @Test
    fun mutedModeOverridesGlobalOn() {
        assertFalse(TtsPolicy.shouldSpeak("muted", legacyEnabled = false, globalEnabled = true))
    }

    @Test
    fun blankModeKeepsLegacyProfileEnabledBehavior() {
        assertTrue(TtsPolicy.shouldSpeak("", legacyEnabled = true, globalEnabled = false))
        assertFalse(TtsPolicy.shouldSpeak("", legacyEnabled = false, globalEnabled = false))
    }

    @Test
    fun rendersSenderPlaceholderFallback() {
        val text = TtsPolicy.renderTemplate(
            "Pembayaran {amount} dari {sender} via {app_name}",
            amount = 15000,
            appName = "GoPay",
            sender = null
        )

        assertEquals("Pembayaran 15000 dari Pelanggan via GoPay", text)
    }
}

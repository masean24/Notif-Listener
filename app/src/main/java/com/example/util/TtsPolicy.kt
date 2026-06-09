package com.example.util

object TtsPolicy {
    const val MODE_GLOBAL = "global"
    const val MODE_FORCE_ON = "force_on"
    const val MODE_MUTED = "muted"

    fun shouldSpeak(mode: String, legacyEnabled: Boolean, globalEnabled: Boolean): Boolean {
        val effectiveMode = mode.ifBlank {
            if (legacyEnabled) MODE_FORCE_ON else MODE_GLOBAL
        }

        return when (effectiveMode) {
            MODE_FORCE_ON -> true
            MODE_MUTED -> false
            else -> globalEnabled
        }
    }

    fun renderTemplate(template: String, amount: Int, appName: String, sender: String?): String {
        return template
            .replace("{amount}", amount.toString())
            .replace("{app_name}", appName)
            .replace("{sender}", sender ?: "Pelanggan")
    }
}

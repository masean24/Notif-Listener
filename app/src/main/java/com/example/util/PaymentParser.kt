package com.example.util

import java.util.regex.Pattern

object PaymentParser {

    /**
     * Parses the rupiah nominal from title, text, or bigText.
     * Returns the integer amount, or null if not found.
     */
    fun parseAmount(vararg texts: String?): Int? {
        for (text in texts) {
            if (text.isNullOrEmpty()) continue
            val amount = extractAmount(text)
            if (amount != null) return amount
        }
        return null
    }

    /**
     * Parses the sender name from title, text, or bigText.
     * Returns the sender name string, or null if not found.
     */
    fun parseSender(vararg texts: String?): String? {
        for (text in texts) {
            if (text.isNullOrEmpty()) continue
            val sender = extractSender(text)
            if (sender != null) return sender
        }
        return null
    }

    private fun extractSender(text: String): String? {
        // Replace multiple spaces with a single space to clean input
        val plainText = text.replace(Regex("\\s+"), " ")

        // Pattern 1: Look for "dari <name>" or "oleh <name>" or "pengirim <name>"
        // matching alphabetic characters, digits, spaces, hyphens but stopping if special keywords appear
        val dariPattern = Pattern.compile("(?i)(?:dari|oleh|pengirim[: ]+)\\s*([A-Za-z0-9.\\-/]+(?:\\s+[A-Za-z0-9.\\-/]+){0,4})")
        val matcher = dariPattern.matcher(plainText)
        if (matcher.find()) {
            val rawName = matcher.group(1) ?: ""
            val cleanName = cleanSenderName(rawName)
            if (isEligibleSenderName(cleanName)) {
                return cleanName
            }
        }

        // Pattern 2: Look for transfer "dari <name> Rp ..."
        val tfPattern = Pattern.compile("(?i)transfer\\s+(?:masuk|dari)\\s+([A-Za-z0-9.\\-/]+(?:\\s+[A-Za-z0-9.\\-/]+){0,4})")
        val tfMatcher = tfPattern.matcher(plainText)
        if (tfMatcher.find()) {
            val rawName = tfMatcher.group(1) ?: ""
            val cleanName = cleanSenderName(rawName)
            if (isEligibleSenderName(cleanName)) {
                return cleanName
            }
        }

        return null
    }

    private fun cleanSenderName(raw: String): String {
        var name = raw.trim()
        
        // Remove trailing punctuation
        name = name.removeSuffix(".").removeSuffix(",").removeSuffix("!").trim()

        val lowercase = name.lowercase()
        // If it contains terminal phrases in typical notification logs, split before them
        if (lowercase.contains(" sebesar")) {
            val idx = lowercase.indexOf(" sebesar")
            name = name.substring(0, idx).trim()
        }
        if (lowercase.contains(" berhasil")) {
            val idx = lowercase.indexOf(" berhasil")
            name = name.substring(0, idx).trim()
        }
        if (lowercase.contains(" sejumlah")) {
            val idx = lowercase.indexOf(" sejumlah")
            name = name.substring(0, idx).trim()
        }
        if (lowercase.contains(" ke ")) {
            val idx = lowercase.indexOf(" ke ")
            name = name.substring(0, idx).trim()
        }
        if (lowercase.contains(" pada ")) {
            val idx = lowercase.indexOf(" pada ")
            name = name.substring(0, idx).trim()
        }
        
        // Strip any trailing digits or currency markers if attached
        val rpIndex = name.lowercase().lastIndexOf(" rp")
        if (rpIndex > 0) {
            name = name.substring(0, rpIndex).trim()
        }
        
        return name
    }

    private fun isEligibleSenderName(name: String): Boolean {
        if (name.length < 2) return false
        val lower = name.lowercase()
        
        // If it has no alphabetic characters
        if (lower.replace(Regex("[^a-z]"), "").isEmpty()) {
            return false
        }
        
        // Remove common system/UI labels which aren't actual human names
        val noiseWords = listOf(
            "rekening", "bank", "shopeepay", "gopay", "dana", "ovo", "qris", "aplikasi", "saldo", "pembayaran", "kiriman", "transfer", "nominal", "sukses"
        )
        
        for (noise in noiseWords) {
            if (lower == noise) return false
        }
        
        return true
    }

    private fun extractAmount(text: String): Int? {
        // Replace multiple spaces with a single space to clean input
        val plainText = text.replace(Regex("\\s+"), " ")

        // Pattern 1: Rp or IDR followed by numbers (with dots/commas/optional cents)
        // e.g. Rp 1.250.000 or Rp. 12000 or IDR 12,000.00
        val rpPattern = Pattern.compile("(?i)(?:Rp\\.?|IDR)\\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+)")
        val rpMatcher = rpPattern.matcher(plainText)
        if (rpMatcher.find()) {
            val numStr = rpMatcher.group(1) ?: ""
            val parsed = sanitizeAndParse(numStr)
            if (parsed != null && parsed >= 100) return parsed
        }

        // Pattern 2: numbers followed by rupiah/rp
        // e.g. 12.000 rupiah or 150000 rp
        val rupiahPrefixPattern = Pattern.compile("(?i)([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+)\\s*(?:rupiah|rp)")
        val rpPrefixMatcher = rupiahPrefixPattern.matcher(plainText)
        if (rpPrefixMatcher.find()) {
            val numStr = rpPrefixMatcher.group(1) ?: ""
            val parsed = sanitizeAndParse(numStr)
            if (parsed != null) return parsed
        }

        // Pattern 3: Standalone formatted numbers like "12.000" or large standalone "15000" (> 1000)
        // e.g. "menerima transfer sebesar 12.000 dari..." or "pembayaran 50000 berhasil"
        val standalonePattern = Pattern.compile("\\b([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,})\\b")
        val standaloneMatcher = standalonePattern.matcher(plainText)
        while (standaloneMatcher.find()) {
            val numStr = standaloneMatcher.group(1) ?: ""
            val parsed = sanitizeAndParse(numStr)
            if (parsed != null && parsed >= 1000) {
                return parsed
            }
        }

        return null
    }

    private fun sanitizeAndParse(raw: String): Int? {
        var str = raw.trim()
        
        // Strip cents matching .00 or ,00
        if (str.endsWith(".00") || str.endsWith(",00")) {
            str = str.substring(0, str.length - 3)
        }
        
        // Remove all separators (dots and commas)
        val cleanStr = str.replace(".", "").replace(",", "")
        return cleanStr.toIntOrNull()
    }
}

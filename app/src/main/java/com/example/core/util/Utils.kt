package com.example.core.util

import android.telephony.PhoneNumberUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object OtpDetector {
    private val OTP_PATTERNS = listOf(
        Pattern.compile("""(?i)\b(?:code|otp|pin|verification\s*code|passcode)\b[:\s\-—]+([0-9]{4,8})\b"""),
        Pattern.compile("""(?i)\b([0-9]{4,8})\b\s+(?:is\s+your\s+(?:verification|security|login|otp|code))"""),
        Pattern.compile("""(?i)\bG-([0-9]{6})\b"""),
        Pattern.compile("""\b([0-9]{6})\b""")
    )

    fun extractOtp(body: String): String? {
        if (body.isBlank()) return null
        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val code = matcher.group(1) ?: matcher.group(0)
                if (code.all { it.isDigit() } && code.length in 4..8) {
                    return code
                }
            }
        }
        return null
    }
}

object SpamDetector {
    private val SPAM_KEYWORDS = listOf(
        "winner", "won cash", "claim your prize", "lottery", "urgent loan",
        "crypto profit", "congratulations you have won", "wire transfer",
        "bank account suspended", "verify your card immediately",
        "casino free spins", "guaranteed returns", "investment payout"
    )

    fun isSuspectedSpam(address: String, body: String): Boolean {
        val lowerBody = body.lowercase(Locale.ROOT)
        // Check keywords
        val keywordMatch = SPAM_KEYWORDS.any { lowerBody.contains(it) }
        if (keywordMatch) return true

        // Suspicious short links with financial hooks
        val hasSuspiciousLink = lowerBody.contains("bit.ly/") || 
                lowerBody.contains("tinyurl.com/") || 
                lowerBody.contains("t.co/")
        val hasFinancialHook = lowerBody.contains("account") || 
                lowerBody.contains("password") || 
                lowerBody.contains("urgent") || 
                lowerBody.contains("claim")

        return hasSuspiciousLink && hasFinancialHook
    }
}

object GsmSegmentCounter {
    data class SegmentResult(
        val charCount: Int,
        val charsRemainingInCurrentSegment: Int,
        val segmentCount: Int,
        val isUcs2: Boolean
    )

    private val GSM7_CHARS = "@£\$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ\u001bÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà"

    fun calculate(text: String): SegmentResult {
        if (text.isEmpty()) {
            return SegmentResult(
                charCount = 0,
                charsRemainingInCurrentSegment = 160,
                segmentCount = 1,
                isUcs2 = false
            )
        }

        val isGsm7 = text.all { GSM7_CHARS.contains(it) }
        val charCount = text.length

        return if (isGsm7) {
            if (charCount <= 160) {
                SegmentResult(charCount, 160 - charCount, 1, false)
            } else {
                val segments = (charCount + 152) / 153
                val remaining = (segments * 153) - charCount
                SegmentResult(charCount, remaining, segments, false)
            }
        } else {
            if (charCount <= 70) {
                SegmentResult(charCount, 70 - charCount, 1, true)
            } else {
                val segments = (charCount + 66) / 67
                val remaining = (segments * 67) - charCount
                SegmentResult(charCount, remaining, segments, true)
            }
        }
    }
}

object DateFormatter {
    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val now = System.currentTimeMillis()
        val diffMs = now - timestamp

        if (diffMs < 60_000) {
            return "Now"
        }

        val diffMinutes = diffMs / 60_000
        if (diffMinutes < 60) {
            return "${diffMinutes}m ago"
        }

        val diffHours = diffMinutes / 60
        if (diffHours < 24) {
            val calNow = Calendar.getInstance()
            val calMsg = Calendar.getInstance().apply { timeInMillis = timestamp }
            if (calNow.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR) &&
                calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR)
            ) {
                return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
            }
        }

        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val calMsg = Calendar.getInstance().apply { timeInMillis = timestamp }
        if (calYesterday.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR) &&
            calYesterday.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR)
        ) {
            return "Yesterday"
        }

        val diffDays = diffMs / (24 * 3600_000)
        return when {
            diffDays < 7 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun formatBubbleTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDayHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        if (now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Today"
        }

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (yesterday.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Yesterday"
        }

        return SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

object PhoneNumberHelper {
    fun normalize(number: String): String {
        return number.replace(Regex("[^0-9+]"), "")
    }

    fun format(number: String): String {
        return PhoneNumberUtils.formatNumber(number, Locale.getDefault().country) ?: number
    }
}

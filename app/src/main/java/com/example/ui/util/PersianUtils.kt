package com.example.ui.util

object PersianUtils {

    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: Any?): String {
        if (input == null) return ""
        val str = input.toString()
        val sb = StringBuilder(str.length)
        for (char in str) {
            val index = char - '0'
            if (index in 0..9) {
                sb.append(persianDigits[index])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    /**
     * Safely formats duration string or returns null if null / "N/A" / "null" / empty
     */
    fun formatDuration(duration: String?): String? {
        if (duration.isNullOrBlank()) return null
        val clean = duration.trim()
        if (clean.equals("null", ignoreCase = true) || clean.equals("N/A", ignoreCase = true)) {
            return null
        }
        // If it's already localized or text, format numbers
        val persianNum = toPersianDigits(clean)
        val hasUnit = persianNum.contains("دقیقه") || persianNum.contains("ساعت")
        val isClockFormat = persianNum.contains(":")
        return if (!hasUnit && !isClockFormat) {
            "$persianNum دقیقه"
        } else {
            persianNum
        }
    }

    /**
     * Formats rating with 1 decimal place and Persian digits
     */
    fun formatRating(rating: Double): String {
        return if (rating <= 0.0) {
            toPersianDigits("0.0")
        } else {
            val formatted = String.format(java.util.Locale.US, "%.1f", rating)
            toPersianDigits(formatted)
        }
    }

    /**
     * Formats seconds into MM:SS or HH:MM:SS with Persian digits
     */
    fun formatTime(totalSeconds: Long): String {
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        val timeString = if (hours > 0) {
            String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        }
        return toPersianDigits(timeString)
    }
}

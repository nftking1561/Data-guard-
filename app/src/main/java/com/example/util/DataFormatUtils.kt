package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object DataFormatUtils {

    fun formatBytes(bytes: Long, useBinary: Boolean = false): String {
        if (bytes <= 0) return "0 MB"
        val unit = if (useBinary) 1024.0 else 1000.0

        return when {
            bytes >= unit * unit * unit -> {
                val gb = bytes / (unit * unit * unit)
                String.format(Locale.US, "%.2f GB", gb)
            }
            bytes >= unit * unit -> {
                val mb = bytes / (unit * unit)
                if (mb >= 10) String.format(Locale.US, "%.0f MB", mb)
                else String.format(Locale.US, "%.1f MB", mb)
            }
            bytes >= unit -> {
                val kb = bytes / unit
                String.format(Locale.US, "%.0f KB", kb)
            }
            else -> "$bytes B"
        }
    }

    /**
     * Friendly, teenager- and grandma-understandable explanation of the byte size.
     */
    fun getGrandmaExplanation(bytes: Long): String {
        val gb = bytes / (1000.0 * 1000.0 * 1000.0)
        val mb = bytes / (1000.0 * 1000.0)
        return when {
            gb >= 15.0 -> "Enough for lots of streaming and daily video browsing"
            gb >= 5.0 -> "Solid amount for regular everyday internet use"
            gb >= 1.0 -> "Around ${gb.toInt()} gigabytes"
            mb >= 600.0 -> "Over half a gigabyte of data"
            mb >= 400.0 -> "About half a gigabyte"
            mb >= 100.0 -> "A moderate amount of daily internet use"
            mb >= 20.0 -> "A small amount of data, mostly light browsing"
            else -> "Very light usage"
        }
    }

    fun formatCurrencyNgn(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = 0
        return "₦" + formatter.format(amount)
    }

    fun formatDate(epochMs: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return sdf.format(Date(epochMs))
    }

    fun formatShortDate(epochMs: Long): String {
        val sdf = SimpleDateFormat("MMM d", Locale.US)
        return sdf.format(Date(epochMs))
    }

    fun formatPercentageDiff(actual: Long, baseline: Long): String {
        if (baseline <= 0) return "Normal"
        val diff = actual - baseline
        val pct = (diff.toDouble() / baseline) * 100
        return if (pct >= 0) {
            "+${String.format(Locale.US, "%.0f", pct)}% above normal"
        } else {
            "${String.format(Locale.US, "%.0f", abs(pct))}% below normal"
        }
    }
}

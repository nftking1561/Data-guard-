package com.example.data.model

import android.graphics.drawable.Drawable

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val uid: Int,
    val isSystemApp: Boolean = false,
    val category: AppCategory = AppCategory.OTHER,
    val mobileBytes: Long = 0L,
    val wifiBytes: Long = 0L,
    val foregroundBytes: Long = 0L,
    val backgroundBytes: Long = 0L,
    val icon: Drawable? = null
) {
    val totalBytes: Long get() = mobileBytes + wifiBytes
    val backgroundRatio: Float get() = if (mobileBytes > 0) backgroundBytes.toFloat() / mobileBytes else 0f
    val hasBackgroundDominance: Boolean get() = mobileBytes > 20 * 1024 * 1024L && backgroundRatio > 0.35f
}

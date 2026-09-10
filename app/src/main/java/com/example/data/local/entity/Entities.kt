package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_plans")
data class DataPlanEntity(
    @PrimaryKey val id: Long = 1L,
    val networkCarrier: String,
    val planName: String,
    val planSizeBytes: Long,
    val priceNgn: Double,
    val startDateEpochMs: Long,
    val expiryDateEpochMs: Long,
    val autoRenewal: Boolean,
    val budgetModeName: String,
    val manualCarrierBalanceBytes: Long?,
    val manualCarrierBalanceTimestamp: Long?
)

@Entity(tableName = "daily_usage_snapshots", primaryKeys = ["dateString", "packageName"])
data class DailyUsageSnapshotEntity(
    val dateString: String, // yyyy-MM-dd
    val packageName: String,
    val appName: String,
    val mobileBytes: Long,
    val wifiBytes: Long,
    val foregroundBytes: Long,
    val backgroundBytes: Long,
    val timestamp: Long
)

@Entity(tableName = "carrier_reconciliations")
data class CarrierReconciliationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val carrierReportedBytes: Long,
    val deviceMeasuredBytes: Long,
    val differenceBytes: Long,
    val notes: String = ""
)

@Entity(tableName = "app_alerts")
data class AppAlertEntity(
    @PrimaryKey val packageName: String,
    val dailyLimitBytes: Long = 0L,
    val highUsageWarningEnabled: Boolean = true,
    val backgroundRestrictedLocally: Boolean = false
)

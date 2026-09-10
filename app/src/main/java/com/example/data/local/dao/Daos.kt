package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AppAlertEntity
import com.example.data.local.entity.CarrierReconciliationEntity
import com.example.data.local.entity.DailyUsageSnapshotEntity
import com.example.data.local.entity.DataPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataPlanDao {
    @Query("SELECT * FROM data_plans WHERE id = 1 LIMIT 1")
    fun getDataPlanFlow(): Flow<DataPlanEntity?>

    @Query("SELECT * FROM data_plans WHERE id = 1 LIMIT 1")
    suspend fun getDataPlan(): DataPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(plan: DataPlanEntity)

    @Query("DELETE FROM data_plans WHERE id = 1")
    suspend fun deletePlan()
}

@Dao
interface UsageSnapshotDao {
    @Query("SELECT * FROM daily_usage_snapshots WHERE dateString = :dateString")
    suspend fun getSnapshotsForDate(dateString: String): List<DailyUsageSnapshotEntity>

    @Query("SELECT * FROM daily_usage_snapshots ORDER BY timestamp DESC LIMIT 2000")
    suspend fun getAllRecentSnapshots(): List<DailyUsageSnapshotEntity>

    @Query("SELECT DISTINCT dateString FROM daily_usage_snapshots ORDER BY dateString DESC")
    suspend fun getDistinctDates(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<DailyUsageSnapshotEntity>)

    @Query("DELETE FROM daily_usage_snapshots WHERE timestamp < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)
}

@Dao
interface CarrierDao {
    @Query("SELECT * FROM carrier_reconciliations ORDER BY timestamp DESC LIMIT 20")
    fun getReconciliationsFlow(): Flow<List<CarrierReconciliationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReconciliation(entity: CarrierReconciliationEntity)
}

@Dao
interface AppAlertDao {
    @Query("SELECT * FROM app_alerts")
    fun getAllAlertsFlow(): Flow<List<AppAlertEntity>>

    @Query("SELECT * FROM app_alerts WHERE packageName = :packageName")
    suspend fun getAlertForPackage(packageName: String): AppAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAlert(entity: AppAlertEntity)
}

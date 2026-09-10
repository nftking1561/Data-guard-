package com.example.data.repository

import com.example.data.local.dao.AppAlertDao
import com.example.data.local.dao.CarrierDao
import com.example.data.local.dao.DataPlanDao
import com.example.data.local.entity.AppAlertEntity
import com.example.data.local.entity.CarrierReconciliationEntity
import com.example.data.local.entity.DataPlanEntity
import com.example.data.model.BudgetMode
import com.example.data.model.DataPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataPlanRepository(
    private val dataPlanDao: DataPlanDao,
    private val carrierDao: CarrierDao,
    private val appAlertDao: AppAlertDao
) {

    val activePlanFlow: Flow<DataPlan?> = dataPlanDao.getDataPlanFlow().map { entity ->
        entity?.toDomainModel()
    }

    val reconciliationsFlow: Flow<List<CarrierReconciliationEntity>> = carrierDao.getReconciliationsFlow()

    val appAlertsFlow: Flow<List<AppAlertEntity>> = appAlertDao.getAllAlertsFlow()

    suspend fun getActivePlan(): DataPlan? {
        return dataPlanDao.getDataPlan()?.toDomainModel()
    }

    suspend fun savePlan(plan: DataPlan) {
        val entity = DataPlanEntity(
            id = 1L,
            networkCarrier = plan.networkCarrier,
            planName = plan.planName,
            planSizeBytes = plan.planSizeBytes,
            priceNgn = plan.priceNgn,
            startDateEpochMs = plan.startDateEpochMs,
            expiryDateEpochMs = plan.expiryDateEpochMs,
            autoRenewal = plan.autoRenewal,
            budgetModeName = plan.budgetMode.name,
            manualCarrierBalanceBytes = plan.manualCarrierBalanceBytes,
            manualCarrierBalanceTimestamp = plan.manualCarrierBalanceTimestamp
        )
        dataPlanDao.insertOrUpdate(entity)
    }

    suspend fun deletePlan() {
        dataPlanDao.deletePlan()
    }

    suspend fun recordCarrierBalance(carrierBalanceBytes: Long, deviceMeasuredBytes: Long, notes: String = "") {
        val currentPlan = getActivePlan()
        if (currentPlan != null) {
            savePlan(
                currentPlan.copy(
                    manualCarrierBalanceBytes = carrierBalanceBytes,
                    manualCarrierBalanceTimestamp = System.currentTimeMillis()
                )
            )
        }
        val diff = carrierBalanceBytes - (currentPlan?.let { it.planSizeBytes - deviceMeasuredBytes } ?: carrierBalanceBytes)
        carrierDao.insertReconciliation(
            CarrierReconciliationEntity(
                timestamp = System.currentTimeMillis(),
                carrierReportedBytes = carrierBalanceBytes,
                deviceMeasuredBytes = deviceMeasuredBytes,
                differenceBytes = kotlin.math.abs(diff),
                notes = notes
            )
        )
    }

    suspend fun setAppAlert(packageName: String, dailyLimitBytes: Long, highUsageWarning: Boolean) {
        appAlertDao.setAlert(
            AppAlertEntity(
                packageName = packageName,
                dailyLimitBytes = dailyLimitBytes,
                highUsageWarningEnabled = highUsageWarning
            )
        )
    }

    private fun DataPlanEntity.toDomainModel(): DataPlan {
        val mode = try {
            BudgetMode.valueOf(budgetModeName)
        } catch (_: Exception) {
            BudgetMode.SMART
        }
        return DataPlan(
            id = id,
            networkCarrier = networkCarrier,
            planName = planName,
            planSizeBytes = planSizeBytes,
            priceNgn = priceNgn,
            startDateEpochMs = startDateEpochMs,
            expiryDateEpochMs = expiryDateEpochMs,
            autoRenewal = autoRenewal,
            budgetMode = mode,
            manualCarrierBalanceBytes = manualCarrierBalanceBytes,
            manualCarrierBalanceTimestamp = manualCarrierBalanceTimestamp
        )
    }
}

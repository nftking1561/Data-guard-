package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AppAlertDao
import com.example.data.local.dao.CarrierDao
import com.example.data.local.dao.DataPlanDao
import com.example.data.local.dao.UsageSnapshotDao
import com.example.data.local.entity.AppAlertEntity
import com.example.data.local.entity.CarrierReconciliationEntity
import com.example.data.local.entity.DailyUsageSnapshotEntity
import com.example.data.local.entity.DataPlanEntity

@Database(
    entities = [
        DataPlanEntity::class,
        DailyUsageSnapshotEntity::class,
        CarrierReconciliationEntity::class,
        AppAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataPlanDao(): DataPlanDao
    abstract fun usageSnapshotDao(): UsageSnapshotDao
    abstract fun carrierDao(): CarrierDao
    abstract fun appAlertDao(): AppAlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "data_guard.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data_guard_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_CARRIER = stringPreferencesKey("selected_carrier")
        val DATA_SAVER_MODE = stringPreferencesKey("data_saver_mode")
        val DAILY_ALERT_MB = intPreferencesKey("daily_alert_mb")
        val ABNORMAL_ALERT_ENABLED = booleanPreferencesKey("abnormal_alert_enabled")
        val RUNWAY_ALERT_ENABLED = booleanPreferencesKey("runway_alert_enabled")
        val USE_BINARY_UNITS = booleanPreferencesKey("use_binary_units")
        val ACTIVE_SIM_SLOT = intPreferencesKey("active_sim_slot")
        val SHOW_SYSTEM_APPS = booleanPreferencesKey("show_system_apps")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val selectedCarrier: Flow<String> = context.dataStore.data.map {
        it[Keys.SELECTED_CARRIER] ?: "MTN"
    }

    val dataSaverMode: Flow<String> = context.dataStore.data.map {
        it[Keys.DATA_SAVER_MODE] ?: "NORMAL"
    }

    val dailyAlertMb: Flow<Int> = context.dataStore.data.map {
        it[Keys.DAILY_ALERT_MB] ?: 500
    }

    val abnormalAlertEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.ABNORMAL_ALERT_ENABLED] ?: true
    }

    val runwayAlertEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.RUNWAY_ALERT_ENABLED] ?: true
    }

    val useBinaryUnits: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.USE_BINARY_UNITS] ?: false
    }

    val showSystemApps: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.SHOW_SYSTEM_APPS] ?: false
    }

    val activeSimSlot: Flow<Int> = context.dataStore.data.map {
        it[Keys.ACTIVE_SIM_SLOT] ?: 0
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSelectedCarrier(carrier: String) {
        context.dataStore.edit { it[Keys.SELECTED_CARRIER] = carrier }
    }

    suspend fun setDataSaverMode(mode: String) {
        context.dataStore.edit { it[Keys.DATA_SAVER_MODE] = mode }
    }

    suspend fun setDailyAlertMb(mb: Int) {
        context.dataStore.edit { it[Keys.DAILY_ALERT_MB] = mb }
    }

    suspend fun setAbnormalAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ABNORMAL_ALERT_ENABLED] = enabled }
    }

    suspend fun setRunwayAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RUNWAY_ALERT_ENABLED] = enabled }
    }

    suspend fun setUseBinaryUnits(useBinary: Boolean) {
        context.dataStore.edit { it[Keys.USE_BINARY_UNITS] = useBinary }
    }

    suspend fun setShowSystemApps(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_SYSTEM_APPS] = show }
    }

    suspend fun setActiveSimSlot(slot: Int) {
        context.dataStore.edit { it[Keys.ACTIVE_SIM_SLOT] = slot }
    }
}

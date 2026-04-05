package com.jbncode.anotadordomino.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kinetic_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_MODE       = booleanPreferencesKey("dark_mode")
        val CAPICUA_POINTS  = booleanPreferencesKey("capicua_points")
        val DOUBLE_TRANQUE  = booleanPreferencesKey("double_tranque")
    }

    // ── Observe ────────────────────────────────────────────────────────────

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.DARK_MODE] ?: true }          // dark por defecto

    val capicuaPointsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.CAPICUA_POINTS] ?: true }

    val doubleTranqueEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.DOUBLE_TRANQUE] ?: false }

    // ── Write ──────────────────────────────────────────────────────────────

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setCapicuaPoints(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CAPICUA_POINTS] = enabled }
    }

    suspend fun setDoubleTranque(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DOUBLE_TRANQUE] = enabled }
    }
}
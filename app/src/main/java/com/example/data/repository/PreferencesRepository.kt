package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.OverlayStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rcb_settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        val KEY_TEAM_NAME = stringPreferencesKey("team_name")
        val KEY_DEFAULT_OVERS = intPreferencesKey("default_overs")
        val KEY_OVERLAY_STYLE = stringPreferencesKey("overlay_style")
        val KEY_OVERLAY_SCALE = floatPreferencesKey("overlay_scale")
        val KEY_MUTE_AUDIO = booleanPreferencesKey("mute_audio")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val teamNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_TEAM_NAME] ?: "Rajpoot Cricket Boys"
    }

    val defaultOversFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_OVERS] ?: 20
    }

    val overlayStyleFlow: Flow<OverlayStyle> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_OVERLAY_STYLE] ?: OverlayStyle.LOWER_THIRD.name
        runCatching { OverlayStyle.valueOf(raw) }.getOrDefault(OverlayStyle.LOWER_THIRD)
    }

    val overlayScaleFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_OVERLAY_SCALE] ?: 1.0f
    }

    val muteAudioFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_MUTE_AUDIO] ?: false
    }

    val hapticFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAPTIC_FEEDBACK] ?: true
    }

    suspend fun setTeamName(name: String) {
        context.dataStore.edit { it[KEY_TEAM_NAME] = name }
    }

    suspend fun setDefaultOvers(overs: Int) {
        context.dataStore.edit { it[KEY_DEFAULT_OVERS] = overs }
    }

    suspend fun setOverlayStyle(style: OverlayStyle) {
        context.dataStore.edit { it[KEY_OVERLAY_STYLE] = style.name }
    }

    suspend fun setOverlayScale(scale: Float) {
        context.dataStore.edit { it[KEY_OVERLAY_SCALE] = scale }
    }

    suspend fun setMuteAudio(mute: Boolean) {
        context.dataStore.edit { it[KEY_MUTE_AUDIO] = mute }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }
}

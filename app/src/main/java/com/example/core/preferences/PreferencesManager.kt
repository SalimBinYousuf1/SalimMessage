package com.example.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "salim_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_DEFAULT_SIM = intPreferencesKey("default_sim")
        val KEY_ASK_SIM_PER_MESSAGE = booleanPreferencesKey("ask_sim_per_message")
        val KEY_DELIVERY_REPORTS = booleanPreferencesKey("delivery_reports")
        val KEY_SPLIT_LONG_SMS = booleanPreferencesKey("split_long_sms")
        val KEY_CONVERT_SPECIAL_CHARS = booleanPreferencesKey("convert_special_chars")

        val KEY_MMS_AUTO_DOWNLOAD = stringPreferencesKey("mms_auto_download") // ALWAYS, WIFI_ONLY, NEVER
        val KEY_MMS_ROAMING_DOWNLOAD = booleanPreferencesKey("mms_roaming_download")
        val KEY_GROUP_MESSAGING = booleanPreferencesKey("group_messaging") // true = MMS, false = individual SMS

        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        val KEY_NOTIFICATION_VIBRATE = booleanPreferencesKey("notification_vibrate")
        val KEY_NOTIFICATION_HEADS_UP = booleanPreferencesKey("notification_heads_up")
        val KEY_NOTIFICATION_PRIVACY = stringPreferencesKey("notification_privacy") // FULL, HIDE_BODY, HIDE_ALL
        val KEY_OTP_ONE_TAP_COPY = booleanPreferencesKey("otp_one_tap_copy")

        val KEY_THEME = stringPreferencesKey("app_theme") // SYSTEM, LIGHT, DARK
        val KEY_SHOW_AVATARS = booleanPreferencesKey("show_avatars")
        val KEY_USE_24_HOUR = booleanPreferencesKey("use_24_hour")

        val KEY_ENTER_IS_SEND = booleanPreferencesKey("enter_is_send")
        val KEY_SWIPE_ACTION_LEFT = stringPreferencesKey("swipe_action_left") // ARCHIVE, DELETE, READ
        val KEY_SWIPE_ACTION_RIGHT = stringPreferencesKey("swipe_action_right")

        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val KEY_HIDE_IN_RECENTS = booleanPreferencesKey("hide_in_recents")
    }

    val defaultSimFlow: Flow<Int> = context.dataStore.data.map { it[KEY_DEFAULT_SIM] ?: -1 }
    val askSimFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_ASK_SIM_PER_MESSAGE] ?: false }
    val deliveryReportsFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_DELIVERY_REPORTS] ?: true }
    val mmsAutoDownloadFlow: Flow<String> = context.dataStore.data.map { it[KEY_MMS_AUTO_DOWNLOAD] ?: "ALWAYS" }
    val groupMessagingFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_GROUP_MESSAGING] ?: true }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }
    val otpOneTapCopyFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_OTP_ONE_TAP_COPY] ?: true }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "LIGHT" }
    val showAvatarsFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_AVATARS] ?: true }
    val enterIsSendFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_ENTER_IS_SEND] ?: false }

    val appLockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_APP_LOCK_ENABLED] ?: false }
    val hideInRecentsFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_HIDE_IN_RECENTS] ?: false }

    suspend fun setDefaultSim(simId: Int) {
        context.dataStore.edit { it[KEY_DEFAULT_SIM] = simId }
    }

    suspend fun setAskSim(ask: Boolean) {
        context.dataStore.edit { it[KEY_ASK_SIM_PER_MESSAGE] = ask }
    }

    suspend fun setDeliveryReports(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DELIVERY_REPORTS] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setEnterIsSend(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ENTER_IS_SEND] = enabled }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setHideInRecents(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HIDE_IN_RECENTS] = enabled }
    }
}

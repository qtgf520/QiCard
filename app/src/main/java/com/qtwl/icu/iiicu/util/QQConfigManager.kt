package com.qtwl.icu.iiicu.util

import android.content.Context
import android.content.SharedPreferences

/**
 * QQ互联自定义配置管理器
 * 允许用户自定义 QQ互联 APP ID 和 APP Key
 * 开启自定义时使用用户配置值，关闭时自动使用系统默认值（strings.xml 中的 qq_app_id）
 */
object QQConfigManager {

    private const val PREFS_NAME = "qq_connect_config"
    private const val KEY_CUSTOM_ENABLED = "custom_enabled"
    private const val KEY_CUSTOM_APP_ID = "custom_app_id"
    private const val KEY_CUSTOM_APP_KEY = "custom_app_key"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** 是否开启自定义 QQ互联配置 */
    fun isCustomEnabled(): Boolean = prefs.getBoolean(KEY_CUSTOM_ENABLED, false)

    /** 设置是否开启自定义 */
    fun setCustomEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CUSTOM_ENABLED, enabled).apply()
    }

    /** 获取自定义 APP ID（用户设置的） */
    fun getCustomAppId(): String = prefs.getString(KEY_CUSTOM_APP_ID, "") ?: ""

    /** 设置自定义 APP ID */
    fun setCustomAppId(appId: String) {
        prefs.edit().putString(KEY_CUSTOM_APP_ID, appId).apply()
    }

    /** 获取自定义 APP Key（用户设置的） */
    fun getCustomAppKey(): String = prefs.getString(KEY_CUSTOM_APP_KEY, "") ?: ""

    /** 设置自定义 APP Key */
    fun setCustomAppKey(appKey: String) {
        prefs.edit().putString(KEY_CUSTOM_APP_KEY, appKey).apply()
    }

    /** 获取当前实际使用的 APP ID（自定义开启则用自定义值，否则返回空字符串表示使用系统默认） */
    fun getEffectiveAppId(): String {
        return if (isCustomEnabled()) getCustomAppId() else ""
    }

    /** 获取当前实际使用的 APP Key（自定义开启则用自定义值，否则返回空字符串） */
    fun getEffectiveAppKey(): String {
        return if (isCustomEnabled()) getCustomAppKey() else ""
    }

    /** 清除自定义配置，恢复为系统默认 */
    fun clearCustomConfig() {
        prefs.edit().clear().apply()
    }
}
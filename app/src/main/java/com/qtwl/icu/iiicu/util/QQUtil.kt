package com.qtwl.icu.iiicu.util

import android.content.Context
import android.content.pm.PackageManager
import com.qtwl.icu.iiicu.R
import com.tencent.tauth.Tencent

/**
 * QQ SDK 工具类（QQ互联）
 */
object QQUtil {
    private var tencent: Tencent? = null

    fun init(context: Context) {
        init(context, null)
    }

    fun init(context: Context, customAppId: String?) {
        if (customAppId.isNullOrBlank()) {
            // 使用系统默认配置（strings.xml 中的 qq_app_id）
            val appId = context.getString(R.string.qq_app_id)
            tencent = Tencent.createInstance(appId, context.applicationContext)
        } else {
            // 自定义 ID：强制使用自定义值（覆盖任何之前的配置）
            tencent = Tencent.createInstance(customAppId, context.applicationContext)
        }
    }

    fun getTencent(): Tencent? = tencent

    /**
     * 刷新QQ配置（重新读取QQConfigManager的最新配置）
     * 每次分享前调用，确保自定义配置生效
     */
    fun refreshConfig(context: Context) {
        init(context, QQConfigManager.getEffectiveAppId())
    }

    /**
     * 检查QQ是否已安装
     * 使用 PackageManager 手动检测 + SDK 检测双重保障
     */
    fun isQQInstalled(context: Context): Boolean {
        // 方式1：使用 PackageManager 手动检测（兼容 API 24+）
        try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            pm.getPackageInfo("com.tencent.mobileqq", 0)
            return true
        } catch (_: Exception) {
            // 未安装QQ
        }
        try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            pm.getPackageInfo("com.tencent.tim", 0)
            return true
        } catch (_: Exception) {
            // 未安装TIM
        }
        try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            pm.getPackageInfo("com.tencent.minihd.qq", 0)
            return true
        } catch (_: Exception) {
            // 未安装QQ HD
        }
        // 方式2：兜底用SDK自带检测
        return try {
            val t = tencent ?: return false
            t.isQQInstalled(context)
        } catch (_: Exception) {
            false
        }
    }
}

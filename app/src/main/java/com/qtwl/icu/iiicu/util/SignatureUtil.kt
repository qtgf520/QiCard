package com.qtwl.icu.iiicu.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * 应用签名工具类
 * 自动获取当前 APK 签名的 MD5 值
 */
object SignatureUtil {

    /**
     * 获取当前应用签名的 MD5 字符串（小写）
     * 兼容 Android 9+ (API 28+) 的签名证书方案
     */
    fun getSignatureMD5(context: Context): String {
        try {
            val pm = context.packageManager
            val packageName = context.packageName

            val signatureBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ 使用 getSigningCertificateManager
                val info = pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                info.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
            } else {
                // Android 8.x 及以下使用 GET_SIGNATURES
                @Suppress("DEPRECATION")
                val info = pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                info.signatures?.firstOrNull()?.toByteArray()
            }

            if (signatureBytes == null) return "未知"

            val md5Digest = MessageDigest.getInstance("MD5").digest(signatureBytes)
            return md5Digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            return "获取签名失败: ${e.localizedMessage}"
        }
    }
}

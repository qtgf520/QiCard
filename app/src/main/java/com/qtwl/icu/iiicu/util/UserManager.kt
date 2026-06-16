package com.qtwl.icu.iiicu.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 用户登录管理器 - 对接 www.jili5.cn 平台
 * 使用 HttpURLConnection 实现，零额外依赖
 */
object UserManager {

    private const val PREFS_NAME = "qitong_user_prefs"
    private const val KEY_LOGGED_IN = "is_logged_in"
    private const val KEY_USERNAME = "username"
    private const val KEY_USER_TOKEN = "user_token"

    // 域名不带 www，与 jili5.cn 官方 HTML 一致
    private const val BASE_URL = "https://jili5.cn"
    private const val LOGIN_PATH = "/user/login.php"

    private lateinit var prefs: SharedPreferences

    /** 初始化（建议在 Application 或 MainActivity 中调用） */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 初始化时配置信任所有证书（开发阶段临时方案）
        trustAllCertificates()
    }

    /**
     * 配置信任所有 SSL 证书（开发阶段使用，解决 jili5.cn 证书不受信任问题）
     * 生产环境应使用正确的证书验证
     */
    private fun trustAllCertificates() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifier { _, _ -> true })
        } catch (_: Exception) {
            // SSL 配置失败不影响基本功能
        }
    }

    /** 是否已登录 */
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    /** 获取当前登录用户名 */
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    /** 获取 user_token Cookie 值 */
    fun getUserToken(): String = prefs.getString(KEY_USER_TOKEN, "") ?: ""

    /**
     * 从 Set-Cookie 响应头中提取 user_token
     */
    private fun extractUserToken(conn: HttpURLConnection): String {
        var idx = 1
        while (true) {
            val key = conn.getHeaderFieldKey(idx)
            if (key == null) break
            if (key.equals("Set-Cookie", ignoreCase = true)) {
                val cookie = conn.getHeaderField(idx) ?: ""
                val match = Regex("user_token=([^;]+)").find(cookie)
                if (match != null) return match.groupValues[1]
            }
            idx++
        }
        return ""
    }

    /**
     * 执行登录请求（在工作线程中执行网络请求，回调在主线程执行）
     * 接口返回 HTML 而非 JSON，通过 Set-Cookie 判断登录成功
     * @param user 用户名 / QQ / 邮箱
     * @param pwd 密码
     * @param onResult (是否成功, 消息) — 保证在主线程回调
     */
    fun login(context: Context, user: String, pwd: String, onResult: (Boolean, String) -> Unit) {
        Thread {
            try {
                val encodedUser = URLEncoder.encode(user, "UTF-8")
                val encodedPwd = URLEncoder.encode(pwd, "UTF-8")
                val urlStr = "$BASE_URL$LOGIN_PATH?mod=dl&user=$encodedUser&pwd=$encodedPwd"

                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.instanceFollowRedirects = false // 不自动跟随重定向，手动解析

                // 读取响应体
                val response = conn.inputStream.bufferedReader().readText()

                // 提取 user_token Cookie（登录成功的标志）
                val userToken = extractUserToken(conn)

                // 根据响应内容判断登录结果
                if (userToken.isNotEmpty()) {
                    // 登录成功 —— 响应中包含 user_token Cookie
                    // 保存登录状态
                    prefs.edit()
                        .putBoolean(KEY_LOGGED_IN, true)
                        .putString(KEY_USERNAME, user)
                        .putString(KEY_USER_TOKEN, userToken)
                        .apply()

                    // 同步 Cookie 到 WebView
                    try {
                        android.webkit.CookieManager.getInstance().apply {
                            setCookie(BASE_URL, "user_token=$userToken; path=/")
                            flush()
                        }
                    } catch (_: Exception) { }

                    // 检查响应中是否包含成功提示
                    val successMsg = when {
                        response.contains("登录成功") -> "登录成功"
                        response.contains("操作成功") -> "登录成功"
                        else -> "登录成功"
                    }

                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onResult(true, successMsg)
                    }
                } else {
                    // 登录失败 —— 没有 user_token
                    // 从响应中提取错误消息（如 alert 弹窗内容）
                    val errorMsg = when {
                        response.contains("用户名或密码不正确") -> "用户名或密码不正确"
                        response.contains("账号不存在") -> "账号不存在"
                        response.contains("验证码错误") -> "验证码错误"
                        response.contains("系统错误") || response.contains("异常") -> "系统异常，请稍后重试"
                        else -> "登录失败，请检查账号密码"
                    }

                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onResult(false, errorMsg)
                    }
                }

                conn.disconnect()
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onResult(false, "网络错误：" + (e.localizedMessage ?: "未知错误"))
                }
            }
        }.start()
    }

    /** 退出登录 */
    fun logout() {
        prefs.edit().clear().apply()
        // 清除 WebView Cookie
        try {
            android.webkit.CookieManager.getInstance().apply {
                removeSessionCookies(null)
                flush()
            }
        } catch (_: Exception) { }
    }

    /** 获取 app.php 完整 URL（登录后 WebView 加载） */
    fun getAppUrl(): String = "http://www.jili5.cn/app.php"

    /** 获取当前 APP 版本号 */
    fun getAppVersion(): String = "8.3"
}

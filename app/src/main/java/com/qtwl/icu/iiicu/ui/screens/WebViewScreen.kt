package com.qtwl.icu.iiicu.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.qtwl.icu.iiicu.util.UserManager

/**
 * 内置浏览器 WebView 页面
 * 加载 http://www.jili5.cn/app.php
 * - 自定义 UA 包含 "jili5" 供后端识别
 * - 自动同步登录 Cookie（user_token）
 * - 所有链接在 App 内部打开，禁止跳出外部浏览器
 * - 添加错误处理、日志输出，便于排查加载失败问题
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String = "http://www.jili5.cn/app.php"
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 如果有错误，显示错误页面 + 重试按钮
    if (errorMsg != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "页面加载失败 😿",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = errorMsg ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
                Button(onClick = {
                    errorMsg = null
                    isLoading = true
                }) {
                    Text("重新加载")
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            WebViewContent(url = url, onLoading = { isLoading = it }, onError = { errorMsg = it })

            // 加载中指示器
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContent(
    url: String,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // ==================== WebView 配置 ====================
                    settings.apply {
                        // 启用 JavaScript（app.php 依赖 JS 渲染）
                        javaScriptEnabled = true

                        // 启用 DOM Storage（H5 页面常用）
                        domStorageEnabled = true
                        databaseEnabled = true

                        // 允许混合内容（目标 URL 是 http://，可能引用 https 资源）
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        // ========== 自定义 User-Agent ==========
                        // 在默认 UA 末尾追加 "jili5" 以便后端 PHP 识别客户端
                        val defaultUA = settings.userAgentString
                        userAgentString = "$defaultUA jili5"

                        // 视口适配移动端
                        loadWithOverviewMode = true
                        useWideViewPort = true

                        // 禁止缩放控件
                        builtInZoomControls = false
                        displayZoomControls = false

                        // 自动加载图片
                        loadsImagesAutomatically = true

                        // 缓存策略
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    // ========== Cookie 同步（保持登录态） ==========
                    CookieManager.getInstance().apply {
                        setAcceptCookie(true)
                    }
                    // 启用第三方 Cookie（用于跨域跳转保持登录）
                    CookieManager.getInstance().acceptThirdPartyCookies(this)

                    // 从 UserManager 获取已保存的 token 并写入 Cookie
                    val token = UserManager.getUserToken()
                    if (token.isNotEmpty()) {
                        // jili5.cn 和 www.jili5.cn 都要写入
                        CookieManager.getInstance().setCookie(".jili5.cn", "user_token=$token; path=/")
                        CookieManager.getInstance().setCookie("www.jili5.cn", "user_token=$token; path=/")
                        CookieManager.getInstance().flush()
                    }

                    // ========== WebViewClient：控制页面导航 ==========
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("WebViewScreen", "onPageStarted: $url")
                            onLoading(true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("WebViewScreen", "onPageFinished: $url")
                            onLoading(false)
                        }

                        /**
                         * 拦截所有链接跳转，确保在 App 内部 WebView 中打开
                         * 不启动外部浏览器
                         * 修复：对于 HTTP/HTTPS 协议在内部加载，其余协议（如 tel://, mailto:）放行
                         */
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val requestUrl = request?.url?.toString() ?: return false
                            Log.d("WebViewScreen", "shouldOverrideUrlLoading: $requestUrl")
                            // 仅拦截 http/https 协议在内部加载
                            if (requestUrl.startsWith("http://") || requestUrl.startsWith("https://")) {
                                view?.loadUrl(requestUrl)
                                return true
                            }
                            // 其他协议（tel, mailto, intent）交给系统处理
                            return false
                        }

                        /**
                         * 处理加载错误（如网络不可用、DNS 解析失败等）
                         */
                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            val errorCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                error?.errorCode ?: -1
                            } else {
                                -1
                            }
                            val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                error?.description?.toString() ?: "未知错误"
                            } else {
                                "未知错误"
                            }
                            Log.e("WebViewScreen", "onReceivedError: code=$errorCode, desc=$description, url=${request?.url}")

                            // 仅当是主框架（顶层页面）错误时才显示错误页面
                            if (request?.isForMainFrame == true) {
                                onError("错误码: $errorCode\n$description")
                                onLoading(false)
                            }
                        }

                        /**
                         * 处理 HTTP 状态码错误（如 404, 500 等）
                         */
                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            errorResponse: WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            val statusCode = errorResponse?.statusCode ?: -1
                            Log.w("WebViewScreen", "onReceivedHttpError: code=$statusCode, url=${request?.url}")

                            if (request?.isForMainFrame == true) {
                                onError("HTTP 错误: $statusCode")
                                onLoading(false)
                            }
                        }
                    }

                    // ========== WebChromeClient：进度等回调 ==========
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                        }
                    }

                    // ========== 加载目标页面 ==========
                    Log.d("WebViewScreen", "开始加载: $url (UA: ${settings.userAgentString})")
                    loadUrl(url)
                }
            },
            update = { webView ->
                // 每次 Composable 更新时确保 Cookie 同步
                val token = UserManager.getUserToken()
                if (token.isNotEmpty()) {
                    CookieManager.getInstance().apply {
                        setCookie(".jili5.cn", "user_token=$token; path=/")
                        setCookie("www.jili5.cn", "user_token=$token; path=/")
                        flush()
                    }
                }
            }
        )
    }
}

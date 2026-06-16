package com.qtwl.icu.iiicu.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qtwl.icu.iiicu.util.UserManager

/**
 * "我的" 页面
 * - 未登录：显示登录入口
 * - 已登录：顶栏 退出/设置 ＋ 直接嵌入 WebView 显示网站内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val isLoggedIn = UserManager.isLoggedIn()

    // 使用 mutableStateOf 监听状态变化
    var loggedIn by remember { mutableStateOf(isLoggedIn) }

    // 每次重新组合时刷新状态
    LaunchedEffect(Unit) {
        loggedIn = UserManager.isLoggedIn()
    }

    // 退出登录后的刷新
    fun refreshState() {
        loggedIn = UserManager.isLoggedIn()
    }

    if (loggedIn) {
        // ========== 已登录：顶栏 + WebView 直接显示 ==========
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("我的") },
                    navigationIcon = {
                        // 退出登录按钮（左上角）
                        IconButton(onClick = {
                            UserManager.logout()
                            refreshState()
                            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "退出登录")
                        }
                    },
                    actions = {
                        // 设置按钮（右上角）
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // 直接嵌入 WebView 显示网站内容
                WebViewContent(
                    url = "http://www.jili5.cn/app.php",
                    onLoading = {},
                    onError = {}
                )
            }
        }
    } else {
        // ========== 未登录：显示登录入口 ==========
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("我的") })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "綦桐网络",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "版本 ${UserManager.getAppVersion()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "登录后可同步网站数据",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onNavigateToLogin() },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("登录账号", fontSize = 18.sp)
                }
            }
        }
    }
}
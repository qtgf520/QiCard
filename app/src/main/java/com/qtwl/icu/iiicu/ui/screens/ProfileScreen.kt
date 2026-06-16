package com.qtwl.icu.iiicu.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.OpenInBrowser
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
 * "我的" 个人页面
 * - 未登录：显示登录入口
 * - 已登录：显示用户信息 + 网站入口
 * - 退出按钮在左上角 | 设置按钮在右上角
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToWebView: () -> Unit = {}
) {
    val context = LocalContext.current
    val isLoggedIn = UserManager.isLoggedIn()
    val username = UserManager.getUsername()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                navigationIcon = {
                    // 退出登录按钮（左上角）
                    if (loggedIn) {
                        IconButton(onClick = {
                            UserManager.logout()
                            refreshState()
                            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "退出登录")
                        }
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
        if (loggedIn) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "欢迎回来",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = username,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { onNavigateToWebView() },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("打开网站管理", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "www.jili5.cn/user/app.php",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "綦桐网络 v${UserManager.getAppVersion()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = "已开源 · MIT License",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        } else {
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

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "无法打开网页", Toast.LENGTH_SHORT).show()
    }
}
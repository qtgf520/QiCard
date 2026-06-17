package com.qtwl.icu.iiicu.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
        // ========== 未登录：直接显示登录表单（内嵌化，无需跳转） ==========
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("我的") },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    }
                )
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "版本 ${UserManager.getAppVersion()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "登录 www.jili5.cn 平台",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                // 用户名输入框
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("账号 / QQ / 邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 密码输入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                isLoading = true
                                UserManager.login(context, username, password) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                                        refreshState()
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(32.dp))

                // 登录按钮
                Button(
                    onClick = {
                        if (username.isBlank()) {
                            Toast.makeText(context, "请输入账号", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password.isBlank()) {
                            Toast.makeText(context, "请输入密码", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        UserManager.login(context, username, password) { success, msg ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                                refreshState()
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("登录", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "注册账号请访问 www.jili5.cn",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
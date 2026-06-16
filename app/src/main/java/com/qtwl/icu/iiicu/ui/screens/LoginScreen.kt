package com.qtwl.icu.iiicu.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qtwl.icu.iiicu.util.UserManager

/**
 * 登录页面
 * 对接 www.jili5.cn 平台
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("登录") }
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
            Text(
                text = "綦桐网络",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "登录 www.jili5.cn 平台",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(48.dp))

            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("账号 / QQ / 邮箱") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (username.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            UserManager.login(context, username, password) { success, msg ->
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
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
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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
package com.qtwl.icu.iiicu.ui.screens

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.qtwl.icu.iiicu.util.UserManager
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 设置页面
 * - APP 权限设置（显示状态）
 * - 开源许可查看（可阅读 LICENSE / THIRD-PARTY-LICENSES.md）
 * - 关于綦桐网络
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showLicenseDialog by remember { mutableStateOf(false) }
    var licenseContent by remember { mutableStateOf("") }
    var showThirdPartyDialog by remember { mutableStateOf(false) }
    var thirdPartyContent by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // 权限开关状态
    var storageGranted by remember { mutableStateOf(checkStoragePermission(context)) }
    var networkGranted by remember { mutableStateOf(true) } // 网络权限默认开启

    // 权限请求 Launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        storageGranted = granted
        Toast.makeText(
            context,
            if (granted) "存储权限已开启" else "存储权限授权被拒绝",
            Toast.LENGTH_SHORT
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // ========== 权限设置区域 ==========
            Text(
                text = "APP 设置",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 网络权限
            PermissionItem(
                icon = Icons.Default.Lock,
                title = "网络访问",
                subtitle = "用于加载网页内容和图片",
                checked = networkGranted,
                enabled = false,
                onToggle = { networkGranted = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // 存储权限
            PermissionItem(
                icon = Icons.Default.Shield,
                title = "存储空间",
                subtitle = "用于缓存卡片数据和图片",
                checked = storageGranted,
                enabled = true,
                onToggle = {
                    if (it) {
                        // 打开 → 请求权限
                        val permission = if (Build.VERSION.SDK_INT >= 33)
                            Manifest.permission.READ_MEDIA_IMAGES
                        else
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        storagePermissionLauncher.launch(permission)
                    } else {
                        // 关闭 → 提示用户手动关闭
                        storageGranted = false
                        Toast.makeText(
                            context,
                            "请在系统设置中关闭存储权限",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // QQ 分享权限
            PermissionItem(
                icon = Icons.Default.CheckCircle,
                title = "QQ 分享",
                subtitle = "用于分享卡片到 QQ / TIM",
                checked = true,
                enabled = false,
                onToggle = { }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ========== 开源信息区域 ==========
            Text(
                text = "开源信息",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // MIT 许可证
            ListItem(
                headlineContent = { Text("MIT 开源许可证") },
                supportingContent = { Text("本项目基于 MIT License 开源") },
                leadingContent = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    try {
                        val inputStream = context.assets.open("LICENSE")
                            ?: context.resources.assets.open("LICENSE")
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        licenseContent = reader.readText()
                        reader.close()
                        showLicenseDialog = true
                    } catch (e: Exception) {
                        // fallback: 尝试从 files 目录读取
                        try {
                            val file = java.io.File(context.filesDir.parentFile?.parentFile, "files/LICENSE")
                            if (file.exists()) {
                                licenseContent = file.readText()
                                showLicenseDialog = true
                            } else {
                                // 直接在应用资源中内嵌 MIT 许可证文本
                                licenseContent = """MIT License

Copyright (c) 2026 綦桐科技 (QiTong Technology)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE."""
                                showLicenseDialog = true
                            }
                        } catch (e2: Exception) {
                            Toast.makeText(context, "无法加载许可证文件", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // 第三方依赖许可证
            ListItem(
                headlineContent = { Text("第三方依赖许可证") },
                supportingContent = { Text("查看 AndroidX、Coil、Gson 等依赖的许可证信息") },
                leadingContent = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    try {
                        val inputStream = context.assets.open("THIRD-PARTY-LICENSES.md")
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        thirdPartyContent = reader.readText()
                        reader.close()
                        showThirdPartyDialog = true
                    } catch (e: Exception) {
                        // fallback 内嵌内容
                        thirdPartyContent = """# Third-Party Licenses

## AndroidX Libraries (Apache License 2.0)
- androidx.core:core-ktx
- androidx.lifecycle:lifecycle-runtime-ktx
- androidx.activity:activity-compose
- androidx.compose.material3:material3
- androidx.navigation:navigation-compose

## Coil (Apache License 2.0)
- coil.compose:coil-compose

## Gson (Apache License 2.0)
- com.google.code.gson:gson

## Kotlin (Apache License 2.0)
- org.jetbrains.kotlin:kotlin-stdlib

## Android Gradle Plugin (Apache License 2.0)
- com.android.tools.build:gradle

## JUnit (Eclipse Public License 1.0)
- junit:junit

## QQ SDK (专有协议 - Tencent)
- 腾讯 QQ 互联 SDK"""
                        showThirdPartyDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ========== 关于区域 ==========
            Text(
                text = "关于",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "綦桐网络",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "v${UserManager.getAppVersion()}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "綦桐网络已开源 · MIT License",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Made with ❤️ by 綦桐科技",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/qtgf520/QiCard"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "无法打开 GitHub 链接", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("查看 GitHub 仓库 →", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ========== MIT 许可证弹窗 ==========
    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text("MIT 开源许可证") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = licenseContent,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }

    // ========== 第三方许可证弹窗 ==========
    if (showThirdPartyDialog) {
        AlertDialog(
            onDismissRequest = { showThirdPartyDialog = false },
            title = { Text("第三方依赖许可证") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = thirdPartyContent,
                        fontSize = 13.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThirdPartyDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = if (enabled) onToggle else null,
                enabled = enabled
            )
        }
    )
}

/**
 * 检查当前存储权限是否已授予
 */
private fun checkStoragePermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }
}
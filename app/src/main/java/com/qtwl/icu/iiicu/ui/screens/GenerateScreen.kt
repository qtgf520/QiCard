package com.qtwl.icu.iiicu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * "生成" 页面 — 待更新，未来可添加好玩的 AI 生成功能
 */
@Composable
fun GenerateScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "生成",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "待更新 · 敬请期待",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "未来将支持 AI 生成卡片、智能摘要等好玩功能 ✨",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
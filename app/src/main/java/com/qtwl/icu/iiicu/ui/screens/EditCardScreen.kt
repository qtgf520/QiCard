package com.qtwl.icu.iiicu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qtwl.icu.iiicu.model.ShareCard
import com.qtwl.icu.iiicu.viewmodel.EditCardViewModel

/**
 * 编辑/创建卡片界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    viewModel: EditCardViewModel,
    onBack: () -> Unit,
    onSaveOnly: (ShareCard) -> Unit
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val url by viewModel.url.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑卡片") },
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 图片预览 — 小正方形预览
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "图片预览",
                    modifier = Modifier
                        .size(width = 100.dp, height = 100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("标题 *") },
                placeholder = { Text("输入分享标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("内容") },
                placeholder = { Text("输入分享描述内容") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            OutlinedTextField(
                value = url,
                onValueChange = { viewModel.updateUrl(it) },
                label = { Text("链接地址 *") },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { viewModel.updateImageUrl(it) },
                label = { Text("图片地址") },
                placeholder = { Text("https://example.com/image.png") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 底部按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }

                Button(
                    onClick = {
                        val card = viewModel.getCardData()
                        onSaveOnly(card)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank() && url.isNotBlank()
                ) {
                    Text("保存")
                }
            }

            // 底部留白，确保滑到底时不被系统导航栏遮挡
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
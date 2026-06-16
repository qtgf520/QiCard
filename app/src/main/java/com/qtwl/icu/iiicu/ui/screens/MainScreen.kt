package com.qtwl.icu.iiicu.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qtwl.icu.iiicu.model.ShareCard

/**
 * 主界面 - 卡片列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    cards: List<ShareCard>,
    onEditCard: (ShareCard) -> Unit,
    onAddCard: () -> Unit,
    onShareCard: (ShareCard) -> Unit,
    onDeleteCard: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("綦桐网络") },
                actions = {
                    IconButton(onClick = onAddCard) {
                        Icon(Icons.Default.Add, contentDescription = "添加卡片")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { paddingValues ->
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂无卡片，点击右下角添加",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    ShareCardItem(
                        card = card,
                        onClick = { onEditCard(card) },
                        onShare = { onShareCard(card) },
                        onDelete = { onDeleteCard(card.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareCardItem(
    card: ShareCard,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 缩略图
            if (card.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (card.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.content,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.url,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }

            // 分享按钮
            IconButton(onClick = onShare) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "分享到QQ",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
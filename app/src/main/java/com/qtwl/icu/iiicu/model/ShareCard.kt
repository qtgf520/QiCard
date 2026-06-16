package com.qtwl.icu.iiicu.model

/**
 * 分享卡片数据模型
 */
data class ShareCard(
    val id: Long = System.currentTimeMillis(),
    val title: String = "",
    val content: String = "",
    val url: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isValid(): Boolean {
        return title.isNotBlank() && url.isNotBlank()
    }
}
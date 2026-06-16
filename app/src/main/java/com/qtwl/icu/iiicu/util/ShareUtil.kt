package com.qtwl.icu.iiicu.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.qtwl.icu.iiicu.model.ShareCard
import com.tencent.tauth.IUiListener

/**
 * QQ分享工具类（QQ互联 - 网页卡片分享）
 * 所有分享操作必须先登录 www.jili5.cn
 */
object ShareUtil {

    /**
     * 检查是否已登录（分享前置条件）
     * @return true=已登录可分享, false=未登录
     */
    fun checkLogin(context: Context): Boolean {
        if (!UserManager.isLoggedIn()) {
            Toast.makeText(context, "请先登录后再分享", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * 分享网页卡片到QQ（自动校验登录状态）
     * @param context Activity 上下文
     * @param card 分享卡片数据
     * @param listener IUiListener 回调（QQ SDK shareToQQ 使用 IUiListener）
     * @return true=已发起分享, false=因未登录等原因被拦截
     */
    fun shareWebPage(
        context: Context,
        card: ShareCard,
        listener: IUiListener
    ): Boolean {
        // 强制校验登录状态
        if (!checkLogin(context)) return false

        // 检查QQ SDK是否初始化成功
        val tencent = QQUtil.getTencent()
        if (tencent == null) {
            Toast.makeText(context, "QQ SDK 初始化失败", Toast.LENGTH_SHORT).show()
            return false
        }

        // 检查QQ客户端是否已安装（使用QQUtil双重检测机制，兼容Android 11+）
        if (!QQUtil.isQQInstalled(context)) {
            Toast.makeText(context, "请先安装QQ客户端", Toast.LENGTH_SHORT).show()
            return false
        }

        // 检查分享数据是否完整
        if (card.title.isBlank() || card.url.isBlank()) {
            Toast.makeText(context, "请填写标题和链接后再分享", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            val params = android.os.Bundle().apply {
                // req_type=1 表示网页卡片分享
                putInt("req_type", 1)
                putString("title", card.title)
                putString("summary", card.content)
                putString("targetUrl", card.url)
                if (card.imageUrl.isNotBlank()) {
                    putString("imageUrl", card.imageUrl)
                }
            }

            tencent.shareToQQ(context as Activity, params, listener)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "分享失败：${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}

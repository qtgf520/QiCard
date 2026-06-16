package com.qtwl.icu.iiicu.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * 网页信息解析器
 * 从分享的 URL 中自动提取标题、描述、图标等信息，用于自动生成卡片
 */
object WebPageParser {

    data class PageInfo(
        val title: String = "",
        val description: String = "",
        val iconUrl: String = "",
        val url: String = ""
    )

    /**
     * 从 URL 解析网页信息（在工作线程执行）
     * @param pageUrl 目标网页 URL
     * @param timeoutMs 超时时间（毫秒）
     * @return 解析到的页面信息
     */
    fun parse(pageUrl: String, timeoutMs: Int = 8000): PageInfo {
        if (pageUrl.isBlank()) return PageInfo(url = pageUrl)

        var finalUrl = pageUrl
        // 自动补全协议头
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }

        try {
            val url = URL(finalUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.instanceFollowRedirects = true
            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36"
            )

            // 获取最终重定向后的 URL
            val responseCode = conn.responseCode
            val effectiveUrl = conn.url.toString()

            // 读取 HTML
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val html = reader.readText()
            reader.close()
            conn.disconnect()

            // 解析元信息
            val title = extractTitle(html) ?: ""
            val description = extractMetaContent(html, "description")
                ?: extractMetaContent(html, "og:description")
                ?: extractMetaContent(html, "twitter:description")
                ?: ""
            val iconUrl = extractFavicon(html, effectiveUrl)

            return PageInfo(
                title = title,
                description = description,
                iconUrl = iconUrl,
                url = effectiveUrl
            )

        } catch (e: Exception) {
            // 解析失败时至少保留 URL
            return PageInfo(url = finalUrl)
        }
    }

    /** 提取 <title> 标签内容 */
    private fun extractTitle(html: String): String? {
        // 优先 og:title
        val ogTitle = extractMetaContent(html, "og:title")
        if (ogTitle != null) return ogTitle

        // 再尝试 <title>
        val regex = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE)
        val match = regex.find(html)
        return match?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    /** 提取 meta 标签 content */
    private fun extractMetaContent(html: String, property: String): String? {
        // 尝试 name="..." 或 property="..."
        val regex = Regex(
            """<meta[^>]+?(?:name|property)=["']$property["'][^>]*?>""",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(html)
        if (match != null) {
            val contentRegex = Regex("""content=["']([^"']*)["']""", RegexOption.IGNORE_CASE)
            val contentMatch = contentRegex.find(match.value)
            val value = contentMatch?.groupValues?.getOrNull(1)
            if (!value.isNullOrBlank()) return value
        }

        // 交换顺序再匹配一次
        val regex2 = Regex(
            """<meta[^>]+?content=["']([^"']*?)["'][^>]+?(?:name|property)=["']$property["'][^>]*?>""",
            RegexOption.IGNORE_CASE
        )
        val match2 = regex2.find(html)
        if (match2 != null) {
            val value = match2.groupValues.getOrNull(1)
            if (!value.isNullOrBlank()) return value
        }

        return null
    }

    /** 提取网站图标 URL */
    private fun extractFavicon(html: String, baseUrl: String): String {
        // 优先 apple-touch-icon
        val appleRegex = Regex(
            """<link[^>]+?rel=["']apple-touch-icon["'][^>]*?>""",
            RegexOption.IGNORE_CASE
        )
        val appleMatch = appleRegex.find(html)
        if (appleMatch != null) {
            val hrefRegex = Regex("""href=["']([^"']*)["']""", RegexOption.IGNORE_CASE)
            val href = hrefRegex.find(appleMatch.value)?.groupValues?.getOrNull(1)
            if (!href.isNullOrBlank()) return resolveUrl(href, baseUrl)
        }

        // 再找 icon
        val iconRegex = Regex(
            """<link[^>]+?rel=["'](?:shortcut )?icon["'][^>]*?>""",
            RegexOption.IGNORE_CASE
        )
        val iconMatch = iconRegex.find(html)
        if (iconMatch != null) {
            val hrefRegex = Regex("""href=["']([^"']*)["']""", RegexOption.IGNORE_CASE)
            val href = hrefRegex.find(iconMatch.value)?.groupValues?.getOrNull(1)
            if (!href.isNullOrBlank()) return resolveUrl(href, baseUrl)
        }

        // 兜底：默认 favicon.ico
        return resolveUrl("/favicon.ico", baseUrl)
    }

    /** 解析相对/绝对 URL */
    private fun resolveUrl(href: String, baseUrl: String): String {
        if (href.startsWith("http://") || href.startsWith("https://")) return href
        val base = baseUrl.trimEnd('/')
        return if (href.startsWith("/")) {
            val host = extractHost(baseUrl)
            "$host$href"
        } else {
            "$base/$href"
        }
    }

    private fun extractHost(url: String): String {
        val regex = Regex("^(https?://[^/]+)")
        return regex.find(url)?.groupValues?.getOrNull(1) ?: url
    }
}
package com.qtwl.icu.iiicu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qtwl.icu.iiicu.ui.screens.*
import com.qtwl.icu.iiicu.ui.theme.MyApplicationTheme
import com.qtwl.icu.iiicu.util.QQUtil
import com.qtwl.icu.iiicu.util.ShareUtil
import com.qtwl.icu.iiicu.util.UserManager
import com.qtwl.icu.iiicu.util.WebPageParser
import com.qtwl.icu.iiicu.viewmodel.EditCardViewModel
import com.qtwl.icu.iiicu.viewmodel.MainViewModel
import com.tencent.tauth.IUiListener
import com.tencent.tauth.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val shareListener = object : IUiListener {
        override fun onComplete(response: Any?) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "分享成功！", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onError(error: UiError?) {
            runOnUiThread {
                val msg = error?.errorMessage ?: "未知错误"
                Toast.makeText(this@MainActivity, "分享失败：$msg", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onCancel() {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "已取消分享", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QQUtil.init(this)
        UserManager.init(this)

        val sharedUrl = extractSharedUrl(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        initialSharedUrl = sharedUrl,
                        shareListener = shareListener,
                        activity = this@MainActivity
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 处理新的分享 Intent（APP 仍在运行）
        setIntent(intent)
        val sharedUrl = extractSharedUrl(intent)
        if (sharedUrl != null) {
            Toast.makeText(this, "收到分享链接：${sharedUrl.take(50)}...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent == null) return null
        val action = intent.action ?: return null
        if (action != Intent.ACTION_SEND) return null
        val type = intent.type ?: return null
        if (type != "text/plain") return null
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
            ?: return null
        return extractUrlFromText(sharedText)
    }

    private fun extractUrlFromText(text: String): String? {
        val urlRegex = Regex("https?://[\\w./?=&%+@#\\-]+")
        return urlRegex.find(text)?.value
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        com.tencent.tauth.Tencent.onActivityResultData(requestCode, resultCode, data, shareListener)
    }
}

/**
 * 应用主导航
 * 底部导航栏：首页 · 生成 · 我的
 * 支持从分享 URL 自动创建卡片
 */
@Composable
fun AppNavigation(
    initialSharedUrl: String?,
    shareListener: IUiListener,
    activity: ComponentActivity
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val bottomBarRoutes = setOf("main", "generate", "webview", "profile")
    val showBottomBar = currentRoute in bottomBarRoutes

    // 处理初始分享 URL（仅一次）
    var sharedUrlProcessed by remember { mutableStateOf(false) }
    LaunchedEffect(initialSharedUrl) {
        if (initialSharedUrl != null && !sharedUrlProcessed) {
            sharedUrlProcessed = true
            Toast.makeText(context, "正在解析分享链接...", Toast.LENGTH_SHORT).show()

            val pageInfo = withContext(Dispatchers.IO) {
                WebPageParser.parse(initialSharedUrl)
            }

            val encodedTitle = java.net.URLEncoder.encode(pageInfo.title, "UTF-8")
            val encodedContent = java.net.URLEncoder.encode(pageInfo.description, "UTF-8")
            val encodedUrl = java.net.URLEncoder.encode(pageInfo.url, "UTF-8")
            val encodedImage = java.net.URLEncoder.encode(pageInfo.iconUrl, "UTF-8")

            navController.navigate("edit/new?title=$encodedTitle&content=$encodedContent&url=$encodedUrl&imageUrl=$encodedImage") {
                popUpTo("main") { saveState = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("首页") },
                        selected = currentRoute == "main",
                        onClick = {
                            navController.navigate("main") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                        label = { Text("生成") },
                        selected = currentRoute == "generate",
                        onClick = {
                            navController.navigate("generate") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("我的") },
                        selected = currentRoute in setOf("profile", "login", "settings"),
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(paddingValues)
        ) {
            // ========== 首页 ==========
            composable("main") {
                val viewModel: MainViewModel = viewModel(activity)
                LaunchedEffect(Unit) { viewModel.loadCards(context) }
                MainScreen(
                    cards = viewModel.cards.collectAsState().value,
                    onEditCard = { card ->
                        navController.navigate("edit/${card.id}")
                    },
                    onAddCard = {
                        navController.navigate("edit/new")
                    },
                    onShareCard = { card ->
                        ShareUtil.shareWebPage(
                            context = activity,
                            card = card,
                            listener = shareListener
                        )
                    },
                    onDeleteCard = { id ->
                        viewModel.deleteCard(context, id)
                    }
                )
            }

            // ========== 生成 ==========
            composable("generate") {
                GenerateScreen()
            }

            // ========== 我的 ==========
            composable("profile") {
                ProfileScreen(
                    onNavigateToLogin = { navController.navigate("login") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            // ========== 设置 ==========
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ========== 登录 ==========
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("profile") {
                            popUpTo("main") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // ========== 内置浏览器 WebView（登录后） ==========
            composable("webview") {
                WebViewScreen()
            }

            // ========== 编辑卡片 ==========
            composable(
                route = "edit/{cardId}?title={title}&content={content}&url={url}&imageUrl={imageUrl}",
                arguments = listOf(
                    navArgument("cardId") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                    navArgument("content") { type = NavType.StringType; defaultValue = "" },
                    navArgument("url") { type = NavType.StringType; defaultValue = "" },
                    navArgument("imageUrl") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId")
                val isNew = cardId == "new"
                val titleArg = backStackEntry.arguments?.getString("title") ?: ""
                val contentArg = backStackEntry.arguments?.getString("content") ?: ""
                val urlArg = backStackEntry.arguments?.getString("url") ?: ""
                val imageUrlArg = backStackEntry.arguments?.getString("imageUrl") ?: ""

                val editViewModel: EditCardViewModel = viewModel()
                val mainViewModel: MainViewModel = viewModel(activity)

                // 如果是从分享进入，预填数据
                if (isNew && titleArg.isNotBlank()) {
                    LaunchedEffect(titleArg) {
                        editViewModel.updateTitle(titleArg)
                        editViewModel.updateContent(contentArg)
                        editViewModel.updateUrl(urlArg)
                        editViewModel.updateImageUrl(imageUrlArg)
                    }
                }

                if (!isNew && cardId != null) {
                    LaunchedEffect(cardId) {
                        val cards = mainViewModel.cards.first()
                        val card = cards.find { it.id.toString() == cardId }
                        if (card != null) editViewModel.loadCard(card)
                    }
                }

                EditCardScreen(
                    viewModel = editViewModel,
                    onBack = { navController.popBackStack() },
                    onSaveOnly = { card ->
                        if (isNew) {
                            mainViewModel.addCard(context, card.title, card.content, card.url, card.imageUrl)
                        } else {
                            mainViewModel.updateCard(context, card)
                        }
                        navController.popBackStack()
                        // 保存成功后自动跳转分享
                        ShareUtil.shareWebPage(
                            context = activity,
                            card = card,
                            listener = shareListener
                        )
                    }
                )
            }
        }
    }
}
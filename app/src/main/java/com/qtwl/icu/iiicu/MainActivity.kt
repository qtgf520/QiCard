package com.qtwl.icu.iiicu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
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
import com.qtwl.icu.iiicu.ui.screens.EditCardScreen
import com.qtwl.icu.iiicu.ui.screens.LoginScreen
import com.qtwl.icu.iiicu.ui.screens.MainScreen
import com.qtwl.icu.iiicu.ui.screens.ProfileScreen
import com.qtwl.icu.iiicu.ui.screens.WebViewScreen
import com.qtwl.icu.iiicu.ui.theme.MyApplicationTheme
import com.qtwl.icu.iiicu.util.QQUtil
import com.qtwl.icu.iiicu.util.ShareUtil
import com.qtwl.icu.iiicu.util.UserManager
import com.qtwl.icu.iiicu.viewmodel.EditCardViewModel
import com.qtwl.icu.iiicu.viewmodel.MainViewModel
import com.tencent.tauth.IUiListener
import com.tencent.tauth.UiError
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    // QQ分享回调
    private val shareListener = object : IUiListener {
        override fun onComplete(response: Any?) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "分享成功！", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onError(error: UiError?) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "分享失败：${error?.errorMessage ?: "未知错误"}",
                    Toast.LENGTH_SHORT
                ).show()
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

        // 初始化QQ SDK
        QQUtil.init(this)
        // 初始化用户登录管理器
        UserManager.init(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // 底部导航栏在首页、我的、登录、WebView 页面都显示
                    val bottomBarRoutes = listOf("main", "profile", "login", "webview")
                    val showBottomBar = currentRoute in bottomBarRoutes

                    Scaffold(
                        topBar = {
                            when (currentRoute) {
                                "main" -> {
                                    CenterAlignedTopAppBar(
                                        title = { Text("綦桐网络") }
                                    )
                                }
                                "profile" -> {
                                    CenterAlignedTopAppBar(
                                        title = { Text("我的") }
                                    )
                                }
                                "webview" -> {
                                    CenterAlignedTopAppBar(
                                        title = {
                                            Text(
                                                if (UserManager.isLoggedIn()) UserManager.getUsername().ifEmpty { "我的" }
                                                else "我的"
                                            )
                                        },
                                        actions = {
                                            // 通知按钮
                                            IconButton(onClick = {
                                                Toast.makeText(this@MainActivity, "暂无新通知", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Notifications, contentDescription = "通知")
                                            }
                                            // 退出登录按钮（仅已登录时显示）
                                            if (UserManager.isLoggedIn()) {
                                                IconButton(onClick = {
                                                    UserManager.logout()
                                                    Toast.makeText(this@MainActivity, "已退出登录", Toast.LENGTH_SHORT).show()
                                                    // 跳转到登录页
                                                    navController.navigate("login") {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            saveState = false
                                                        }
                                                        launchSingleTop = true
                                                    }
                                                }) {
                                                    Icon(Icons.Default.ExitToApp, contentDescription = "退出登录")
                                                }
                                            }
                                        }
                                    )
                                }
                                else -> {
                                    // 其他页面（如编辑卡片）不显示顶栏
                                }
                            }
                        },
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                        label = { Text("首页") },
                                        selected = currentRoute == "main",
                                        onClick = {
                                            navController.navigate("main") {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        label = { Text("我的") },
                                        selected = currentRoute == "profile" || currentRoute == "login" || currentRoute == "webview",
                                        onClick = {
                                            if (UserManager.isLoggedIn()) {
                                                navController.navigate("webview") {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            } else {
                                                navController.navigate("login") {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
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
                                val context = LocalContext.current
                                val viewModel: MainViewModel = viewModel(context as ComponentActivity)
                                LaunchedEffect(Unit) {
                                    viewModel.loadCards(context)
                                }
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
                                            context = this@MainActivity,
                                            card = card,
                                            listener = shareListener
                                        )
                                    },
                                    onDeleteCard = { id ->
                                        viewModel.deleteCard(context, id)
                                    }
                                )
                            }

                            // ========== 我的 ==========
                            composable("profile") {
                                ProfileScreen(
                                    onNavigateToLogin = {
                                        navController.navigate("login")
                                    }
                                )
                            }

                            // ========== 登录 ==========
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = {
                                        // 登录成功直接进入内置浏览器 WebView
                                        navController.navigate("webview") {
                                            popUpTo("main") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }

                            // ========== 内置浏览器 WebView（我的） ==========
                            composable("webview") {
                                WebViewScreen()
                            }

                            // ========== 编辑卡片 ==========
                            composable(
                                route = "edit/{cardId}",
                                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val cardId = backStackEntry.arguments?.getString("cardId")
                                val isNew = cardId == "new"
                                val editViewModel: EditCardViewModel = viewModel()
                                val context = LocalContext.current
                                // 共享 Activity 级别的 MainViewModel（确保和主页面是同一个实例）
                                val mainViewModel: MainViewModel = viewModel(context as ComponentActivity)

                                if (!isNew && cardId != null) {
                                    LaunchedEffect(cardId) {
                                        val cards = mainViewModel.cards.first()
                                        val card = cards.find { it.id.toString() == cardId }
                                        if (card != null) {
                                            editViewModel.loadCard(card)
                                        }
                                    }
                                }

                                EditCardScreen(
                                    viewModel = editViewModel,
                                    onBack = { navController.popBackStack() },
                                    onSaveOnly = { card ->
                                        // 1. 先保存到存储
                                        if (isNew) {
                                            mainViewModel.addCard(
                                                context, card.title, card.content,
                                                card.url, card.imageUrl
                                            )
                                        } else {
                                            mainViewModel.updateCard(context, card)
                                        }
                                        // 2. 返回首页
                                        navController.popBackStack()
                                        // 3. 保存成功后自动跳转分享
                                        ShareUtil.shareWebPage(
                                            context = this@MainActivity,
                                            card = card,
                                            listener = shareListener
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * QQ互联回调转发
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        com.tencent.tauth.Tencent.onActivityResultData(requestCode, resultCode, data, shareListener)
    }
}
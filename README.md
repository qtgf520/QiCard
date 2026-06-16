# 綦桐网络 (QiTong Network)

![Version](https://img.shields.io/badge/version-7.0-blue)
![Kotlin](https://img.shields.io/badge/language-Kotlin-purple)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green)
![License](https://img.shields.io/badge/license-MIT-yellow)

**綦桐网络** 是一款基于 **Jetpack Compose** 构建的现代化 Android 应用，提供卡片内容管理、QQ 社交分享及内置 WebView 浏览等功能。

---

## ✨ 功能特性

### 📇 卡片管理
- 卡片列表展示（包含标题、内容简介、链接与图片）
- 新增/编辑/删除 卡片
- 保存后自动进入 QQ 分享流程

### 🌐 内置浏览器（WebView）
- 登录后进入内置浏览器页面
- 顶部状态栏（用户名显示 + 通知按钮 + 退出登录）
- 底部导航栏（首页 / 我的）

### 🔐 用户系统
- 登录/登出管理
- 持久化用户会话

### 🤝 QQ 社交分享
- 集成腾讯 QQ SDK
- 支持分享卡片（标题、简介、图片、链接）
- 兼容 Android 11+ 包可见性

### 📱 Material Design 3
- 现代化 Material You 设计语言
- 自适应主题配色
- 平滑动画与过渡

---

## 🛠️ 技术栈

| 技术 | 版本 |
|------|------|
| **Kotlin** | 2.3.x |
| **Jetpack Compose** | BOM 2026.01.01 |
| **Material Design 3** | ✅ |
| **Android API** | Target 35, Min 24 |
| **Gradle** | 9.x |
| **Navigation Compose** | ✅ |
| **Coil** (图片加载) | ✅ |
| **Gson** (JSON 解析) | ✅ |
| **QQ SDK** (社交分享) | ✅ |

---

## 🚀 快速开始

### 环境要求
- **JDK 17+**
- **Android SDK** (API 35)

### 克隆与构建

```bash
# 克隆项目
git clone https://github.com/你的用户名/qitong-network.git
cd qitong-network

# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 构建 Release APK（需配置签名）
./gradlew assembleRelease
```

### 关于签名
> 项目中的 `qitong.jks` 签名文件已从仓库中移除（出于安全考虑）。
> 如需发布 Release 版本，请参考以下方式配置签名：

```kotlin
// app/build.gradle.kts
signingConfigs {
    create("release") {
        storeFile = file("你的签名文件.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your_password"
        keyAlias = System.getenv("KEY_ALIAS") ?: "your_alias"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "your_password"
    }
}
```

> 推荐使用环境变量注入密码，避免硬编码到源码中。

### APK 输出位置
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 项目结构

```
qitong-network/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/qtwl/icu/iiicu/
│   │   │   │   ├── MainActivity.kt           # 主Activity（导航 + 顶底栏）
│   │   │   │   ├── model/
│   │   │   │   │   └── ShareCard.kt           # 卡片数据模型
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── MainScreen.kt      # 首页卡片列表
│   │   │   │   │   │   ├── LoginScreen.kt     # 登录页
│   │   │   │   │   │   ├── ProfileScreen.kt   # 我的页面
│   │   │   │   │   │   ├── EditCardScreen.kt  # 编辑卡片页
│   │   │   │   │   │   └── WebViewScreen.kt   # 内置浏览器页
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt           # 颜色定义
│   │   │   │   │       ├── Theme.kt           # 主题配置
│   │   │   │   │       └── Type.kt            # 字体配置
│   │   │   │   ├── util/
│   │   │   │   │   ├── UserManager.kt         # 用户管理（登录状态、Cookie）
│   │   │   │   │   ├── CardStorage.kt         # 卡片本地存储
│   │   │   │   │   ├── QQUtil.kt             # QQ SDK 工具
│   │   │   │   │   └── ShareUtil.kt           # 分享工具
│   │   │   │   └── viewmodel/
│   │   │   │       ├── MainViewModel.kt       # 首页 ViewModel
│   │   │   │       └── EditCardViewModel.kt   # 编辑页 ViewModel
│   │   │   ├── res/                           # 资源文件
│   │   │   │   ├── values/strings.xml
│   │   │   │   ├── values/colors.xml
│   │   │   │   └── ...
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle.kts
│   ├── libs/QQSDK.aar                        # QQ SDK
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml                     # 依赖版本管理
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── .gitignore
```

---

## 🔧 开发指南

### 常用 Gradle 命令
```bash
./gradlew tasks                         # 查看所有可用任务
./gradlew assembleDebug                 # 构建 Debug APK
./gradlew assembleRelease               # 构建 Release APK
./gradlew clean                         # 清理构建缓存
./gradlew test                          # 运行单元测试
./gradlew connectedAndroidTest          # 运行 Android 测试
```

### 修改应用名称
编辑 `app/src/main/res/values/strings.xml`：
```xml
<string name="app_name">綦桐网络</string>
```

### 修改应用图标
替换 `app/src/main/res/mipmap-*/ic_launcher.xml` 及相关资源文件。

---

## 📄 开源协议

本项目基于 **MIT License** 开源。

```
MIT License

Copyright (c) 2026 綦桐科技

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
SOFTWARE.
```

---

## 💬 关于

綦桐网络 (QiTong Network) —— 由 **綦桐科技** 开发维护。

如有问题或建议，欢迎提交 Issue 或 Pull Request！

---

**Made with ❤️ by 綦桐科技**
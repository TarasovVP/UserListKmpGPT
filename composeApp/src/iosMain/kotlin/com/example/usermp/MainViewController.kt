package com.example.usermp

import androidx.compose.ui.window.ComposeUIViewController
import com.example.usermp.shared.PlatformConfiguration
import com.example.usermp.shared.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin(PlatformConfiguration())
    return ComposeUIViewController { UserListApp() }
}

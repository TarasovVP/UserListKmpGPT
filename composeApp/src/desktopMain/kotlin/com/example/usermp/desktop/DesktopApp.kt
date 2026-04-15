package com.example.usermp.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.usermp.UserListApp
import com.example.usermp.shared.PlatformConfiguration
import com.example.usermp.shared.initKoin

fun main() = application {
    initKoin(PlatformConfiguration())

    Window(
        onCloseRequest = ::exitApplication,
        title = "User List KMP",
    ) {
        UserListApp()
    }
}

package com.example.usermp

import android.app.Application
import com.example.usermp.shared.PlatformConfiguration
import com.example.usermp.shared.initKoin

class UserListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(PlatformConfiguration(this))
    }
}

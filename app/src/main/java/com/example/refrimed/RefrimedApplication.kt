package com.example.refrimed

import android.app.Application
import com.example.refrimed.data.AppContainer
import com.example.refrimed.data.AppDataContainer

class RefrimedApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
package com.maomei.petchatapp

import android.app.Application
import com.maomei.petchatapp.di.AppContainer

class PetChatApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

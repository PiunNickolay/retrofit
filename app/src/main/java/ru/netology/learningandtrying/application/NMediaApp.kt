package ru.netology.learningandtrying.application

import android.app.Application
import ru.netology.learningandtrying.auth.AppAuth

class NMediaApp: Application() {
    override fun onCreate() {
        super.onCreate()
        AppAuth.initAuth(this)
    }
}
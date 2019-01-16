package com.effective.android.wxrp

import android.app.Application

class RpApplication : Application() {

    companion object {
        @Volatile
        private var instance: Application? = null

        @Synchronized
        fun getApplication(): Application {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
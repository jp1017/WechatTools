package com.effective.android.wxrp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.effective.android.wxrp.utils.Logger

/**
 * 保活使用
 */
class WXRPService : Service() {

    override fun onCreate() {
        Logger.i(TAG, "onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Logger.i(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Logger.i(TAG, "onBind")
        return null
    }

    override fun onDestroy() {
        Logger.i(TAG, "onDestroy")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "WXRPService"
    }
}
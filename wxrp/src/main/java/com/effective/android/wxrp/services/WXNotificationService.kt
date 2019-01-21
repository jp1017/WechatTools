package com.effective.android.wxrp.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Toast
import com.effective.android.wxrp.*
import com.effective.android.wxrp.utils.AccessibilityUtil
import com.effective.android.wxrp.utils.Logger
import com.effective.android.wxrp.utils.ToolUtil

/**
 * 通知栏service
 * created by yummylau
 */
class WXNotificationService: NotificationListenerService() {

    companion object {
        private const val TAG = "WXNotificationService"
    }
    private var wqNotificationService: WXNotificationService? = null

    override fun onListenerConnected() {
        Logger.i(TAG, "onListenerConnected")
        Toast.makeText(this, "监听微信通知栏 服务已开启", Toast.LENGTH_LONG).show()
        if (wqNotificationService == null) {
            wqNotificationService = this
        }
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        Logger.i(TAG, "onListenerDisconnected")
        Toast.makeText(this, "监听微信通知栏 服务已断开", Toast.LENGTH_LONG).show()
        wqNotificationService = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (Constants.isGotNotification || Constants.isClickedNewMessageList || Constants.isGotPacket) {
            Logger.i(TAG, "onNotificationPosted: return \n"
                    + "WQ.isGotNotification = " + Constants.isGotNotification
                    + "WQ.isClickedNewMessageList = " + Constants.isClickedNewMessageList
                    + "WQ.isGotPacket = " + Constants.isGotPacket)
            return
        }
        Logger.i(TAG, "onNotificationPosted: " + sbn.packageName
                + " " + sbn.notification.extras.getString(Notification.EXTRA_TEXT))
        if (ToolUtil.isLockScreen(this.application)) {
            ToolUtil.wakeAndUnlock(this.application)
            Constants.isPreviouslyLockScreen = true
        }
        if (AccessibilityUtil.openNotification(sbn, Constants.PACKAFEGE_WECHAT, Constants.TEXT_WX_PACKET)) {
            // WQ.isGotNotification = true;
            /* handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    WQ.isGotNotification = false;
                }
            }, 500); */
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Logger.i(TAG, "onNotificationRemoved: " + sbn.toString())
        // WQ.isGotNotification = false;
        // super.onNotificationRemoved(sbn);
    }

    override fun onDestroy() {
        Logger.i(TAG, "onDestroy")
        wqNotificationService = null
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent): Boolean {
        Logger.i(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        Logger.i(TAG, "onBind!")
        return super.onBind(intent)
    }

    fun restarNotificationListenerService(context: Context) {
        Logger.i(TAG, "restarNotificationListenerService")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationListenerService.requestRebind(ComponentName(context, WXNotificationService::class.java))
        } else {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                    ComponentName(context, WXNotificationService::class.java!!),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

            pm.setComponentEnabledSetting(
                    ComponentName(context, WXNotificationService::class.java!!),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    fun getWqNotificationService(): WXNotificationService? {
        return if (wqNotificationService != null) {
            wqNotificationService
        } else null
    }
}
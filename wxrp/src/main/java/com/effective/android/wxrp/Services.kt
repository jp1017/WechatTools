package com.effective.android.wxrp

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.ArrayList


/**
 * 通知栏service
 */
class WQNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "WQNotificationService"
    }

    private var wqNotificationService: WQNotificationService? = null

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
        if (Tools.isLockScreen(this.application)) {
            Tools.wakeAndUnlock(this.application)
            Constants.isPreviouslyLockScreen = true
        }
        if (AccessibilityHelper.openNotification(sbn, Constants.WECHAT_PACKAGE_NAME, Constants.WT_PACKET)) {
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
            NotificationListenerService.requestRebind(ComponentName(context, WQNotificationService::class.java))
        } else {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                    ComponentName(context, WQNotificationService::class.java!!),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

            pm.setComponentEnabledSetting(
                    ComponentName(context, WQNotificationService::class.java!!),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    fun getWqNotificationService(): WQNotificationService? {
        return if (wqNotificationService != null) {
            wqNotificationService
        } else null
    }
}


/**
 * 助手service
 */
class WQAccessibilityService : AccessibilityService() {

    companion object {
        private var service: WQAccessibilityService? = null
        private const val TAG = "WQAccessibilityService"

        fun getService(): AccessibilityService? {
            return service!!
        }
    }


    private var highSpeedMode: HighSpeedMode? = null
    private var compatibleMode: CompatibleMode? = null
    private var config: Config? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        val eventType = accessibilityEvent.eventType
        val className = accessibilityEvent.className.toString()
        val rootNode = rootInActiveWindow
        // AccessibilityNodeInfo rootNode = findCurrentWindows(accessibilityEvent, WQ.WECHAT);

        Logger.i(TAG, "onAccessibilityEvent eventType = " + eventType + "className = " + className)
        when (eventType) {
            // 第一步：监听通知栏消息
            /*case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
                if (WQ.isGotNotification) {
                    return;
                }
                WQ.isGotNotification = true;
                Logger.i(TAG, "通知栏消息改变");
                if (Tools.isLockScreen(this.getApplication())) {
                    Tools.wakeAndUnlock(this.getApplication());
                    WQ.isPreviouslyLockScreen = true;
                }
                AccessibilityHelper.openNotification(accessibilityEvent, WQ.WT_PACKET);
                WQ.isGotNotification = false;
                break;
            }*/
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Logger.i(TAG, "窗口状态改变 className = $className")
                if (config?.getRunningMode() == Config.compatibleMode) {
                    compatibleMode?.dealWindowStateChanged(className, rootNode)
                } else {
                    highSpeedMode?.dealWindowStateChanged(className, rootNode)
                }
            }/*if (WQ.isPreviouslyLockScreen && WQ.currentAutoPacketStatus == WQ.W_rebackUIStatus) {
                    WQ.isPreviouslyLockScreen = false;
                    WQ.setCurrentAutoPacketStatus(WQ.W_waitStatus);
                    AccessibilityHelper.sleepAndLock(this.getApplication());
                }*/
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Logger.i(TAG, "窗口内容变化");
                if (config?.getRunningMode() == Config.compatibleMode) {
                    // 联系人列表
                    compatibleMode?.dealWindowContentChanged(rootNode)
                } else {
                    highSpeedMode?.dealWindowContentChanged(className, rootNode)
                }
            }
            else -> {
            }
        }
        rootNode?.recycle()
    }

    override fun onInterrupt() {
        Logger.i(TAG, "onInterrupt")
        Toast.makeText(this, "Hello World 模拟操作 服务被中断", Toast.LENGTH_LONG).show()
    }

    override fun onServiceConnected() {
        Logger.i(TAG, "onServiceConnected")
        Toast.makeText(this, "Hello World 模拟操作 服务已连接", Toast.LENGTH_LONG).show()
        service = this
        initObj()
        super.onServiceConnected()
    }

    override fun onDestroy() {
        service = null
        super.onDestroy()
    }

    private fun findRootInWindows(windows: ArrayList<AccessibilityNodeInfo>, ViewID: String): AccessibilityNodeInfo? {
        for (i in windows.indices) {
            if (windows[i] != null) {
                val packetList = windows[i].findAccessibilityNodeInfosByViewId(ViewID)
                if (!packetList.isEmpty()) {
                    if (packetList[0].isClickable) {
                        return packetList[0]
                    }
                }
            }
        }
        Logger.i(TAG, "findRootInWindows == null")
        return null
    }

    private fun findCurrentWindows(accessibilityEvent: AccessibilityEvent, title: String): AccessibilityNodeInfo? {
        var windowListRoots: ArrayList<AccessibilityNodeInfo>? = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val windowList = windows
            if (windowList.size > 0) {
                for (window in windowList) {
                    Logger.i(TAG, "findCurrentWindows " + window.toString())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (window.title != null) {
                            if (window.title!!.toString() == title) {
                                return window.root
                            }
                        }
                    } else {
                        windowListRoots!!.add(window.root)
                    }
                }
            }
        } else {
            val windowSource = accessibilityEvent.source
            var windowChild: AccessibilityNodeInfo
            if (windowSource != null) {
                for (i in 0 until windowSource.childCount) {
                    windowChild = windowSource.getChild(i)
                    windowListRoots!!.add(windowChild)
                }
            } else {
                windowListRoots = null
            }
        }
        if (windowListRoots != null) {
            Logger.i(TAG, "findCurrentWindows size = " + windowListRoots.size)
        }
        return if (windowListRoots!!.size > 0) {
            windowListRoots[windowListRoots.size - 1]
        } else {
            null
        }
    }

    private fun initObj() {
        if (highSpeedMode == null) {
            highSpeedMode = HighSpeedMode()
        }
        if (compatibleMode == null) {
            compatibleMode = CompatibleMode()
        }
        if (config == null) {
            config = Config.getConfig(getService())
        }
    }
}

/**
 * 全局service
 */
class WQService : Service() {

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
        private const val TAG = "WQService"
    }
}
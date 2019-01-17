package com.effective.android.wxrp.services

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.effective.android.wxrp.mode.PacketManager
import com.effective.android.wxrp.utils.Logger
import com.effective.android.wxrp.utils.ToolUtil
import java.util.ArrayList

class WXAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "WXAccessibilityService"
        private var service: WXAccessibilityService? = null
        fun getService(): AccessibilityService? {
            return service
        }
    }

    private var packetManager: PacketManager? = null

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
                AccessibilityHelper.openNotification(accessibilityEvent, WQ.TEXT_WX_PACKET);
                WQ.isGotNotification = false;
                break;
            }*/
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Logger.i(TAG, "窗口状态改变 className = $className")
                packetManager?.dealWindowStateChanged(className, rootNode)
            }/*if (WQ.isPreviouslyLockScreen && WQ.currentAutoPacketStatus == WQ.W_rebackUIStatus) {
                    WQ.isPreviouslyLockScreen = false;
                    WQ.setCurrentAutoPacketStatus(WQ.W_waitStatus);
                    AccessibilityHelper.sleepAndLock(this.getApplication());
                }*/
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                Logger.i(TAG, "窗口内容变化")
                packetManager?.dealWindowContentChanged(className, rootNode)
            }
            else -> {
            }
        }
        rootNode?.recycle()
    }

    override fun onInterrupt() {
        Logger.i(TAG, "onInterrupt")
        ToolUtil.toast(this, "Hello World 模拟操作 服务被中断")
    }

    override fun onServiceConnected() {
        Logger.i(TAG, "onServiceConnected")
        ToolUtil.toast(this, "Hello World 模拟操作 服务已连接");
        service = this
        if (packetManager == null) {
            packetManager = PacketManager()
        }
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
}
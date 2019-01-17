package com.effective.android.wxrp.utils

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.support.v4.app.NotificationManagerCompat
import android.widget.Toast
import com.effective.android.wxrp.Constants

class ToolUtil private constructor() {
    companion object {
        private const val TAG = "Tools"

        fun toast(context: Context, msg: String) = Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

        fun isServiceRunning(context: Context, className: String): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val serviceList = activityManager.getRunningServices(1000)
            if (serviceList.size < 0) {
                return false
            }
            for (i in serviceList.indices) {
                if (serviceList[i].service.className == className) {
                    return true
                }
            }
            return false
        }

        fun isNotificationListenerServiceEnabled(context: Context): Boolean {
            val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
            if (packageNames.contains(context.packageName)) {
                Logger.i(TAG, "isNotificationListenerServiceEnabled = true")
                return true
            }
            Logger.i(TAG, "isNotificationListenerServiceEnabled = false")
            return false
        }

        fun wakeAndUnlock(context: Context) {
            Logger.i(TAG, "wakeAndUnlock")
            //获取电源管理器对象
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright")
            //点亮屏幕
            wl.acquire(1000)
            //得到键盘锁管理器对象
            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val kl = km.newKeyguardLock("unLock")
            //解锁,加上会自动解锁，比较危险
            kl.disableKeyguard()
        }

        /** 是否为锁屏或黑屏状态 */
        fun isLockScreen(context: Context): Boolean {
            val result: Boolean
            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isScreenOn: Boolean
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                isScreenOn = pm.isInteractive
            } else {
                isScreenOn = pm.isScreenOn
            }
            result = km.inKeyguardRestrictedInputMode() || !isScreenOn
            Logger.i(TAG, "isLockScreen = $result")
            return result
        }

        fun sleepAndLock(context: Context) {
            Logger.i(TAG, "sleepAndLock")
            //获取电源管理器对象
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright")
            //点亮屏幕
            wl.acquire(1000)
            //得到键盘锁管理器对象
            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val kl = km.newKeyguardLock("unLock")
            //锁屏
            kl.reenableKeyguard()
            //释放wakeLock，关灯
            wl.release()
        }

        fun getWeChatVersion(context: Context): String? {
            val packageManager = context.packageManager
            val packageInfos = packageManager.getInstalledPackages(0)
            for (packageInfo in packageInfos) {
                if (packageManager.getApplicationLabel(packageInfo.applicationInfo).toString() == Constants.WECHAT) {
                    return packageInfo.versionName
                }
            }
            return null
        }
    }
}

package com.effective.android.wxrp

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.PowerManager
import android.support.v4.app.NotificationManagerCompat
import android.util.Log

class Config private constructor() {

    private var sharedPreferences: SharedPreferences
    private var context: Context

    init {
        this.context = RpApplication.getApplication()
        this.sharedPreferences = context.getSharedPreferences(WonderConfig, Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "Config"
        private const val WonderConfig = "wonderConfig"
        private val IS_GOT_PACKET_SELF = "isGotPacketSelf"
        private val IS_USED_DELAYED = "isUsedDelayed"
        private val IS_USED_RANDOM_DELAYED = "isUsedRandomDelayed"
        private val DELAYED_TIME = "delayedTime"
        private val IS_USED_KEY_WORDS = "isUsedKeyWords"
        private val PACKET_KEY_WORDS = "packetKeyWords"


        private var isGotPacketSelf: Boolean = true
        private var isUsedDelayed: Boolean = true
        private var isUsedRandomDelayed: Boolean = true
        private var delayedTime: Int = 100
        private var isUsedKeyWords: Boolean = true
        private var packetKeyWords: String = "测试、挂、专属、生日、踢"


        @Volatile
        private var instance: Config? = null

        @Synchronized
        fun getInstance(): Config {
            Logger.i(TAG, "getInstance")
            if (instance == null) {
                instance = Config()
            }
            return instance!!
        }
    }

    fun saveAllConfig() {
        sharedPreferences.edit()
                .putBoolean(IS_GOT_PACKET_SELF, isGotPacketSelf)
                .putBoolean(IS_USED_DELAYED, isUsedDelayed)
                .putBoolean(IS_USED_RANDOM_DELAYED, isUsedRandomDelayed)
                .putInt(DELAYED_TIME, delayedTime)
                .putBoolean(IS_USED_KEY_WORDS, isUsedKeyWords)
                .putString(PACKET_KEY_WORDS, packetKeyWords)
                .apply()
    }

    fun getAllConfig() {
        isGotPacketSelf = sharedPreferences.getBoolean(IS_GOT_PACKET_SELF, isGotPacketSelf)
        isUsedDelayed = sharedPreferences.getBoolean(IS_USED_DELAYED, isUsedDelayed)
        isUsedRandomDelayed = sharedPreferences.getBoolean(IS_USED_RANDOM_DELAYED, isUsedRandomDelayed)
        delayedTime = sharedPreferences.getInt(DELAYED_TIME, delayedTime)
        isUsedKeyWords = sharedPreferences.getBoolean(IS_USED_KEY_WORDS, isUsedKeyWords)
        packetKeyWords = sharedPreferences.getString(PACKET_KEY_WORDS, packetKeyWords)
    }


    fun getIsGotPacketSelf() = sharedPreferences.getBoolean(IS_GOT_PACKET_SELF, isGotPacketSelf)

    fun getIsUsedDelayed() = sharedPreferences.getBoolean(IS_USED_DELAYED, isUsedDelayed)

    fun getIsUsedRandomDelayed() = sharedPreferences.getBoolean(IS_USED_RANDOM_DELAYED, isUsedRandomDelayed)

    fun getDelayedTime() = sharedPreferences.getInt(DELAYED_TIME, delayedTime)

    fun getIsUsedKeyWords() = sharedPreferences.getBoolean(IS_USED_KEY_WORDS, isUsedKeyWords)

    fun getPacketKeyWords() = sharedPreferences.getString(PACKET_KEY_WORDS, packetKeyWords)

    fun saveIsGotPacketSelf(b: Boolean) = sharedPreferences.edit().putBoolean(IS_GOT_PACKET_SELF, b).apply()

    fun saveIsUsedDelayed(b: Boolean) = sharedPreferences.edit().putBoolean(IS_USED_DELAYED, b).apply()

    fun saveIsUsedRandomDelayed(b: Boolean) = sharedPreferences.edit().putBoolean(IS_USED_RANDOM_DELAYED, b).apply()

    fun saveDelayedTime(time: Int) = sharedPreferences.edit().putInt(DELAYED_TIME, time).apply()

    fun saveIsUsedKeyWords(b: Boolean) = sharedPreferences.edit().putBoolean(IS_USED_KEY_WORDS, b).apply()

    fun savePacketKeyWords(keyWords: String) = sharedPreferences.edit().putString(PACKET_KEY_WORDS, keyWords).apply()

}

class Logger private constructor() {
    companion object {
        private const val TAG = "logger - "
        fun e(tag: String, msg: String) = Log.e("$TAG $tag", msg)
        fun w(tag: String, msg: String) = Log.w("$TAG $tag", msg)
        fun i(tag: String, msg: String) = Log.i("$TAG $tag", msg)
        fun d(tag: String, msg: String) = Log.d("$TAG $tag", msg)
        fun v(tag: String, msg: String) = Log.v("$TAG $tag", msg)
    }
}

class Tools private constructor() {
    companion object {
        private val TAG = "Tools"

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
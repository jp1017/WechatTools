package com.effective.android.wxrp

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        config = Config.getConfig(this)
        initViewState()
        setClickListener()
    }

    private fun initViewState() {
        if (config.getRunningMode() == Config.highSpeedMode) {
            if (Tools.isSupportHighSpeedMode(this@MainActivity)) {
                cb_highSpeedMode.isChecked = true
            } else {
                cb_highSpeedMode.isChecked = false
                config.saveRunningMode(Config.compatibleMode)
                cb_usedKeyWords.isChecked = false
                config.saveIsUsedKeyWords(false)
            }
        } else {
            cb_highSpeedMode.isChecked = false
            cb_usedKeyWords.isChecked = false
            config.saveIsUsedKeyWords(false)
        }
        cb_notification.isChecked = Tools.isServiceRunning(this, Constants.SELF_PACKAGE_NAME + "."
                + Constants.SELFCN_NOTIFICATION)

        cb_accessibility.isChecked = Tools.isServiceRunning(this, Constants.SELF_PACKAGE_NAME + "."
                + Constants.SELFCN_ACCESSBILITY)

        cb_getPacketSelf.isChecked = config.getIsGotPacketSelf()
        cb_usedDelayed.isChecked = config.getIsUsedDelayed()
        cb_usedRandomDelayed.isChecked = config.getIsUsedRandomDelayed()
        cb_usedKeyWords.isChecked = config.getIsUsedKeyWords()
        et_delayedTime.setText(config.getDelayedTime().toString())
        et_keyWords.setText(config.getPacketKeyWords())
        bt_test.visibility = View.INVISIBLE
    }

    private fun setClickListener() {
        cb_highSpeedMode.setOnClickListener {
            if (cb_highSpeedMode.isChecked) {
                if (Tools.isSupportHighSpeedMode(this@MainActivity)) {
                    config.saveRunningMode(Config.highSpeedMode)
                } else {
                    cb_highSpeedMode.isChecked = false
                    config.saveRunningMode(Config.compatibleMode)
                    Toast.makeText(this@MainActivity, "当前微信版本不支持高速模式！",
                            Toast.LENGTH_LONG).show()
                }
            } else {
                config.saveRunningMode(Config.compatibleMode)
                config.saveIsUsedKeyWords(false)
                cb_usedKeyWords.isChecked = false
            }
        }

        cb_getPacketSelf.setOnClickListener {
            if (cb_getPacketSelf.isChecked) {
                config.saveIsGotPacketSelf(true)
            } else {
                config.saveIsGotPacketSelf(false)
            }
        }

        cb_usedDelayed.setOnClickListener {
            if (cb_usedDelayed.isChecked) {
                config.saveIsUsedDelayed(true)
                if (cb_usedRandomDelayed.isChecked) {
                    cb_usedRandomDelayed.isChecked = false
                    config.saveIsUsedRandomDelayed(false)
                }
            } else {
                config.saveIsUsedDelayed(false)
            }
        }

        cb_usedRandomDelayed.setOnClickListener {
            if (cb_usedRandomDelayed.isChecked) {
                config.saveIsUsedRandomDelayed(true)
                if (cb_usedDelayed.isChecked) {
                    cb_usedDelayed.isChecked = false
                    config.saveIsUsedDelayed(false)
                }
            } else {
                config.saveIsUsedRandomDelayed(false)
            }
        }
        cb_usedKeyWords.setOnClickListener {
            if (cb_usedKeyWords.isChecked) {
                if (config.getRunningMode() != Config.compatibleMode) {
                    config.saveIsUsedKeyWords(true)
                } else {
                    cb_usedKeyWords.isChecked = false
                    config.saveIsUsedKeyWords(false)
                    Toast.makeText(this@MainActivity, "目前兼容模式下不支持关键字过滤",
                            Toast.LENGTH_LONG).show()
                }
            } else {
                config.saveIsUsedKeyWords(false)
            }
        }

        cb_notification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(application, "当前安卓系统版本过低，请手动设置本应用的通知读取权限",
                        Toast.LENGTH_LONG).show()
                cb_notification.isChecked = Tools.isServiceRunning(application,
                        Constants.SELF_PACKAGE_NAME + "." + Constants.SELFCN_NOTIFICATION)
            }
        }

        cb_accessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        bt_test.setOnClickListener {
            // WQcb_notificationService.restarcb_notificationListenerService(getApplication());
        }
    }

    private fun getEditTextContent() {
        val delayedTime = Integer.parseInt(et_delayedTime.text.toString())
        val keyWords = et_keyWords.text.toString()
        Logger.i(TAG, "getEditTextContent delayTime = " + delayedTime
                + "et_keyWords = " + keyWords)
        config.savePacketKeyWords(keyWords)
        config.saveDelayedTime(delayedTime)
    }

    override fun onResume() {
        initViewState()
        super.onResume()
    }

    override fun onPause() {
        getEditTextContent()
        super.onPause()
    }

    override fun onStop() {
        getEditTextContent()
        super.onStop()
    }

}

package com.effective.android.wxrp.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.effective.android.wxrp.R
import com.effective.android.wxrp.store.Config
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var settingFragment: SettingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setting.setOnClickListener {
            showSettingFragment()
        }
    }

    private fun showSettingFragment() {
        if (settingFragment == null) {
            settingFragment = SettingFragment()
            supportFragmentManager.beginTransaction().replace(R.id.setting_container, settingFragment!!).commit()
        } else {
            supportFragmentManager.beginTransaction().show(settingFragment!!).commit()
        }
    }

    private fun hideSettingFragment() {
        supportFragmentManager.beginTransaction().hide(settingFragment!!).commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        Config.onSave()
    }

    //    private fun init() {
//        initViewState()
//        setClickListener();
//    }
//
//    private fun initViewState() {
//        cb_notification.isChecked = ToolUtil.isServiceRunning(this, Constants.PACKAGE_SELF_APPLICATION + "."
//                + Constants.CLASS_NOTIFICATION)
//
//        cb_accessibility.isChecked = ToolUtil.isServiceRunning(this, Constants.PACKAGE_SELF_APPLICATION + "."
//                + Constants.CLASS_ACCESSBILITY)￼
//        cb_getPacketSelf.isChecked = Config.INSTANCE.getIsGotPacketSelf()
//        cb_usedDelayed.isChecked = Config.INSTANCE.getIsUsedDelayed()
//        cb_usedRandomDelayed.isChecked = Config.INSTANCE.getIsUsedRandomDelayed()
//        cb_usedKeyWords.isChecked = Config.INSTANCE.getIsUsedKeyWords()
//        et_delayedTime.setText(Config.INSTANCE.getDelayedTime().toString())
//        et_keyWords.setText(Config.INSTANCE.getPacketKeyWords())
//        bt_test.visibility = View.INVISIBLE
//    }
//
//    private fun setClickListener() {
//
//        cb_getPacketSelf.setOnClickListener {
//            if (cb_getPacketSelf.isChecked) {
//                Config.INSTANCE.saveIsGotPacketSelf(true)
//            } else {
//                Config.INSTANCE.saveIsGotPacketSelf(false)
//            }
//        }
//
//        cb_usedDelayed.setOnClickListener {
//            if (cb_usedDelayed.isChecked) {
//                Config.INSTANCE.saveIsUsedDelayed(true)
//                if (cb_usedRandomDelayed.isChecked) {
//                    cb_usedRandomDelayed.isChecked = false
//                    Config.INSTANCE.saveIsUsedRandomDelayed(false)
//                }
//            } else {
//                Config.INSTANCE.saveIsUsedDelayed(false)
//            }
//        }
//
//        cb_usedRandomDelayed.setOnClickListener {
//            if (cb_usedRandomDelayed.isChecked) {
//                Config.INSTANCE.saveIsUsedRandomDelayed(true)
//                if (cb_usedDelayed.isChecked) {
//                    cb_usedDelayed.isChecked = false
//                    Config.INSTANCE.saveIsUsedDelayed(false)
//                }
//            } else {
//                Config.INSTANCE.saveIsUsedRandomDelayed(false)
//            }
//        }
//        cb_usedKeyWords.setOnClickListener {
//            if (cb_usedKeyWords.isChecked) {
//                Config.INSTANCE.saveIsUsedKeyWords(true)
//            } else {
//                Config.INSTANCE.saveIsUsedKeyWords(false)
//            }
//        }
//
//        cb_notification.setOnClickListener {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//            } else {
//                Toast.makeText(application, "当前安卓系统版本过低，请手动设置本应用的通知读取权限",
//                        Toast.LENGTH_LONG).show()
//                cb_notification.isChecked = ToolUtil.isServiceRunning(application,
//                        Constants.PACKAGE_SELF_APPLICATION + "." + Constants.CLASS_NOTIFICATION)
//            }
//        }
//
//        cb_accessibility.setOnClickListener {
//            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//
//    }
//
//    private fun getEditTextContent() {
//        val delayedTime = Integer.parseInt(et_delayedTime.text.toString())
//        val keyWords = et_keyWords.text.toString()
//        Logger.i(TAG, "getEditTextContent delayTime = " + delayedTime
//                + "et_keyWords = " + keyWords)
//        Config.INSTANCE.savePacketKeyWords(keyWords)
//        Config.INSTANCE.saveDelayedTime(delayedTime)
//    }

    override fun onBackPressed() {
        if (settingFragment != null && settingFragment!!.isVisible) {
            hideSettingFragment()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
//        initViewState()
        super.onResume()
    }

    override fun onPause() {
//        getEditTextContent()
        super.onPause()
    }

    override fun onStop() {
//        getEditTextContent()
        super.onStop()
    }

}

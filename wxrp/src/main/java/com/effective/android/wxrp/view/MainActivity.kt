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

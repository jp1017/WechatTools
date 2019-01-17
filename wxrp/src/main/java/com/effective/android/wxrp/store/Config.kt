package com.effective.android.wxrp.store

import android.support.annotation.IntegerRes
import com.effective.android.wxrp.RpApplication
import java.lang.StringBuilder
import java.util.*

class Config private constructor() {

    companion object {

        private const val SPLIT_POINT = "_&_"

        //是否打开自己红包
        private const val KEY_OPEN_GET_SELF_PACKET = "key_open_get_self_packet"
        private var openGetSelfPacket: Boolean = true

        //是否支持过滤
        private const val KEY_OPEN_FILTER_TAG = "key_open_filter_tag"
        private const val KEY_FILTER_TAG_DATA = "filterTagData"           //"测_&_挂_&_专属_&_生日_&_踢"
        private var openFilterTag: Boolean = false
        val filterTags = ArrayList<String>()
        private var defaultTagsString: String = "测_&_挂_&_专属_&_生日_&_踢"

        //是否支持延迟：分为 无延迟，固定延迟和随机延迟
        private const val KEY_DELAY_OPTION = "key_delay_option"
        private const val KEY_DELAY_NUM_DATA = "delayRandomNum"           //随机最大数 {$fixationDelayTime}_&_{randomDelayTime}
        private var defaultTimesString: String = "100_&_100"
        const val DELAY_OPTION_NONE = 0
        const val DELAY_OPTION_FOXATION = 1
        const val DELAY_OPTIOM_RANDOM = 2
        private var delayOption = DELAY_OPTION_NONE
        var fixationDelayTime: Int = 100
        var randomDelayTime: Int = 100


        /**
         * 应用启动时需要初始化
         */
        fun init() {
            openGetSelfPacket = RpApplication.SP().getBoolean(KEY_OPEN_GET_SELF_PACKET, openGetSelfPacket)

            val list = RpApplication.SP().getString(KEY_FILTER_TAG_DATA, defaultTagsString)!!.split(SPLIT_POINT).toList()
            filterTags.addAll(list)
            openFilterTag = RpApplication.SP().getBoolean(KEY_OPEN_FILTER_TAG, openFilterTag)

            delayOption = RpApplication.SP().getInt(KEY_DELAY_OPTION, delayOption)
            val times = RpApplication.SP().getString(KEY_DELAY_NUM_DATA, defaultTimesString)!!.split(SPLIT_POINT).toList()
            if (!times.isEmpty() && times.size == 2) {
                fixationDelayTime = times[0].toInt()
                randomDelayTime = times[1].toInt()
            }
        }

        fun onSave() {
            var tagsString = StringBuilder()
            var isFirst = true
            filterTags?.map {
                if (isFirst) {
                    tagsString.append(it)
                    isFirst = false
                } else {
                    tagsString.append(SPLIT_POINT + it)
                }
            }

            var timesString = StringBuilder()
            timesString.append(fixationDelayTime)
            timesString.append(SPLIT_POINT)
            timesString.append(randomDelayTime)

            RpApplication.SP().edit()
                    .putBoolean(KEY_OPEN_GET_SELF_PACKET, openGetSelfPacket)
                    .putBoolean(KEY_OPEN_FILTER_TAG, openFilterTag).putString(KEY_FILTER_TAG_DATA, tagsString.toString())
                    .putInt(KEY_DELAY_OPTION, delayOption).putString(KEY_DELAY_NUM_DATA, timesString.toString())
                    .apply()
        }


        fun openGetSelfPacket(b: Boolean) {
            openGetSelfPacket = b
        }

        fun isOpenGetSelfPacket(): Boolean = openGetSelfPacket

        fun openFilterTag(b: Boolean) {
            openFilterTag = b
        }

        fun isOpenFilterTag(): Boolean = openFilterTag


        fun setDelayOption(option: Int) {
            delayOption = when (option) {
                Config.DELAY_OPTION_NONE -> {
                    Config.DELAY_OPTION_NONE
                }
                Config.DELAY_OPTION_FOXATION -> {
                    Config.DELAY_OPTION_FOXATION
                }
                Config.DELAY_OPTIOM_RANDOM -> {
                    Config.DELAY_OPTIOM_RANDOM
                }
                else -> {
                    Config.DELAY_OPTION_NONE
                }
            }
        }

        fun getDelayOption(): Int = delayOption

        fun getDelayTime(): Int = getDelayTime(getDelayOption())

        private fun getDelayTime(option: Int): Int {
            return when (option) {
                Config.DELAY_OPTION_NONE -> {
                    0
                }
                Config.DELAY_OPTION_FOXATION -> {
                    fixationDelayTime
                }
                Config.DELAY_OPTIOM_RANDOM -> {
                    generateRandomNum(randomDelayTime)
                }
                else -> {
                    0
                }
            }
        }

        private fun generateRandomNum(n: Int): Int {
            val rand = Random()
            return rand.nextInt(n + 1)
        }
    }
}
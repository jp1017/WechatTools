package com.effective.android.wxrp.mode

import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.accessibility.AccessibilityNodeInfo
import com.effective.android.wxrp.Constants
import com.effective.android.wxrp.store.Config
import com.effective.android.wxrp.store.db.PacketRecord
import com.effective.android.wxrp.utils.AccessibilityUtil
import com.effective.android.wxrp.utils.Logger
import java.util.*

class EventScheduling {

    companion object {
        private const val TAG = "EventScheduling"
    }

    private val getPacketList: ArrayList<AccessibilityNodeInfo> = ArrayList()
    private val openPacketList: ArrayList<AccessibilityNodeInfo> = ArrayList()

    init {
        initBackThread()
    }

    private val timeResetSelfPacketStatus = 1000
    private val timeResetIsClickedNewMessageList = 500
    private val timeResetIsGotPacket = 500
    private val timeResetBackToMessageListStatus = 2000

    private val msgGetPacket = 0
    private val msgOpenPacket = 1
    private val msgPacketRecord = 6
    private val msgResetSelfPacketStatus = 2
    private val msgResetIsClickedNewMessageList = 3
    private val msgResetIsGotPacket = 4
    private val msgResetBackToMessageListStatus = 5

    private var checkMsgThread: HandlerThread? = null
    private var checkMsgHandler: Handler? = null

    /**
     * 添加红包列表,用于点击弹出红包对话框
     */
    fun addGetPacketList(nodeInfo: AccessibilityNodeInfo) {
        var delayedTime = Config.getDelayTime(false)
        if (delayedTime > 0) {
            delayedTime /= 2
        }
        Logger.i(TAG, "addGetPacketList delayedTime = $delayedTime")
        if (getPacketList.isEmpty()) {
            Logger.i(TAG, "addGetPacketList " + nodeInfo.toString())
            getPacketList.add(nodeInfo)
            sendHandlerMessage(msgGetPacket, delayedTime)
        } else {
            if (!isHasSameNodeInfo(nodeInfo, getPacketList)) {
                Logger.i(TAG, "addGetPacketList " + nodeInfo.toString())
                getPacketList.add(nodeInfo)
                sortGetPacketList()
                sendHandlerMessage(msgGetPacket, delayedTime)
            }
        }
    }

    /**
     * 添加打开红包，用于点击开打开红包
     */
    fun addOpenPacketList(nodeInfo: AccessibilityNodeInfo) {
        var delayedTime = Config.getDelayTime(false)
        if (delayedTime > 0) {
            delayedTime = delayedTime / 2
        }
        Logger.i(TAG, "addOpenPacketList delayedTime = $delayedTime")
        if (openPacketList.size == 0) {
            Logger.i(TAG, "addOpenPacketList " + nodeInfo.toString())
            openPacketList.add(nodeInfo)
            sendHandlerMessage(msgOpenPacket, delayedTime)
        } else {
            if (!isHasSameNodeInfo(nodeInfo, openPacketList)) {
                Logger.i(TAG, "addOpenPacketList $" + nodeInfo.toString())
                openPacketList.add(nodeInfo)
                sendHandlerMessage(msgOpenPacket, delayedTime)
            }
        }
    }

    private fun getRectFromNodeInfo(nodeInfo: AccessibilityNodeInfo): Rect {
        val rect = Rect()
        nodeInfo.getBoundsInScreen(rect)
        return rect
    }

    private fun isHasSameNodeInfo(nodeInfo: AccessibilityNodeInfo,
                                  nodeInfos: ArrayList<AccessibilityNodeInfo>): Boolean {
        var result = false
        for (i in nodeInfos.indices) {
            if (nodeInfo == nodeInfos[i]) {
                result = true
                break
            }
        }
        Logger.i(TAG, "isHasSameNodeInfo result = $result")
        return result
    }

    private fun sendHandlerMessage(what: Int, delayedTime: Int) {
        val msg = checkMsgHandler!!.obtainMessage()
        msg!!.what = what
        checkMsgHandler!!.sendMessageDelayed(msg, delayedTime.toLong())
    }

    private fun initBackThread() {
        if (checkMsgHandler != null) {
            return
        }
        checkMsgThread = HandlerThread("check-message-coming")
        checkMsgThread?.start()
        checkMsgHandler = object : Handler(checkMsgThread?.looper) {
            override fun handleMessage(msg: Message) {
                Logger.i(TAG, "handleMessage msg.what = " + msg.what)
                when (msg.what) {
                    //如果是聊天界面出现的红包item，则点击打开红包
                    msgGetPacket -> if (!getPacketList.isEmpty()) {
                        AccessibilityUtil.performClick(getPacketList.last())
                        getPacketList.removeAt(getPacketList.lastIndex)
                    }
                    //如果是红包界面，则模拟点击开
                    msgOpenPacket -> if (!openPacketList.isEmpty()) {
                        AccessibilityUtil.performClick(openPacketList.last())
                        openPacketList.removeAt(openPacketList.lastIndex)
                    }
                    //重新设置点击新消息列表
                    msgResetIsClickedNewMessageList -> Constants.isClickedNewMessageList = false
                    //重新设置获取红包
                    msgResetIsGotPacket -> Constants.isGotPacket = false
                    else -> {
                    }
                }
            }
        }
    }


    fun sendPacketRecord(packetRecord: PacketRecord?) {
        if (packetRecord == null) {
            return
        }
        val msg = checkMsgHandler!!.obtainMessage()
        msg!!.what = msgPacketRecord
        msg!!.obj = packetRecord
        checkMsgHandler!!.sendMessageDelayed(msg, 0)
    }

    fun resetIsClickedNewMessageList() {
        sendHandlerMessage(msgResetIsClickedNewMessageList, timeResetIsClickedNewMessageList)
    }

    fun resetIsGotPacket() {
        sendHandlerMessage(msgResetIsGotPacket, timeResetIsGotPacket)
    }

    private fun getBottomFromNodeInfo(nodeInfo: AccessibilityNodeInfo): Int {
        val rect = getRectFromNodeInfo(nodeInfo)
        Logger.i(TAG, "getBottomFromNodeInfo bottom = " + rect.bottom)
        return rect.bottom
    }

    /**
     * 排序列表
     */
    private fun sortGetPacketList() {
        if (getPacketList.size == 1) {
            return
        }
        val tempGetPacketList = ArrayList<AccessibilityNodeInfo>()
        val nodeInfosBottom = IntArray(getPacketList.size)
        val nodeInfosIndex = IntArray(getPacketList.size)
        for (i in getPacketList.indices) {
            nodeInfosBottom[i] = getBottomFromNodeInfo(getPacketList[i])
            nodeInfosIndex[i] = i
            tempGetPacketList.add(getPacketList[i])
        }
        getPacketList.clear()
        insertSort(nodeInfosBottom, nodeInfosIndex)
        for (i in tempGetPacketList.indices) {
            getPacketList.add(tempGetPacketList[nodeInfosIndex[i]])
            Logger.i(TAG, "sortGetPacketList nodeInfoBottom[" + i + "] = "
                    + getBottomFromNodeInfo(getPacketList[i]))
        }
    }

    private fun insertSort(a: IntArray, b: IntArray) {
        // nodeInofs的数量一般小于10，插入排序效率较高
        var i: Int
        var j: Int
        var insertNoteA: Int
        var insertNoteB: Int             // 要插入的数据
        i = 1
        while (i < a.size) {                // 从数组的第二个元素开始循环将数组中的元素插入
            insertNoteA = a[i]                         // 设置数组中的第2个元素为第一次循环要插入的数据
            insertNoteB = b[i]
            j = i - 1
            while (j >= 0 && insertNoteA < a[j]) {
                a[j + 1] = a[j]             // 如果要插入的元素小于第j个元素,就将第j个元素向后移动
                b[j + 1] = b[j]
                j--
            }
            a[j + 1] = insertNoteA          // 直到要插入的元素不小于第j个元素,将insertNote插入到数组中
            b[j + 1] = insertNoteB
            i++
        }
    }
}
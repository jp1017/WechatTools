package com.effective.android.wxrp

import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.accessibility.AccessibilityNodeInfo
import java.util.*

class EventScheduling {

    companion object {
        private const val TAG = "EventScheduling"
    }

    private val getPacketList: ArrayList<AccessibilityNodeInfo> = ArrayList()
    private val openPacketList: ArrayList<AccessibilityNodeInfo> = ArrayList()
    private val config = Config.getInstance()

    init {
        initBackThread()
    }

    private val timeResetSelfPacketStatus = 1000
    private val timeResetIsClickedNewMessageList = 500
    private val timeResetIsGotPacket = 500
    private val timeResetBackToMessageListStatus = 2000

    private val msgGetPacket = 0
    private val msgOpenPacket = 1
    private val msgResetSelfPacketStatus = 2
    private val msgResetIsClickedNewMessageList = 3
    private val msgResetIsGotPacket = 4
    private val msgResetBackToMessageListStatus = 5

    private var checkMsgThread: HandlerThread? = null
    private var checkMsgHandler: Handler? = null

    /**
     * 添加红包列表
     */
    fun addGetPacketList(nodeInfo: AccessibilityNodeInfo) {
        val delayedTime = config.getDelayedTime()
        Logger.i(TAG, "addGetPacketList delayedTime = $delayedTime")
        if (getPacketList.isEmpty()) {
            Logger.i(TAG, "addGetPacketList " + nodeInfo.toString())
            getPacketList.add(nodeInfo)
            sendHandlerMessage(msgGetPacket, calculateDelayedTime(delayedTime))
        } else {
            if (!isHasSameNodeInfo(nodeInfo, getPacketList)) {
                Logger.i(TAG, "addGetPacketList " + nodeInfo.toString())
                getPacketList.add(nodeInfo)
                sortGetPacketList()
                sendHandlerMessage(msgGetPacket, calculateDelayedTime(delayedTime))
            }
        }
    }

    /**
     * 添加打开红包列表
     */
    fun addOpenPacketList(nodeInfo: AccessibilityNodeInfo) {
        val delayedTime = config.getDelayedTime()
        Logger.i(TAG, "addOpenPacketList delayedTime = $delayedTime")
        if (openPacketList.size == 0) {
            Logger.i(TAG, "addOpenPacketList " + nodeInfo.toString())
            openPacketList.add(nodeInfo)
            sendHandlerMessage(msgOpenPacket, calculateDelayedTime(delayedTime))
        } else {
            if (!isHasSameNodeInfo(nodeInfo, openPacketList)) {
                Logger.i(TAG, "addOpenPacketList $" + nodeInfo.toString())
                openPacketList.add(nodeInfo)
                sendHandlerMessage(msgOpenPacket, calculateDelayedTime(delayedTime))
            }
        }
    }


    private fun removeLastGetPacketList() {
        Logger.i(TAG, "removeLastGetPacketList")
        if (isSafeToArrayList(getPacketList)) {
            getPacketList.removeAt(getLastIndex(getPacketList))
        }
    }

    private fun removeLastOpenPacketList() {
        Logger.i(TAG, "removeLastOpenPacketList")
        if (isSafeToArrayList(openPacketList)) {
            openPacketList.removeAt(getLastIndex(openPacketList))
        }
    }

    private fun isSafeToArrayList(nodeInfos: ArrayList<AccessibilityNodeInfo>): Boolean {
        return !nodeInfos.isEmpty()
    }

    private fun getLastIndex(nodeInfos: ArrayList<AccessibilityNodeInfo>): Int = nodeInfos.size - 1;

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
        val msg = checkMsgHandler?.obtainMessage()
        msg?.what = what
        checkMsgHandler?.sendMessageDelayed(msg, delayedTime.toLong())
    }

    private fun initBackThread() {
        if (checkMsgHandler != null) {
            return
        }
        checkMsgThread = HandlerThread("check-message-coming")
        checkMsgThread?.start()
        checkMsgHandler = object : Handler(checkMsgThread?.getLooper()) {
            override fun handleMessage(msg: Message) {
                Logger.i(TAG, "handleMessage msg.what = " + msg.what)
                when (msg.what) {
                    //如果是聊天界面出现的红包item，则点击打开红包
                    msgGetPacket -> if (isSafeToArrayList(getPacketList)) {
                        AccessibilityHelper.performClick(getPacketList?.get(getLastIndex(getPacketList)))
                        removeLastGetPacketList()
                    }
                    //如果是红包界面，则模拟点击开
                    msgOpenPacket -> if (isSafeToArrayList(openPacketList)) {
                        AccessibilityHelper.performClick(openPacketList?.get(getLastIndex(openPacketList)))
                        removeLastOpenPacketList()
                    }
                    //重新设置个人状态
                    msgResetSelfPacketStatus -> Constants.setCurrentSelfPacketStatusData(Constants.W_otherStatus)
                    //重新设置点击新消息列表
                    msgResetIsClickedNewMessageList -> Constants.isClickedNewMessageList = false
                    //重新设置获取红包
                    msgResetIsGotPacket -> Constants.isGotPacket = false
                    //重新设置返回列表界面
                    msgResetBackToMessageListStatus -> Constants.backtoMessageListStatus = Constants.backtoMessageListOther
                    else -> {
                    }
                }
            }
        }
    }

    private fun calculateDelayedTime(delayedTime: Int): Int {
        val time: Int
        val isUsedDelayed = config.getIsUsedDelayed()
        val isUsedRandomDelayed = config.getIsUsedRandomDelayed()
        if (isUsedDelayed) {
            time = delayedTime / 2         // 除以2，是因为分别在聊天界面和红包拆开界面分别延时
        } else if (isUsedRandomDelayed) {
            time = generateRandomNum(delayedTime) / 2
        } else {
            // 不延时
            time = 0
        }
        Logger.i(TAG, "calculateDelayedTime time = $time")
        return time
    }

    private fun generateRandomNum(n: Int): Int {
        val rand = Random()
        return rand.nextInt(n + 1)
    }

    fun resetSelfPacketStatus() {
        sendHandlerMessage(msgResetSelfPacketStatus, timeResetSelfPacketStatus)
    }

    fun resetIsClickedNewMessageList() {
        sendHandlerMessage(msgResetIsClickedNewMessageList, timeResetIsClickedNewMessageList)
    }

    fun resetIsGotPacket() {
        sendHandlerMessage(msgResetIsGotPacket, timeResetIsGotPacket)
    }

    fun resetBacktoMessageListStatus() {
        sendHandlerMessage(msgResetBackToMessageListStatus, timeResetBackToMessageListStatus)
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


class HighSpeedMode {

    companion object {
        private val TAG = "HiggSpeedMode"
        private var isGotPacket = false
    }

    private val eventScheduling = EventScheduling()
    private var config = Config.getInstance()


    fun dealWindowStateChanged(className: String, rootNode: AccessibilityNodeInfo) {
        Logger.i(TAG, "dealWindowStateChanged")
        when (className) {

            //如果当前是聊天窗口
            Constants.CLASS_LAUNCHER -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前在首页")
                // 聊天页面，则返回列表
                if (Constants.backtoMessageListStatus == Constants.backtoMessageListReceiveUI) {
                    AccessibilityHelper.performBack(WQAccessibilityService.getService())
                    Constants.backtoMessageListStatus = Constants.backtoMessageListChatDialog
                    return
                    //如果当前是列表对话框，则不处理
                } else if (Constants.backtoMessageListStatus == Constants.backtoMessageListChatDialog) {
                    return
                }

                //如果当前是允许获取自己的红包且已经是打开支付状态
                if (config!!.getIsGotPacketSelf() && Constants.currentSelfPacketStatus == Constants.W_openedPayStatus) {
                    Constants.setCurrentSelfPacketStatusData(Constants.W_intoChatDialogStatus)
                    getPacket(rootNode, true)
                } else {
                    getPacket(rootNode, false)
                }
            }

            //红包页面
            Constants.CLASS_PACKET_RECEIVE -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前已打开红包")
                if (config!!.getIsGotPacketSelf() && Constants.currentSelfPacketStatus == Constants.W_intoChatDialogStatus) {
                    if (openPacket(rootNode)) {
                        Constants.setCurrentSelfPacketStatusData(Constants.W_gotSelfPacketStatus)
                    }
                } else {
                    if (openPacket(rootNode)) {
                        isGotPacket = true
                    }
                }
                Constants.isClickedNewMessageList = false
                Constants.isGotPacket = false
            }

            //红包发送页面
            Constants.CLASS_PACKET_SEND -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前在红包发送页面")
                if (Constants.currentSelfPacketStatus <= Constants.W_otherStatus) {
                    Constants.setCurrentSelfPacketStatusData(Constants.W_openedPacketSendStatus)
                }
            }


            //红包支付页面
            Constants.CLASS_PACKET_PAY -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前在红包支付页面")
                if (Constants.currentSelfPacketStatus == Constants.W_openedPacketSendStatus) {
                    Constants.setCurrentSelfPacketStatusData(Constants.W_openedPayStatus)
                }
            }


            //红包详情页
            Constants.CLASS_PACKET_DETAIL -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前在红包详情页")
                if (Constants.currentSelfPacketStatus != Constants.W_otherStatus) {
                    AccessibilityHelper.performBack(WQAccessibilityService.getService())
                    Constants.setCurrentSelfPacketStatusData(Constants.W_otherStatus)
                }
                if (isGotPacket) {
                    AccessibilityHelper.performBack(WQAccessibilityService.getService())
                    isGotPacket = false
                }
            }

        }
    }

    fun dealWindowContentChanged(className: String, rootNode: AccessibilityNodeInfo) {
        Logger.i(TAG, "dealWindowContentChanged")

        //如果当前是列表对话框，则判断当前节点是否存在红包
        if (Constants.backtoMessageListStatus == Constants.backtoMessageListChatDialog) {
            //点击进入详情页页面
            if (clickMessage(rootNode)) {
                Constants.backtoMessageListStatus = Constants.backtoMessageListOther
            }
            return

        } else if (Constants.backtoMessageListStatus >= Constants.backtoMessageListReceiveUI) {
            return
        }

        //会话列表，点击新消息进入详情页面
        if (clickNewMessage(rootNode)) {
            Constants.isClickedNewMessageList = true
            eventScheduling.resetIsClickedNewMessageList()
            return
        }

        //聊天页面获取红包列表
        if (getPacket(rootNode, false)) {
            Constants.isGotPacket = true
            eventScheduling.resetIsGotPacket()
            return
        }
    }


    /**
     * 获取红包列表
     */
    private fun getPacket(rootNote: AccessibilityNodeInfo?, isSelfPacket: Boolean): Boolean {
        Logger.i(TAG, "getPacket")
        if (rootNote == null) {
            Logger.i(TAG, "getPacket rootNode == null")
            return false
        }
        var result = false
        var packetList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_ITEM)
        if (!packetList.isEmpty()) {
            for (i in packetList.indices.reversed()) {
                //如果存在红包
                val packetList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET)
                if (!packetList.isEmpty()) {
                    if (!isSelfPacket) {
                        val packetTextList = packetList[i].findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET_MESSAGE)
                        if (!packetTextList.isEmpty()) {
                            val blackKeys = config!!.getPacketKeyWords()
                            val packetText = packetTextList[0].text.toString()
                            if (config!!.getIsUsedKeyWords() && isContainKeyWords(blackKeys, packetText)) {
                                Logger.i(TAG, "getPacket ： 开启关键词过滤（$blackKeys), 当前红包包含（$packetText),已过滤")
                                result = false || result
                            } else {
                                Logger.i(TAG, "getPacket ： 未开启关键词过滤, 默认支持打开")
                                eventScheduling.addGetPacketList(packetList[i])
                                result = true || result
                            }
                        }
                    } else {
                        val packetList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_AVATAR)
                        if (!packetList.isEmpty() && packetList[0].text != "yummylau头像") {
                            val packetTextList = packetList[i].findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET_MESSAGE)
                            if (!packetTextList.isEmpty()) {
                                val blackKeys = config!!.getPacketKeyWords()
                                val packetText = packetTextList[0].text.toString()
                                if (config!!.getIsUsedKeyWords() && isContainKeyWords(blackKeys, packetText)) {
                                    Logger.i(TAG, "getPacket ： 开启关键词过滤（$blackKeys), 当前红包包含（$packetText),已过滤")
                                    result = false || result
                                } else {
                                    Logger.i(TAG, "getPacket ： 未开启关键词过滤, 默认支持打开")
                                    eventScheduling.addGetPacketList(packetList[i])
                                    result = true || result
                                }
                            }
                        }
                    }

                }
            }

        }
        if (!packetList.isEmpty()) {
            Logger.i(TAG, "getPacket ： 找到红包（" + packetList.size + "个）")
            for (i in packetList.indices.reversed()) {
                val packetTextList = packetList[i].findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET_MESSAGE)
                if (!packetTextList.isEmpty()) {
                    val blackKeys = config!!.getPacketKeyWords()
                    val packetText = packetTextList[0].text.toString()
                    if (config!!.getIsUsedKeyWords() && isContainKeyWords(blackKeys, packetText)) {
                        Logger.i(TAG, "getPacket ： 开启关键词过滤（$blackKeys), 当前红包包含（$packetText),已过滤")
                        result = false || result
                    } else {
                        Logger.i(TAG, "getPacket ： 未开启关键词过滤, 默认支持打开")
                        eventScheduling.addGetPacketList(packetList[i])
                        result = true || result
                    }
                }
            }
        }
        Logger.i(TAG, "getPacket result = $result")
        return result
    }


    /**
     * 打开红包，当前已经显示了一个红包窗口
     */
    private fun openPacket(rootNode: AccessibilityNodeInfo?): Boolean {
        Logger.i(TAG, "openPacket")
        if (rootNode == null && Constants.backtoMessageListStatus == Constants.backtoMessageListOther) {
            AccessibilityHelper.performBack(WQAccessibilityService.getService())
            Constants.backtoMessageListStatus = Constants.backtoMessageListReceiveUI
            Logger.w(TAG, "openPacket == null")
            eventScheduling.resetBacktoMessageListStatus()
            return false
        } else if (Constants.backtoMessageListStatus >= Constants.backtoMessageListReceiveUI) {
            return false
        }
        var result = false
        val packetList = rootNode!!.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_PACKET_DIALOG_BUTTON)
        if (!packetList.isEmpty()) {
            val item = packetList[0]
            if (item.isClickable) {
                eventScheduling.addOpenPacketList(item)
                result = true
            }
        }
        Logger.i(TAG, "openPacket result = $result")
        return result
    }

    /**
     * 是否包含某些关键字
     */
    private fun isContainKeyWords(keyWords: String, content: String): Boolean {
        var result = false
        val arrayListKeyWords = keyWords.split("、".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (keyWord in arrayListKeyWords) {
            Logger.i(TAG, "isContainKeyWords keyWord = $keyWord")
            if (content.contains(keyWord)) {
                result = true
                break
            }
        }
        Logger.i(TAG, "isContainKeyWords result = $result")
        return result
    }


    /**
     * 点击消息列表
     */
    private fun clickNewMessage(nodeInfo: AccessibilityNodeInfo?): Boolean {
        Logger.i(TAG, "clickNewMessage")
        if (nodeInfo == null) {
            Logger.i(TAG, "clickNewMessage ： 点击消息为 null")
            return false
        }
        var result = false
        val itemList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_ITEM)
        if (!itemList.isEmpty()) {
            for (item in itemList) {
                val newMessageTextList = item.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_MESSAGE_TEXT)
                if (!newMessageTextList.isEmpty()) {
                    if (AccessibilityHelper.isRedPacketItem(newMessageTextList[0])) {
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        result = true
                    }
                }
            }
        }
        Logger.i(TAG, "clickNewMessage ： 是否模拟点击进入聊天页面（$result)");
        return result
    }

    /**
     * 点击消息
     */
    private fun clickMessage(nodeInfo: AccessibilityNodeInfo): Boolean {
        Logger.i(TAG, "clickMessage")
        if (nodeInfo == null) {
            Logger.i(TAG, "clickMessage ： 点击消息为 null")
            return false
        }
        var result = false
        val dialogList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_ITEM)
        if (!dialogList.isEmpty()) {
            for (item in dialogList) {
                val messageTextList = item.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_MESSAGE_TEXT)
                if (!messageTextList.isEmpty()) {
                    if (AccessibilityHelper.isRedPacketItem(messageTextList[0])) {
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        result = true
                    }
                }
            }
        }
        Logger.i(TAG, "clickMessage ： 是否模拟点击进入聊天页面（$result)");
        return result
    }
}

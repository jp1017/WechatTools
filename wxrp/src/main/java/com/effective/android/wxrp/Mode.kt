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
    private val config = Config.getConfig(WQAccessibilityService.getService())

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
                    msgGetPacket -> if (isSafeToArrayList(getPacketList)) {
                        AccessibilityHelper.performClick(getPacketList?.get(getLastIndex(getPacketList)))
                        removeLastGetPacketList()
                    }
                    msgOpenPacket -> if (isSafeToArrayList(openPacketList)) {
                        AccessibilityHelper.performClick(openPacketList?.get(getLastIndex(openPacketList)))
                        removeLastOpenPacketList()
                    }
                    msgResetSelfPacketStatus -> Constants.setCurrentSelfPacketStatusData(Constants.W_otherStatus)
                    msgResetIsClickedNewMessageList -> Constants.isClickedNewMessageList = false
                    msgResetIsGotPacket -> Constants.isGotPacket = false
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

//interface Mode {
//    fun dealWindowStateChanged(className: String, rootNode: AccessibilityNodeInfo)
//    fun dealWindowContentChanged(className: String, rootNode: AccessibilityNodeInfo)
//    fun openPacket(rootNode: AccessibilityNodeInfo)
//    fun getPacket(rootNode: AccessibilityNodeInfo, isSelfPacket: Boolean)
//}

class HighSpeedMode constructor() {

    companion object {
        private val TAG = "HigeSpeedMode"
        private var isGotPacket = false
    }

    private val eventScheduling = EventScheduling()
    private var config = Config.getConfig(WQAccessibilityService.getService())

    init {
        WQ.initWQ(WQAccessibilityService.getService())
    }


    fun dealWindowStateChanged(className: String, rootNode: AccessibilityNodeInfo) {
        Logger.i(TAG, "dealWindowStateChanged")
        if (className == Constants.WCN_LAUNCHER) {
            // 聊天页面
            if (Constants.backtoMessageListStatus == Constants.backtoMessageListReceiveUI) {
                AccessibilityHelper.performBack(WQAccessibilityService.getService())
                Constants.backtoMessageListStatus = Constants.backtoMessageListChatDialog
                return
            } else if (Constants.backtoMessageListStatus == Constants.backtoMessageListChatDialog) {
                return
            }
            if (config!!.getIsGotPacketSelf() && Constants.currentSelfPacketStatus == Constants.W_openedPayStatus) {
                Constants.setCurrentSelfPacketStatusData(Constants.W_intoChatDialogStatus)
                getPacket(rootNode, true)
            } else {
                getPacket(rootNode, false)
            }
        } else if (className == Constants.WCN_PACKET_RECEIVE) {
            // 打开红包
            Logger.i(TAG, "dealWindowStateChanged 打开红包页面")
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
        } else if (className == Constants.WCN_PACKET_SEND) {
            if (Constants.currentSelfPacketStatus <= Constants.W_otherStatus) {
                Constants.setCurrentSelfPacketStatusData(Constants.W_openedPacketSendStatus)
            }
        } else if (className == Constants.WCN_PACKET_PAY) {
            if (Constants.currentSelfPacketStatus == Constants.W_openedPacketSendStatus) {
                Constants.setCurrentSelfPacketStatusData(Constants.W_openedPayStatus)
            }
        } else if (className == Constants.WCN_PACKET_DETAIL) {
            Logger.i(TAG, "dealWindowStateChanged 红包详情页面")
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

    fun dealWindowContentChanged(className: String, rootNode: AccessibilityNodeInfo) {
        Logger.i(TAG, "dealWindowContentChanged")
        if (Constants.backtoMessageListStatus == Constants.backtoMessageListChatDialog) {
            if (clickMessage(rootNode)) {
                Constants.backtoMessageListStatus = Constants.backtoMessageListOther
            }
            return
        } else if (Constants.backtoMessageListStatus >= Constants.backtoMessageListReceiveUI) {
            return
        }

        if (clickNewMessage(rootNode)) {
            Constants.isClickedNewMessageList = true
            eventScheduling.resetIsClickedNewMessageList()
            return
        }
        if (getPacket(rootNode, false)) {
            Constants.isGotPacket = true
            eventScheduling.resetIsGotPacket()
            return
        }

    }

    private fun getPacket(rootNote: AccessibilityNodeInfo?, isSelfPacket: Boolean): Boolean {
        if (rootNote == null) {
            Logger.i(TAG, "getPacket rootNode == null")
            return false
        }
        var result = false
        val packetList = rootNote.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_DIALOG_PACKET)
        if (!packetList.isEmpty()) {
            for (i in packetList.indices.reversed()) {
                val packetTextList = packetList[i].findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_DIALOG_PACKET_TEXT)
                if (!packetTextList.isEmpty()) {
                    if (isSelfPacket) {
                        if (packetTextList[0].text.toString().contains(Constants.WT_SEE_PACKET)) {
                            if (config!!.getIsUsedKeyWords()) {
                                result = isInKeyWordsMode(packetList, i)
                            } else {
                                // packetList.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                eventScheduling.addGetPacketList(packetList[i])
                                result = true
                            }
                        }
                    } else {
                        if (packetTextList[0].text.toString().contains(Constants.WT_GET_PACKET)) {
                            if (config!!.getIsUsedKeyWords()) {
                                result = isInKeyWordsMode(packetList, i)
                            } else {
                                // packetList.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                eventScheduling.addGetPacketList(packetList[i])
                                result = true
                            }
                        }
                    }
                }
            }
        }
        Logger.i(TAG, "getPacket result = $result")
        return result
    }


    /**
     * 打开红包
     */
    private fun openPacket(rootNode: AccessibilityNodeInfo?): Boolean {
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
        val packetList = rootNode!!.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_PACKET_DIALOG_BUTTON)
        if (!packetList.isEmpty()) {
            val item = packetList[0]
            if (item.isClickable) {
                // item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                eventScheduling.addOpenPacketList(item)
                result = true
            }
        }
        Logger.i(TAG, "openPacket result = $result")
        return result
    }

    /**
     * 查看节点是否包含某些关键字
     */
    private fun isInKeyWordsMode(nodeInfos: List<AccessibilityNodeInfo>, index: Int): Boolean {
        val packetContentList = nodeInfos[index]
                .findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_DIALOG_PACKET_CONTENT)
        if (!packetContentList.isEmpty()) {
            if (!isContainKeyWords(config!!.getPacketKeyWords(),
                            packetContentList[0].text.toString())) {
                // packetList.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                eventScheduling.addGetPacketList(nodeInfos[index])
                return true
            }
        }
        return false
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
     * 点击新消息列表
     */
    private fun clickNewMessage(nodeInfo: AccessibilityNodeInfo?): Boolean {
        Logger.i(TAG, "clickNewMessage")
        if (nodeInfo == null) {
            Logger.i(TAG, "clickNewMessage nodeInfo == null")
            return false
        }
        var result = false
        val dialogList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_LIST_DIALOG)
        if (!dialogList.isEmpty()) {
            for (item in dialogList) {
                val newMessageList = item.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_LIST_MESSAGE_NUM)
                newMessageList.addAll(item.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_LIST_MESSAGE_POT))
                if (!newMessageList.isEmpty()) {
                    val newMessageTextList = item.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_LIST_MESSAGE_TEXT)
                    if (!newMessageTextList.isEmpty()) {
                        if (newMessageTextList[0].text.toString().contains(Constants.WT_PACKET)) {
                            item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            result = true
                        }
                    }
                }
            }
        }
        Logger.i(TAG, "clickNewMessage result = $result")
        return result
    }

    /**
     * 点击消息
     */
    private fun clickMessage(nodeInfo: AccessibilityNodeInfo): Boolean {
        Logger.i(TAG, "clickMessage")
        if (nodeInfo == null) {
            Logger.i(TAG, "clickMessage nodeInfo == null")
            return false
        }
        var result = false
        val dialogList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_LIST_DIALOG)
        if (!dialogList.isEmpty()) {
            for (item in dialogList) {
                val messageTextList = item.findAccessibilityNodeInfosByViewId(Constants.WID_CHAT_LIST_MESSAGE_TEXT)
                if (!messageTextList.isEmpty()) {
                    if (messageTextList[0].text.toString().contains(Constants.WT_PACKET)) {
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        result = true
                    }
                }
            }
        }
        Logger.i(TAG, "clickMessage result = $result")
        return result
    }
}

class CompatibleMode{

    private val config = Config.getConfig(WQAccessibilityService.getService())
    private val eventScheduling = EventScheduling()

    init {
        WQ.initWQ(WQAccessibilityService.getService())
    }

    companion object {
        private val TAG = "CompatibleMode"
        private var isGotPacket = false
    }

    fun dealWindowStateChanged(className: String, rootNode: AccessibilityNodeInfo) {
        if (className == Constants.WCN_LAUNCHER) {
            // 聊天页面
            if (config.getIsGotPacketSelf() && Constants.currentSelfPacketStatus == Constants.W_openedPayStatus) {
                Constants.setCurrentSelfPacketStatusData(Constants.W_intoChatDialogStatus)
                getPacket(rootNode, true)
            } else {
                getPacket(rootNode, false)
            }
        } else if (className == Constants.WCN_PACKET_RECEIVE) {
            // 打开红包
            if (config.getIsGotPacketSelf() && Constants.currentSelfPacketStatus == Constants.W_intoChatDialogStatus) {
                openPacket(rootNode)
                Constants.setCurrentSelfPacketStatusData(Constants.W_gotSelfPacketStatus)
            } else {
                if (openPacket(rootNode)) {
                    isGotPacket = true
                }
            }
            Constants.isGotPacket = false
        } else if (className == Constants.WCN_PACKET_SEND) {
            if (Constants.currentSelfPacketStatus <= Constants.W_otherStatus) {
                Constants.setCurrentSelfPacketStatusData(Constants.W_openedPacketSendStatus)
            }
        } else if (className == Constants.WCN_PACKET_PAY) {
            if (Constants.currentSelfPacketStatus == Constants.W_openedPacketSendStatus) {
                Constants.setCurrentSelfPacketStatusData(Constants.W_openedPayStatus)
            }
        } else if (className == Constants.WCN_PACKET_DETAIL) {
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

    fun dealWindowContentChanged(rootNode: AccessibilityNodeInfo) {
        Logger.i(TAG, "dealWindowContentChanged")
        if (getPacket(rootNode, false)) {
            Constants.isGotPacket = true
            eventScheduling.resetIsGotPacket()
            return
        }
    }

    private fun openPacket(rootNode: AccessibilityNodeInfo?): Boolean {
        var result = false
        if (rootNode != null) {
            Logger.i(TAG, "openPacket! " + rootNode.toString())
            val nodeInfos = rootNode
                    .findAccessibilityNodeInfosByText(Constants.WT_OPEN_SEND_A_PACKET)
            if (!nodeInfos.isEmpty()) {
                val parent = nodeInfos[0].parent
                for (i in 0 until parent.childCount) {
                    val nodeInfo = parent.getChild(i)
                    if (nodeInfo.className.toString() == Constants.WCN_PACKET_BUTTON) {
                        if (nodeInfo.isEnabled) {
                            eventScheduling.addOpenPacketList(nodeInfo)
                            result = true
                        }
                    }
                }
            }
        }
        Logger.i(TAG, "openPacket result =  $result")
        return result
    }

    private fun getPacket(rootNode: AccessibilityNodeInfo?, isSelfPacket: Boolean): Boolean {
        Logger.i(TAG, "getPacket")
        var result = false
        if (rootNode != null) {
            val nodeInfoList: List<AccessibilityNodeInfo>
            if (isSelfPacket) {
                nodeInfoList = rootNode.findAccessibilityNodeInfosByText(Constants.WT_SEE_PACKET)
            } else {
                nodeInfoList = rootNode.findAccessibilityNodeInfosByText(Constants.WT_GET_PACKET)
            }
            if (!nodeInfoList.isEmpty()) {
                for (i in nodeInfoList.indices.reversed()) {
                    // AccessibilityHelper.performClick(nodeInfoList.get(i));
                    eventScheduling.addGetPacketList(nodeInfoList[i])
                    result = true
                }
            }
        }
        return result
    }
}
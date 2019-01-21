package com.effective.android.wxrp.mode

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.effective.android.wxrp.Constants
import com.effective.android.wxrp.services.WXAccessibilityService
import com.effective.android.wxrp.store.Config
import com.effective.android.wxrp.utils.AccessibilityUtil
import com.effective.android.wxrp.utils.Logger

class PacketManager {

    companion object {
        private const val TAG = "PacketManager"
        private var isGotPacket = false
    }

    private val eventScheduling = EventScheduling()


    fun dealWindowStateChanged(className: String, rootNode: AccessibilityNodeInfo?) {
        Logger.i(TAG, "dealWindowStateChanged")
        if (rootNode == null) {
            Logger.i(TAG, "dealWindowStateChanged-rootNode : null")
            return
        }
        when (className) {
            //如果当前是聊天窗口
            Constants.CLASS_LAUNCHER -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前在首页")
                if (filterHomeTabPage(rootNode)) {
                    if (Config.isOpenGetSelfPacket() && Constants.currentSelfPacketStatus == Constants.W_openedPayStatus) {
                        Constants.setCurrentSelfPacketStatusData(Constants.W_intoChatDialogStatus)
                        getPacket(rootNode, true)
                    } else {
                        getPacket(rootNode, false)
                    }
                }
            }

            //红包页面
            Constants.CLASS_PACKET_RECEIVE -> {
                Logger.i(TAG, "dealWindowStateChanged className: 当前已打开红包")
                if (Config.isOpenGetSelfPacket() && Constants.currentSelfPacketStatus == Constants.W_intoChatDialogStatus) {
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
                    AccessibilityUtil.performBack(WXAccessibilityService.getService())
                    Constants.setCurrentSelfPacketStatusData(Constants.W_otherStatus)
                }
                if (isGotPacket) {
                    AccessibilityUtil.performBack(WXAccessibilityService.getService())
                    isGotPacket = false
                }
            }

        }
    }

    /**
     * 如果在聊天会话列表，则判断当前是否需要点击消息
     * 如果在聊天页面，则判断是否需要获取红包
     */
    fun dealWindowContentChanged(className: String, rootNode: AccessibilityNodeInfo?) {
        Logger.i(TAG, "dealWindowContentChanged")
        if (rootNode == null) {
            Logger.i(TAG, "dealWindowStateChanged-rootNode : null")
            return
        }

        if (openPacket(rootNode)) {
            isGotPacket = true
            Constants.isClickedNewMessageList = false
            Constants.isGotPacket = false
        }

        //如果不是首页，则默认不需要点击会话或者获取红包
        //只有首页第一个tab-微信才需要检测点击
        if (isClickableConversation(rootNode)) {
            if (clickConversation(rootNode)) {
                Constants.isClickedNewMessageList = true
                eventScheduling.resetIsClickedNewMessageList()
                return
            }
        }

        //只有聊天详情页才需要查询红包
        if (filterHomeTabPage(rootNode)) {
            if (getPacket(rootNode, false)) {
                Constants.isGotPacket = true
                eventScheduling.resetIsGotPacket()
                return
            }
        }
    }

    private fun filterHomeTabPage(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) {
            return false
        }
        val tabTitle = rootNode.findAccessibilityNodeInfosByViewId(Constants.ID_HOME_TITLE)               //会话item
        if (tabTitle.isEmpty()) {
            return true
        }
        return false
    }

    private fun isClickableConversation(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) {
            return false
        }
        val tabTitle = rootNode.findAccessibilityNodeInfosByViewId(Constants.ID_HOME_TITLE)               //会话item
        if (tabTitle.isEmpty()) {
            return false
        }
        val actionText = tabTitle[0].text
        val result = !TextUtils.isEmpty(actionText) && actionText.startsWith(Constants.TEXT_TAB_TITLE_WEIXIN)
        Logger.i(TAG, "isClickableConversation ： $result, 当前tab($actionText)")
        return result
    }


    private fun hasGotPacketTip(tipList: List<AccessibilityNodeInfo>, currentIndex: Int): Boolean {
        if (tipList.isEmpty() || tipList.size <= currentIndex) {
            return false
        }
        val actionText = tipList[currentIndex].text
        var result = !TextUtils.isEmpty(actionText) && !actionText.isEmpty()
        Logger.i(TAG, "hasGotPacketTip($currentIndex) ： $result, 当前红包提示($actionText)")
        return result
    }


    /**
     * 是否是群节点
     */
    private fun isGroupNode(pageTitle: List<AccessibilityNodeInfo>): Boolean {
        if (pageTitle.isEmpty()) {
            return false
        }
        val title = pageTitle[0].text.toString()
        val result = !TextUtils.isEmpty(title) && title.contains("(") && title.contains(")")
        Logger.i(TAG, "isGroupNode ： $result , 会话名称为$title")
        return result
    }

    /**
     * 适用于聊天会话，聊天对话
     */
    private fun isSelfNode(avatarList: List<AccessibilityNodeInfo>, currentIndex: Int): Boolean {
        if (Config.getUserWxName().isEmpty() || avatarList.isEmpty() || avatarList.size <= currentIndex) {
            return false
        }
        val contentDescription = avatarList[currentIndex].contentDescription
        val result = contentDescription.contains(Config.getUserWxName())
        Logger.i(TAG, "isSelfNode($currentIndex) ： $result , 节点名称为$contentDescription")
        return result
    }


    /**
     * 适用于聊天会话，聊天对话
     * 过滤特定节点，如果包含关键字的话
     */
    private fun handleKeyWords(nodes: List<AccessibilityNodeInfo>, currentIndex: Int): Boolean {
        if (nodes.isEmpty() || nodes.size <= currentIndex) {
            return false
        }
        val packetText = nodes[currentIndex].text.toString()
        val result = Config.isOpenFilterTag() && isContainKeyWords(Config.filterTags, packetText)
        Logger.i(TAG, "handleKeyWords($currentIndex)  ： $result  当前节点包含（$packetText)")
        return result
    }


    /**
     * 是否包含某些关键字
     */
    private fun isContainKeyWords(keyWords: List<String>, content: String): Boolean {
        var result = false
        keyWords.map {
            if (content.contains(it)) {
                result = true
            }
        }
        Logger.i(TAG, "isContainKeyWords result = $result")
        return result
    }

    private fun isRedPacketNode(messageList: List<AccessibilityNodeInfo>, currentIndex: Int): Boolean {
        if (messageList.isEmpty() || messageList.size <= currentIndex) {
            return false
        }
        val text = messageList[currentIndex].text.toString()
        val result = !TextUtils.isEmpty(text) && text.contains(Constants.TEXT_WX_PACKET)
        Logger.i(TAG, "isRedPacketNode result = $result text($text)")
        return result
    }


    /**
     * 点击会话
     */
    private fun clickConversation(nodeInfo: AccessibilityNodeInfo?): Boolean {
        Logger.i(TAG, "clickConversation")
        if (nodeInfo == null) {
            Logger.i(TAG, "clickConversation ： 点击消息为 null")
            return false
        }
        var result = false
        val dialogList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_ITEM)               //会话item
        val messageTextList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_MESSAGE_TEXT)  //会话内容
        val TitleList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_TITLE_TEXT)          //会话名字
        if (!dialogList.isEmpty()) {
            for (i in dialogList.indices.reversed()) {

                if (isRedPacketNode(messageTextList, i)) {

                    //是否需要过滤关键字
                    if (handleKeyWords(messageTextList, i)) {
                        continue
                    }

                    dialogList[i].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    result = true
                }
            }
        }
        Logger.i(TAG, "clickConversation ： 是否模拟点击进入聊天页面（$result)")
        return result
    }

    /**
     * 获取红包列表
     * 兼容是否抢自己的红包，兼容是否有关键字
     */
    private fun getPacket(rootNote: AccessibilityNodeInfo?, isSelfPacket: Boolean): Boolean {
        Logger.i(TAG, "getPacket")
        if (rootNote == null) {
            Logger.i(TAG, "getPacket rootNode == null")
            return false
        }
        var result = false
        val avatarList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_AVATAR)
        val tipList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET_TIP)
        val messageList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET_MESSAGE)
        val pageTitle = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PAGE_TITLE)
        val packetList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET)

        if (!packetList.isEmpty()) {
            for (i in packetList.indices.reversed()) {

                //如果是自己的节点
                //如果当前节点不是群聊，则过滤
                //如果当前节点不没有开启强群的红包，则过滤
                if (isSelfNode(avatarList, i) && (!isGroupNode(pageTitle) || !Config.isOpenGetSelfPacket())) {
                    continue
                }

                //过滤已经抢过的，已过期等等
                if (hasGotPacketTip(tipList, i)) {
                    continue
                }

                //过滤关键词
                if (handleKeyWords(messageList, i)) {
                    continue
                }

                eventScheduling.addGetPacketList(packetList[i])
                result = true
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

        //如果当前节点存在红包，则遍历寻找"开"
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
}
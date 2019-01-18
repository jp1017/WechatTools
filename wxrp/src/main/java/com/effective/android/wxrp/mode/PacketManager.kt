package com.effective.android.wxrp.mode

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
        when (className) {

            //如果当前是聊天窗口
            Constants.CLASS_LAUNCHER -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前在首页")
                // 红包接收页面，返回会聊天列表
//                if (Constants.backtoMessageListStatus == Constants.backtoMessageListReceiveUI) {
//                    AccessibilityUtil.performBack(WXAccessibilityService.getService())
//                    Constants.backtoMessageListStatus = Constants.backtoMessageListChatDialog
//                    return
//                   //如果是聊天对话框，则
//                } else if (Constants.backtoMessageListStatus == Constants.backtoMessageListChatDialog) {
//                    return
//                }

                if (Config.isOpenGetSelfPacket() && Constants.currentSelfPacketStatus == Constants.W_openedPayStatus) {
                    Constants.setCurrentSelfPacketStatusData(Constants.W_intoChatDialogStatus)
                    getPacket(rootNode, true)
                } else {
                    getPacket(rootNode, false)
                }
            }

            //红包页面
            Constants.CLASS_PACKET_RECEIVE -> {
                Logger.i(TAG, "dealWindowStateChanged : 当前已打开红包")
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

//        //如果当前是列表对话框，则判断当前节点是否存在红包
//        if (Constants.backtoMessageListStatus == Constants.backtoMessageListChatDialog) {
//            //点击进入详情页页面
//            if (clickMessage(rootNode)) {
//                Constants.backtoMessageListStatus = Constants.backtoMessageListOther
//            }
//            return
//
//        } else if (Constants.backtoMessageListStatus >= Constants.backtoMessageListReceiveUI) {
//            return
//        }

        //如果不是通过流程进入，则默认遍历点击新消息
        if (clickMessage(rootNode)) {
            Constants.isClickedNewMessageList = true
            eventScheduling.resetIsClickedNewMessageList()
            return
        }

        //此时已经进入红包界面，开始获取红包
        if (getPacket(rootNode, false)) {
            Constants.isGotPacket = true
            eventScheduling.resetIsGotPacket()
            return
        }
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
        val needGetSelf = Config.isOpenGetSelfPacket()
        val blackKeys = Config.filterTags
        val needUserWord = Config.isOpenFilterTag()
        val avatarList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_AVATAR)
        val tipList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET_TIP)
        val messageList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET_MESSAGE)
        val pageTitle = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PAGE_TITLE)
        //找到所有红包绩点
        val packetList = rootNote.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_DIALOG_PACKET)
        if (!packetList.isEmpty()) {
            for (i in packetList.indices.reversed()) {

                //是否先过滤自己
                if (avatarList.size > i) {
                    if (!avatarList.isEmpty() && !Config.getUserWxName().isEmpty() && avatarList[i].contentDescription.contains(Config.getUserWxName())) {
                        var isGroup = false
                        if (!pageTitle.isEmpty()) {
                            val title = pageTitle[0].text.toString()
                            if ((title.contains("(") && title.contains(")"))) {
                                Logger.i(TAG, "getPacket($i)  ： 当前会话为群聊, 昵称为$title")
                                isGroup = true
                            } else {
                                Logger.i(TAG, "getPacket($i)  ： 当前会话不是群聊, 昵称为$title")
                            }
                        }
                        if (!isGroup) {
                            Logger.i(TAG, "getPacket($i)  ： 单聊不抢自己发送的红包")
                            continue
                        }
                        if (!needGetSelf && isGroup) {
                            Logger.i(TAG, "getPacket($i)  ： 群聊但是没有开启抢自己的红包")
                            continue
                        }
                        continue
                    }
                }

                //过滤已经抢过的，已过期等等
                if (tipList.size > i) {
                    val actionText = tipList[i].text;
                    if (!tipList.isEmpty() && !actionText.isEmpty()) {
                        Logger.i(TAG, "getPacket($i) ： 当前红包已被操作过($actionText)，已过滤")
                        continue
                    }
                }

                //过滤关键词
                val packetText = messageList[i].text.toString()
                if (needUserWord && isContainKeyWords(blackKeys, packetText)) {
                    Logger.i(TAG, "getPacket($i)  ： 开启关键词过滤（$blackKeys), 当前红包包含（$packetText),已过滤")
                    continue
                }

                Logger.i(TAG, "getPacket($i)  ： 未开启关键词过滤, 默认支持打开")
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
//        if (rootNode == null && Constants.backtoMessageListStatus == Constants.backtoMessageListOther) {
//            AccessibilityUtil.performBack(WXAccessibilityService.getService())
//            Constants.backtoMessageListStatus = Constants.backtoMessageListReceiveUI
//            Logger.w(TAG, "openPacket == null")
//            eventScheduling.resetBacktoMessageListStatus()
//            return false
//        } else if (Constants.backtoMessageListStatus >= Constants.backtoMessageListReceiveUI) {
//            return false
//        }

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


    /**
     * 点击消息
     */
    private fun clickMessage(nodeInfo: AccessibilityNodeInfo?): Boolean {
        Logger.i(TAG, "clickMessage")
        if (nodeInfo == null) {
            Logger.i(TAG, "clickMessage ： 点击消息为 null")
            return false
        }
        var result = false
        val blackKeys = Config.filterTags
        val needUserWord = Config.isOpenFilterTag()
        val dialogList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_ITEM)               //会话item
        val messageTextList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_MESSAGE_TEXT)  //会话内容
        val TitleList = nodeInfo.findAccessibilityNodeInfosByViewId(Constants.ID_WID_CHAT_LIST_TITLE_TEXT)          //会话名字
        if (!dialogList.isEmpty()) {
            for (i in dialogList.indices.reversed()) {

                //todo 目前如果是自己发送的，无法过滤？？
//                if (TitleList.size > i) {
//                    if (!TitleList.isEmpty() && TitleList[i].text.contains(Config.getUserWxName())) {
//                        Logger.i(TAG, "clickMessage($i)  ： 会话是自己发送的红包, 已过滤")
//                        continue
//                    }
//                }

                //是否需要过滤关键字
                if (messageTextList.size > i) {
                    val messageTip = messageTextList[0].text.toString()
                    if (needUserWord && isContainKeyWords(blackKeys, messageTip)) {
                        Logger.i(TAG, "clickMessage($i) ： 会话红包存在过滤词($messageTip),已过滤")
                        continue
                    }
                }

                if (AccessibilityUtil.isRedPacketItem(messageTextList[0])) {
                    dialogList[i].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    result = true
                }
            }
        }
        Logger.i(TAG, "clickMessage ： 是否模拟点击进入聊天页面（$result)");
        return result
    }
}
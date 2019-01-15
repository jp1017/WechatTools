package com.effective.android.wxrp

import android.content.Context

open class Constants internal constructor() {
    companion object {

        // 本程序包名
        var SELF_PACKAGE_NAME = "com.effective.android.wxrp"
        // 微信包名
        var WECHAT_PACKAGE_NAME = "com.tencent.mm"
        // 微信
        var WECHAT = "微信"

        /*
            Class Name
        */
        // 通知监听类名
        var SELFCN_NOTIFICATION = "WQNotificationService"
        // 辅助服务类名
        var SELFCN_ACCESSBILITY = "WQAccessibilityService"

        // 微信 聊天列表、聊天窗口
        var WCN_LAUNCHER = "com.tencent.mm.ui.LauncherUI"
        // 微信 红包“開”的窗口
        var WCN_PACKET_RECEIVE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI"
        // 微信 自己发红包的窗口
        var WCN_PACKET_SEND = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI"
        // 微信 自己发红包输入密码的界面
        var WCN_PACKET_PAY = "com.tencent.mm.plugin.wallet_core.ui.l"
        // 微信 红包详情
        var WCN_PACKET_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"
        // 微信 红包详情
        var WCN_PACKET_BUTTON = "android.widget.Button"


        /*
            Resource-id
        */
        // 微信 聊天列表的联系人列表 ListView
        var WID_CHAT_LIST_LISTVIEW = "com.tencent.mm:id/cwp"

        // 微信 聊天列表的联系人里面的消息内容 不可点击
        var WID_CHAT_LIST_MESSAGE_TEXT = "com.tencent.mm:id/apv"

        // 微信 聊天列表的联系人 内括名字、消息内容、消息数字、头像  可点击
        var WID_CHAT_LIST_DIALOG = "com.tencent.mm:id/apr"

        // 微信 聊天列表的联系人里面的消息数字 不可点击
        var WID_CHAT_LIST_MESSAGE_NUM = "com.tencent.mm:id/iu"

        // 微信 聊天列表的联系人里面的消息点（即屏蔽的群有消息） 不可点击
        var WID_CHAT_LIST_MESSAGE_POT = "com.tencent.mm:id/aps"

        // 微信 红包详情的后退按钮
        var WID_PACKET_DETAIL_BACK_BUTTON = "com.tencent.mm:id/ho"


        // 微信 聊天窗口 ListView
        var WID_CHAT_DIALOG_LISTVIEW = "com.tencent.mm:id/a_h"
        // 微信 聊天窗口 收到红包 可点击
        var WID_CHAT_DIALOG_PACKET = "com.tencent.mm:id/ada"
        // 微信 聊天窗口 收到红包 红包的文字（领取红包/查看红包） 不可点击
        var WID_CHAT_DIALOG_PACKET_TEXT = "com.tencent.mm:id/aeb"
        // 微信 聊天窗口 收到红包 红包的文字内容 不可点击
        var WID_CHAT_DIALOG_PACKET_CONTENT = "com.tencent.mm:id/aea"
        // 微信 聊天窗口 收到红包 XX领取了XX的红包
        var WID_CHAT_DIALOG_HAD_OPEN_PACKET = "com.tencent.mm:id/j_"

        // 微信 聊天窗口 打开红包弹窗  “開”
        var WID_CHAT_PACKET_DIALOG_BUTTON = "com.tencent.mm:id/c2i"


        /*
            Resource text
        */
        var WT_PACKET = "[微信红包]"
        var WT_GET_PACKET = "领取红包"
        var WT_SEE_PACKET = "查看红包"
        var WT_GET_PACKET_SELF = "你领取了凡人的红包"
        var WT_OPEN_SEND_A_PACKET = "发了一个红包"


        var W_otherStatus = 0
        var W_openedPacketSendStatus = 1
        var W_openedPayStatus = 2
        var W_intoChatDialogStatus = 3
        var W_gotSelfPacketStatus = 4
        var currentSelfPacketStatus = W_otherStatus;

        var isPreviouslyLockScreen = false

        var isGotNotification = false
        var isClickedNewMessageList = false
        var isGotPacket = false

        var backtoMessageListOther = 0
        var backtoMessageListReceiveUI = 1
        var backtoMessageListChatDialog = 2
        var backtoMessageListStatus = backtoMessageListOther


        fun setCurrentSelfPacketStatusData(status: Int): Unit {
            currentSelfPacketStatus = status
        }
    }
}

class WQ : Constants() {

    companion object {

        private const val WeChatVersion_7_0_0 = "7.0.0"

        fun initWQ(context: Context) {
            when (Tools.getWeChatVersion(context)) {
                WeChatVersion_7_0_0 -> {
                    setWeChatVersion_7_0_0()
                }
            }
        }

        private fun setWeChatVersion_7_0_0() {
//            // 微信 聊天列表的联系人里面的消息内容 不可点击
//            WID_CHAT_LIST_MESSAGE_TEXT = "com.tencent.mm:id/apt"
//            // 微信 聊天列表的联系人 内括名字、消息内容、消息数字、头像 可点击
//            WID_CHAT_LIST_DIALOG = "com.tencent.mm:id/app"
//            // 微信 聊天列表的联系人里面的消息数字 不可点击
//            WID_CHAT_LIST_MESSAGE_NUM = "com.tencent.mm:id/j4"
//            // 微信 聊天列表的联系人里面的消息点（即屏蔽的群有消息） 不可点击
//            WID_CHAT_LIST_MESSAGE_POT = "com.tencent.mm:id/apq"
//            // 微信 聊天窗口 收到红包 可点击
//            WID_CHAT_DIALOG_PACKET = "com.tencent.mm:id/ad8"
//            // 微信 聊天窗口 收到红包 红包的文字（领取红包/查看红包） 不可点击
//            WID_CHAT_DIALOG_PACKET_TEXT = "com.tencent.mm:id/ae_"
//            // 微信 聊天窗口 收到红包 红包的文字内容 不可点击
//            WID_CHAT_DIALOG_PACKET_CONTENT = "com.tencent.mm:id/ae9"
//            // 微信 聊天窗口 打开红包弹窗 “開”
//            WID_CHAT_PACKET_DIALOG_BUTTON = "com.tencent.mm:id/c4j"
//
//            // 微信 自己发红包输入密码的界面
//            WCN_PACKET_PAY = "com.tencent.mm.plugin.wallet_core.ui.m"
        }
    }
}
package com.effective.android.wxrp.version


object VersionManager {

    val PACKAGE_SELF_APPLICATION = "com.effective.android.wxrp"           // 本程序包名
    val PACKAFEGE_WECHAT = "com.tencent.mm"                               // 微信包名
    val WECHAT = "微信"                                                   // 微信
    val WECHAT_7_0_0 = "7.0.0"
    val WECHAT_7_0_3 = "7.0.3"
    val CLASS_NOTIFICATION = "services.WXNotificationService"            // 通知监听类名
    val CLASS_ACCESSBILITY = "services.WXAccessibilityService"           // 辅助服务类名
    val TEXT_WX_PACKET = "[微信红包]"
    val TEXT_WX_PACKET_WITHOUT_SPL = "微信红包"
    val TEXT_TAB_TITLE_WEIXIN = "微信"
    val TEXT_TAB_TITLE_CHATLIST = "通讯录"
    val TEXT_TAB_TITLE_DISC = "发现"

    fun setWeChatVersionInfo(versionInfo: VersionInfo?) {
        VersionManager.versionInfo = versionInfo
    }

    fun runningPlus(): Boolean {
        return versionInfo != null
    }

    private var versionInfo: VersionInfo? = null


    fun launcherClass(): String = versionInfo!!.launcherClass()
    fun packetReceiveClass(): String = versionInfo!!.packetReceiveClass()
    fun packetSendClass(): String = versionInfo!!.packetSendClass()
    fun packetPayClass(): String = versionInfo!!.packetPayClass()
    fun packetDetailClass(): String = versionInfo!!.packetDetailClass()

    fun packetDialogOpenId(): String = versionInfo!!.packetDialogOpenId()
    fun packetDetailPostUserId(): String = versionInfo!!.packetDetailPostUserId()
    fun packetDetailPostNumId(): String = versionInfo!!.packetDetailPostNumId()
    fun chatPagerItemId(): String = versionInfo!!.chatPagerItemId()
    fun chatPagerItemAvatatId(): String = versionInfo!!.chatPagerItemAvatatId()
    fun chatPagerItemPacketId(): String = versionInfo!!.chatPagerItemPacketId()
    fun chatPagerItemPacketMessageId(): String = versionInfo!!.chatPagerItemPacketMessageId()
    fun chatPagerItemPacketFlagId(): String = versionInfo!!.chatPagerItemPacketFlagId()
    fun chatPagerItemPacketTipId(): String = versionInfo!!.chatPagerItemPacketTipId()
    fun chatPagerTitleId(): String = versionInfo!!.chatPagerTitleId()
    fun homeChatListItemId(): String = versionInfo!!.homeChatListItemId()
    fun homeChatListItemMessageId(): String = versionInfo!!.homeChatListItemMessageId()
    fun homeChatListItemTextId(): String = versionInfo!!.homeChatListItemTextId()
    fun homeUserPagerNickId(): String = versionInfo!!.homeUserPagerNickId()
    fun homeUserPagerWeChatNumId(): String = versionInfo!!.homeUserPagerWeChatNumId()
    fun homeTabTitleId(): String = versionInfo!!.homeTabTitleId()


    /**
     * 流程状态
     */
    var W_otherStatus = 0
    var W_openedPacketSendStatus = 1            //打开红包发送界面
    var W_openedPayStatus = 2                   //打开支付界面
    var W_intoChatDialogStatus = 3              //聊天对话框状态
    var W_gotSelfPacketStatus = 4               //获取自己的红包 （在聊天详情页中点击自己发送的红包）
    var currentSelfPacketStatus = W_otherStatus

    fun setCurrentSelfPacketStatusData(status: Int) {
        currentSelfPacketStatus = status
    }

    var isPreviouslyLockScreen = false          //是否已经锁屏
    var isGotNotification = false               //是否获取锁屏信息
    var isClickedNewMessageList = false         //是否点击新消息列表
    var isGotPacket = false                     //是否获取到红包，打开之后为false
}
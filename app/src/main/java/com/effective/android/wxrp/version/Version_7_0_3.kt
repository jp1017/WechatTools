package com.effective.android.wxrp.version

class Version_7_0_3 : VersionInfo {


    /**
     * 类名
     */
    val CLASS_LAUNCHER = "com.tencent.mm.ui.LauncherUI"         // 微信 聊天列表、聊天窗口（单聊私聊都是）
    var CLASS_PACKET_RECEIVE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI"           // 微信 红包“開”的窗口
    val CLASS_PACKET_SEND = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI"               // 微信 自己发红包的窗口
    val CLASS_PACKET_PAY = "com.tencent.mm.plugin.wallet_core.ui.l"                                 // 微信 自己发红包输入密码的界面
    val CLASS_PACKET_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"              // 微信 红包详情


    /**
     * 红包界面 id
     */
    val ID_PACKET_DIALOG_OPEN = "com.tencent.mm:id/cyf"       // 聊天页面 - 红包对话框 - 开

    /**
     * 红包详情页 id
     */
    val ID_PACKET_DETAIL_POST_USER = "com.tencent.mm:id/csu"       // 红包详情页 - 发送者昵称
    val ID_PACKET_DETAIL_POST_NUM = "com.tencent.mm:id/csy"           // 红包详情页 - 红包金额


    /**
     * 聊天页面 id
     */
    val ID_CHAT_PAGER_ITEM = "com.tencent.mm:id/aa"                //聊天界面 - 聊天列表 - 对话item
    val ID_CHAT_PAGER_ITEM_AVATAR = "com.tencent.mm:id/ns"              // 聊天页面 - 聊天列表 - 红包item - 头像
    val ID_CHAT_PAGER_ITEM_PACKET = "com.tencent.mm:id/aou"             // 聊天页面 - 聊天列表 - 红包item - 包括下面
    val ID_CHAT_PAGER_ITEM_PACKET_MESSAGE = "com.tencent.mm:id/aq5"     // 聊天页面 - 聊天列表 - 红包item - 祝福语比如说恭喜发财
    val ID_CHAT_PAGER_ITEM_PACKET_FLAG = "com.tencent.mm:id/aq7"        // 聊天页面 - 聊天列表 - 红包item - 底部微信红包
    val ID_CHAT_PAGER_ITEM_PACKET_TIP = "com.tencent.mm:id/aq6"         // 聊天页面 - 聊天列表 - 红包item - 红包状态比如说领取之后显示已领取
    val ID_CHAT_PAGER_TITLE = "com.tencent.mm:id/k3"          // 聊天页面 - 聊天列表 - 个人聊天则是昵称，群聊天则是群昵称

    /**
     * 首页-微信tab 聊天会话 id
     */
    val ID_HOME_CHAT_LIST_ITEM = "com.tencent.mm:id/b5m"                     // 首页列表 - 聊天会话 - item id
    val ID_HOME_CHAT_LIST_ITEM_MESSAGE = "com.tencent.mm:id/b5q"            // 微信列表 每一个item中的文本id
    val ID_WID_CHAT_LIST_TITLE_TEXT = "com.tencent.mm:id/b5o"            // 微信列表 每一个item中的会话名字

    /**
     * 首页-我tab
     */
    val ID_HOME_USER_PAGER_NICK = "com.tencent.mm:id/a63"                  // 我的页面，微信昵称
    val ID_HOME_USER_PAGER_WEICHATNUM = "com.tencent.mm:id/dag"            // 我的页面，微信号

    val ID_HOME_TAB_TITLE = "android:id/text1"              // 首页4个tab的标题，存在与微信tab，通讯录tab，发现tab，用于过滤没有必要的轮训


    override fun wechatVersion(): String =  VersionManager.WECHAT_7_0_3

    override fun launcherClass(): String = CLASS_LAUNCHER

    override fun packetReceiveClass(): String = CLASS_PACKET_RECEIVE

    override fun packetSendClass(): String = CLASS_PACKET_SEND

    override fun packetPayClass(): String = CLASS_PACKET_PAY

    override fun packetDetailClass(): String = CLASS_PACKET_DETAIL

    override fun packetDialogOpenId(): String = ID_PACKET_DIALOG_OPEN

    override fun packetDetailPostUserId(): String = ID_PACKET_DETAIL_POST_USER

    override fun packetDetailPostNumId(): String = ID_PACKET_DETAIL_POST_NUM

    override fun chatPagerItemId(): String = ID_CHAT_PAGER_ITEM

    override fun chatPagerItemAvatatId(): String = ID_CHAT_PAGER_ITEM_AVATAR

    override fun chatPagerItemPacketId(): String = ID_CHAT_PAGER_ITEM_PACKET

    override fun chatPagerItemPacketMessageId(): String = ID_CHAT_PAGER_ITEM_PACKET_MESSAGE

    override fun chatPagerItemPacketFlagId(): String = ID_CHAT_PAGER_ITEM_PACKET_FLAG

    override fun chatPagerItemPacketTipId(): String = ID_CHAT_PAGER_ITEM_PACKET_TIP

    override fun chatPagerTitleId(): String = ID_CHAT_PAGER_TITLE

    override fun homeChatListItemId(): String = ID_HOME_CHAT_LIST_ITEM

    override fun homeChatListItemMessageId(): String = ID_HOME_CHAT_LIST_ITEM_MESSAGE

    override fun homeChatListItemTextId(): String = ID_WID_CHAT_LIST_TITLE_TEXT

    override fun homeUserPagerNickId(): String = ID_HOME_USER_PAGER_NICK

    override fun homeUserPagerWeChatNumId(): String = ID_HOME_USER_PAGER_WEICHATNUM

    override fun homeTabTitleId(): String = ID_HOME_TAB_TITLE
}
package com.hsj.redpacket.base;

/**
 * @Company: ****科技有限公司
 * @Class: BaseInfo
 * @Description:
 * @Author: HSJ
 * @Version:
 * @Date: 2017/1/10 18:59
 * @E-mail: mr.ajun@foxmail.com
 */
public final class BaseInfo {

/****************************************开关变量****************************************************/

    /**
     * 抢完红包是否自动关闭详情界面的开关
     */
    public static boolean autoBackFlag = false;

    /**
     * QQ 辅助服务是否开启的标识
     */
    public static boolean qqAsHasOpened = false;

    /**
     * 微信辅助服务是否开启的标识
     */
    public static boolean wechatAsHasOpened = false;

    /**
     * 咻红包服务是否开启的标识
     */
    public static boolean xiuAsHasOpened = false;

/****************************************QQ变量****************************************************/

    /**
     * 红包消息的关键字(普通红包和口令红包获取都是这个关键字)
     */
    public static final String QQ_TEXT_KEY = "[QQ红包]";

    /**
     * 普通红包未拆开时的关键字
     */
    public static final String QQ_UNCLICK_TEXT_KEY1 = "点击拆开";

    /**
     * 红包 关键字(普通红包和口令红包)
     */
    public static final String QQ_UNCLICK_TEXT_KEY2 = "QQ红包";

    /**
     * 红包 关键字(自己发的口令红包)
     */
    public static final String QQ_UNCLICK_TEXT_KEY3 = "查看领取详情";

    /**
     * 普通个性化红包未拆开时的关键字1
     */
    public static final String SPECIAL_QQ_UNCLICK_TEXT_KEY1 = "查看详情";

    /**
     * 普通个性化红包未拆开时的关键字1
     */
    public static final String SPECIAL_QQ_UNCLICK_TEXT_KEY2 = "QQ红包个性版";

    /**
     * 口令红包未拆开时的关键字
     */
    public static final String PASSWORD_QQ_UNCLICK_TEXT_KEY = "口令红包";

    /**
     * 点击口令红包后的关键字输入按钮名称
     */
    public static final String INPUT_PASSWORD_BUTTON_KEY = "点击输入口令";

    /**
     * 聊天窗口唯一的发送按钮组件标识
     */
    public static final String GLOBAL_SEND_BUTTON_KEY = "android.widget.Button";

    /**
     * 全局发送按钮的关键字
     */
    public static final String GLOBAL_SENT_TEXT_KEY = "发送";

/****************************************WeChat变量****************************************************/

    /**
     * 红包消息的关键字(普通红包和口令红包获取都是这个关键字)
     */
    public static final String WECHAT_TEXT_KEY = "[微信红包]";

    /**
     * 普通红包未拆开时的关键字
     */
    public static final String WECHAT_UNCLICK_TEXT_KEY = "领取红包";

}

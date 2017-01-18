package com.hsj.redpacket.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.hsj.redpacket.base.StateEnum;
import com.hsj.redpacket.R;
import com.hsj.redpacket.base.BaseInfo;
import com.hsj.redpacket.ui.MainActivity;
import java.util.LinkedList;
import java.util.List;

/**
 * @Company: ****科技有限公司
 * @Class: QQService
 * @Description:
 * @Author: HSJ
 * @Version:
 * @Date: 2017/1/10 19:06
 * @E-mail: mr.ajun@foxmail.com
 */
public class QQService extends AccessibilityService {

    private final String TAG = "QQService";

    /**
     * 当前段
     */
    public String nowStage = StateEnum.fetched.name();

    /**
     * 全局的消息发送按钮
     */
    private AccessibilityNodeInfo sendNode;

    /**
     * 已经开过的个性化红包列表
     */
    private List<Integer> openedList = new LinkedList<Integer>();

    /**
     * 主要将服务注册为前台服务，常驻内存
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());

        Intent nfIntent = new Intent(this, MainActivity.class);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
               .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
               .setContentTitle("QQ红包服务")    // 设置下拉列表里的标题
               .setSmallIcon(R.mipmap.ic_launcher)    // 设置状态栏内的小图标
               .setContentText("抢到qq红包")            // 设置上下文内容
               .setWhen(System.currentTimeMillis());  // 设置该通知发生的时间

        Notification notification = builder.build();  // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        startForeground(1, notification);
    }

    @Override
    public void onInterrupt() {
        BaseInfo.qqAsHasOpened = false;
        Toast.makeText(this, "关闭QQ红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseInfo.qqAsHasOpened = false;
        Toast.makeText(this, "关闭QQ红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        BaseInfo.qqAsHasOpened = true;
        Toast.makeText(this, "开启QQ红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            // 如果是通知栏事件，则判断是不是红包通知，是则打开通知,进入有红包的聊天界面
            String text = String.valueOf(event.getText());
            if (!text.isEmpty() && text.contains(BaseInfo.QQ_TEXT_KEY)) {
                openNotification(event);
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // 如果是窗口状态变化事件(打开通知，或者切换到QQ,或者点开红包时触发)，则在当前窗体扫描红包
            processMessage(event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && "android.widget.AbsListView".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
            processMessage(event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && "android.widget.TextView".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
            // 监听其他群的红包通知
            processOthersMessage(event);
        }
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotification(AccessibilityEvent event) {
        if (event.getParcelableData() == null
                || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        // 将通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 识别所处UI，分类处理
     * Description: <br>
     *
     * @param event <br>
     * @author lei.qiang<br>
     * @taskId <br>
     */
    private void processMessage(AccessibilityEvent event) {
        if (nowStage.equalsIgnoreCase(StateEnum.opening.name())) {
            // 有正在拆的包，则将当前包加入队列
            return;
        } else {

            if ("com.tencent.mobileqq.activity.SplashActivity".equals(event
                    .getClassName())) {
                nowStage = StateEnum.fetching.name();
                // 聊天窗体，点开红包
                scanAndOpenEnvelope(event.getSource());
            } else if (BaseInfo.autoBackFlag
                    && "cooperation.qwallet.plugin.QWalletPluginProxyActivity".equals(event.getClassName())
                    && nowStage.equalsIgnoreCase(StateEnum.opened.name())) {
                // 允许自动返回且处于拆完红包阶段
                // 执行全局返回，意图是关闭红包详情，有可能误返回
                performGlobalAction(GLOBAL_ACTION_BACK);
            } else if ("android.widget.AbsListView".equals(event.getClassName())) {
                nowStage = StateEnum.fetching.name();
                // 没有正在拆的包，则拆开当前包
                scanAndOpenEnvelope(event.getSource());
            }
        }
    }

    /**
     * 处理其他联系人以及群的消息
     * Description: <br>
     *
     * @param event <br>
     * @author lei.qiang<br>
     * @taskId <br>
     */
    private void processOthersMessage(AccessibilityEvent event) {
        if (nowStage.equalsIgnoreCase(StateEnum.opening.name())) {
            // 有正在拆的包，则将当前包加入队列
            return;
        } else {
            nowStage = StateEnum.fetching.name();
            AccessibilityNodeInfo rootNode = event.getSource();
            if (null != rootNode) {
                List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(BaseInfo.QQ_TEXT_KEY);
                if (null != nodes && !nodes.isEmpty()) {
                    nodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    scanAndOpenEnvelope(getRootInActiveWindow());
                }
            }
        }
    }

    /**
     * 扫描且拆包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void scanAndOpenEnvelope(AccessibilityNodeInfo rootNode) {
        nowStage = StateEnum.opening.name();
        // 当前事件所属的信息节点
        if (rootNode == null) {
            return;
        }
        // 处理普通红包
        openGenEnvelope(rootNode);
        // 处理口令红包
        openPasswordEnvelope(rootNode);
        // 处理个性化红包
        openSpecialGenEnvelope(rootNode);

        nowStage = StateEnum.opened.name();
    }

    /**
     * 初始化全局唯一发送按钮用于口令红包的发送
     */
    private boolean initGlobalSendButton(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> sendBtnlist = rootNode
                .findAccessibilityNodeInfosByText(BaseInfo.GLOBAL_SENT_TEXT_KEY);
        if (null != sendBtnlist && !sendBtnlist.isEmpty()) {
            for (AccessibilityNodeInfo node : sendBtnlist) {
                if (node.getClassName().toString().equals(BaseInfo.GLOBAL_SEND_BUTTON_KEY)
                        && node.getText().toString().equals(BaseInfo.GLOBAL_SENT_TEXT_KEY)) {
                    sendNode = node;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 处理个性化红包，与普通红包以及口令红包规则不一样，个性化红包拆过后在界面表现的状态没有变化
     * 难点在于识别哪些是拆过的红包
     * 2016.1.30 个性化红包只拆新的，不拆老的
     * Description: <br>
     */
    private void openSpecialGenEnvelope(AccessibilityNodeInfo rootNode) {
        // 当前聊天窗口中的未拆开普通红包信息节点集合
        List<AccessibilityNodeInfo> specialGenlist = rootNode
                .findAccessibilityNodeInfosByText(BaseInfo.SPECIAL_QQ_UNCLICK_TEXT_KEY1);
        if (null != specialGenlist && !specialGenlist.isEmpty()) {
            specialGenlist.get(specialGenlist.size() - 1);
        }
        List<AccessibilityNodeInfo> realSpecialList = new LinkedList<AccessibilityNodeInfo>();
        for (AccessibilityNodeInfo node : specialGenlist) {
            if (null != node.getChild(0)
                    && (BaseInfo.SPECIAL_QQ_UNCLICK_TEXT_KEY2.equalsIgnoreCase(String.valueOf(node.getChild(0).getText()))
                    || BaseInfo.QQ_UNCLICK_TEXT_KEY3.equalsIgnoreCase(String.valueOf(node.getChild(0).getText())))) {
                realSpecialList.add(node);
            }
        }

        if (!realSpecialList.isEmpty()) {
            AccessibilityNodeInfo lastNode = realSpecialList.get(realSpecialList.size() - 1);
            if (openedList.isEmpty()) {
                // 如果开过的列表为空，则realSpecialList 列表红包全部打开
                for (AccessibilityNodeInfo node : realSpecialList) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    openedList.add(node.hashCode());
                }
            } else if (realSpecialList.size() == openedList.size()) {
                // 长度一样，比较最后一个，判断是有新红包
                if (lastNode.hashCode() == openedList.get(realSpecialList.size() - 1)) {
                    // 如果一样，则表示没有新红包出现
                    openedList.clear();
                    for (AccessibilityNodeInfo node : realSpecialList) {
                        openedList.add(node.hashCode());
                    }
                } else {
                    openedList.clear();
                    for (AccessibilityNodeInfo node : realSpecialList) {
                        openedList.add(node.hashCode());
                    }
                    // 如果不一样，则最后一个肯定是新红包，就开最后一个红包就好了,能适用大部分情况，毕竟没有人连续发4个红包(魅族4一屏最多4个红包)，而没有人抢
                    lastNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            } else {
                // 长度不一样，肯定是有新的红包出现，把老的顶出了屏幕外
                openedList.clear();
                for (AccessibilityNodeInfo node : realSpecialList) {
                    openedList.add(node.hashCode());
                }
                // 如果不一样，则最后一个肯定是新红包，就开最后一个红包就好了,能适用大部分情况，毕竟没有人连续发4个红包(魅族4一屏最多4个红包)，而没有人抢
                lastNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else {
            // 如果当前页面没有个性化红包，则将openedList清空
            openedList.clear();
        }
    }

    /**
     * 处理口令红包，口令红包与普通红包操作流程不一样，需要3次点击
     * Description: <br>
     *
     * @param rootNode <br>
     * @author lei.qiang<br>
     * @taskId <br>
     */
    private void openPasswordEnvelope(AccessibilityNodeInfo rootNode) {
        // 当前聊天窗口中的未拆开普通红包信息节点集合
        List<AccessibilityNodeInfo> pdlist = rootNode
                .findAccessibilityNodeInfosByText(BaseInfo.PASSWORD_QQ_UNCLICK_TEXT_KEY);
        for (AccessibilityNodeInfo node : pdlist) {
            AccessibilityNodeInfo parent = node.getParent();
            if (!node.isClickable()
                    && null != parent
                    && parent.getChildCount() == 3
                    && BaseInfo.PASSWORD_QQ_UNCLICK_TEXT_KEY.equalsIgnoreCase(String.valueOf(node.getText()))
                    && (BaseInfo.QQ_UNCLICK_TEXT_KEY2.equalsIgnoreCase(String.valueOf(parent.getChild(2).getText()))
                    || BaseInfo.QQ_UNCLICK_TEXT_KEY3.equalsIgnoreCase(String.valueOf(parent.getChild(2).getText())))) {
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                List<AccessibilityNodeInfo> inputlist = getRootInActiveWindow()
                        .findAccessibilityNodeInfosByText(BaseInfo.INPUT_PASSWORD_BUTTON_KEY);
                if (null != inputlist && !inputlist.isEmpty()) {
                    // 点击输入口令
                    inputlist.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    // 查找聊天界面中的发送按钮，应该是全局唯一
                    // 点击全局发送按钮
                    if (null != sendNode || initGlobalSendButton(rootNode)) {
                        sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        }
    }

    /**
     * 处理普通红包，只需要一次点击
     * Description: <br>
     *
     * @param rootNode <br>
     * @author lei.qiang<br>
     * @taskId <br>
     */
    private void openGenEnvelope(AccessibilityNodeInfo rootNode) {
        // 当前聊天窗口中的未拆开普通红包信息节点集合
        List<AccessibilityNodeInfo> genlist = rootNode
                .findAccessibilityNodeInfosByText(BaseInfo.QQ_UNCLICK_TEXT_KEY1);
        for (AccessibilityNodeInfo node : genlist) {
            AccessibilityNodeInfo parent = node.getParent();
            if (null != parent
                    && null != parent.getChild(2)
                    && BaseInfo.QQ_UNCLICK_TEXT_KEY2.equalsIgnoreCase(String.valueOf(parent.getChild(2).getText()))) {
                // 非textView的才能点击
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

}

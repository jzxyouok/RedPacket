package com.jun.redpacket.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.jun.redpacket.R;
import com.jun.redpacket.base.BaseInfo;
import com.jun.redpacket.base.StateEnum;
import com.jun.redpacket.ui.MainActivity;

import java.util.List;

/**
 * @Company: ****科技有限公司
 * @Class: WeChatService
 * @Description:
 * @Author: HSJ
 * @Version:
 * @Date: 2017/1/10 19:17
 * @E-mail: mr.ajun@foxmail.com
 */
public class WeChatService extends AccessibilityService {

    /**
     * 当前阶段
     */
    public String nowStage = StateEnum.fetched.name();

    /**
     * 最后一次拆开过的包
     */
    public int lastedOpenEnvelope;

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
                .setContentTitle("微信红包服务")    // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher)    // 设置状态栏内的小图标
                .setContentText("抢到微信红包")          // 设置上下文内容
                .setWhen(System.currentTimeMillis());  // 设置该通知发生的时间


        Notification notification = builder.build();  // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        startForeground(1, notification);
    }

    @Override
    public void onInterrupt() {
        BaseInfo.wechatAsHasOpened = false;
        Toast.makeText(this, getResources().getString(R.string.tips_wechat_envelope_accessibility_end), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseInfo.wechatAsHasOpened = false;
        Toast.makeText(this, getResources().getString(R.string.tips_wechat_envelope_accessibility_end), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        BaseInfo.wechatAsHasOpened = true;
        Toast.makeText(this, getResources().getString(R.string.tips_wechat_envelope_accessibility_start), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            // 如果是通知栏事件，则判断是不是红包通知，是则打开通知,进入有红包的聊天界面
            String text = String.valueOf(event.getText());
            if (!text.isEmpty() && text.contains(BaseInfo.WECHAT_TEXT_KEY)) {
                openNotification(event);
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // 如果是窗口状态变化事件(打开通知，或者切换到微信,或者点开红包时触发)，则在当前窗体扫描红包
            processMessage(event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            processMessage(event);
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
     */
    private void processMessage(AccessibilityEvent event) {
        if (nowStage.equalsIgnoreCase(StateEnum.opening.name())) {
            // 有正在拆的包，则将当前包加入队列
            return;
        } else {

        /*
         *  非拆红包阶段，进行下列处理
         *  1. 正在拆红包，则等待拆红包，不进行后续处理，优化性能
         *  2. 避免误返回
         */
            if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event
                    .getClassName())) {
                nowStage = StateEnum.fetching.name();
                // 点开微信红包后，如果是这个UI，则表示红包未领取
                openEnvelope();
            } else if (BaseInfo.autoBackFlag
                    && "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())
                    && nowStage.equalsIgnoreCase(StateEnum.opened.name())) {
                // 点开微信红包后，如果是这个UI，则表示已经领取了，跳转到了红包领取详情界面
                // 执行全局返回，意图是关闭红包详情，有可能误返回
                performGlobalAction(GLOBAL_ACTION_BACK);
            } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
                nowStage = StateEnum.fetching.name();
                // 处于聊天界面
                scanEnvelope();
            } else {
                nowStage = StateEnum.fetching.name();
                // 处于聊天界面
                scanEnvelope();
            }
        }
    }

    /**
     * 扫描到红包，然后点开(此点开没有真的拆开红包)
     * 为加快效率，只拆当前窗口最下面的包(最新的包)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void scanEnvelope() {
        nowStage = StateEnum.fetching.name();
        // 当前聊天窗口节点
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        if (rootNode == null) {
            return;
        }
        // 处理普通红包
        // 当前聊天窗口中的红包信息节点集合
        List<AccessibilityNodeInfo> genlist = rootNode
                .findAccessibilityNodeInfosByText(BaseInfo.WECHAT_UNCLICK_TEXT_KEY);
        if (null != genlist && !genlist.isEmpty()) {
            AccessibilityNodeInfo newEnv = genlist.get(0);
            if (null != newEnv.getParent()) {
                Log.v("Temp", "parent hashCode : " + newEnv.getParent().hashCode());
            }
            if (!newEnv.isClickable()
                    && lastedOpenEnvelope != newEnv.getParent().hashCode()) {
                newEnv.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                lastedOpenEnvelope = newEnv.getParent().hashCode();
                Log.v("Temp", "lasted hashCode : " + lastedOpenEnvelope);
            }
        }
        nowStage = StateEnum.fetched.name();
    }

    /**
     * 真正的拆开红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openEnvelope() {
        nowStage = StateEnum.opening.name();
        // 当前聊天窗口节点
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        if (rootNode == null) {
            return;
        }
        // 处理普通红包
        // 当前聊天窗口中的未拆开普通红包信息节点集合
        AccessibilityNodeInfo buttonNode = rootNode.getChild(3);
        if ("android.widget.Button".equalsIgnoreCase(String.valueOf(buttonNode.getClassName()))) {
            // 非textView的才能点击
            buttonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        nowStage = StateEnum.opened.name();
    }

}

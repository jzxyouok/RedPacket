package com.jun.redpacket.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.jun.redpacket.R;
import com.jun.redpacket.base.BaseInfo;
import com.jun.redpacket.base.StateEnum;
import com.jun.redpacket.ui.MainActivity;

/**
 * @Company: ****科技有限公司
 * @Class: AliPayService
 * @Description:
 * @Author: HSJ
 * @Version:
 * @Date: 2017/1/10 19:44
 * @E-mail: mr.ajun@foxmail.com
 */
public class AliPayService extends AccessibilityService {

    /**
     * 当前阶段
     */
    public String nowStage = StateEnum.fetched.name();

    /**
     * 全局的消息发送按钮
     */
    AccessibilityNodeInfo sendNode;

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
                .setContentTitle("支付宝红包服务")        // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher)     // 设置状态栏内的小图标
                .setContentText("抢到支付宝红包")         // 设置上下文内容
                .setWhen(System.currentTimeMillis());   // 设置该通知发生的时间


        Notification notification = builder.build();        // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        startForeground(1, notification);
    }

    @Override
    public void onInterrupt() {
        BaseInfo.xiuAsHasOpened = false;
        Toast.makeText(this, "关闭AliPay红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseInfo.xiuAsHasOpened = false;
        Toast.makeText(this, getResources().getString(R.string.tips_alipay_xiu_envelope_accessibility_end), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        BaseInfo.xiuAsHasOpened = true;
        Toast.makeText(this, getResources().getString(R.string.tips_alipay_xiu_envelope_accessibility_start), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && "com.alipay.android.wallet.newyear.activity.MonkeyYearActivity".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
            // 如果窗口切换到咻红包界面
            nowStage = StateEnum.fetched.name();
            cycleXiu(event);
        }
        else  if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && "android.widget.TextView".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
            cycleXiu(event);
        }
        else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && "android.app.Dialog".equalsIgnoreCase(String.valueOf(event.getClassName()))) {

            nowStage = StateEnum.fetched.name();

            openEnvelope();
        }
    }

    /**
     * 领取红包
     */
    private void openEnvelope() {

        if (StateEnum.fetched.name().equalsIgnoreCase(nowStage)) {
            final AccessibilityNodeInfo parent = getRootInActiveWindow();
            if (null != parent && parent.getChildCount() > 0) {
                for (int j = 0; j < parent.getChildCount(); j++) {
                    Log.v("package", "noed : " + parent.getChild(j));
                }
                final int lastNodeIndex = parent.getChildCount() - 1;
                if ("android.widget.button".equalsIgnoreCase(String.valueOf(parent.getChild(lastNodeIndex).getClassName()))) {
                    nowStage = StateEnum.opening.name();
                    parent.getChild(lastNodeIndex).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    nowStage = StateEnum.opened.name();
                    if (BaseInfo.autoBackFlag) {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        nowStage = StateEnum.fetched.name();
                    }
                }
            }
            else {
                nowStage = StateEnum.fetched.name();
            }
        }
    }

    /**
     * 用线程循环咻红包
     */
    private void cycleXiu(AccessibilityEvent event) {
        if (StateEnum.fetched.name().equalsIgnoreCase(nowStage)) {
            final AccessibilityNodeInfo parent = getRootInActiveWindow();
            if (null != parent
                    && parent.getChildCount() > 0) {
                for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                    final AccessibilityNodeInfo temNode = parent.getChild(i);
                    if ("android.widget.button".equalsIgnoreCase(String.valueOf(temNode.getClassName()))
                            && temNode.getChildCount() == 0 && TextUtils.isEmpty(temNode.getText())) {
                        nowStage = StateEnum.fetching.name();
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                do {
                                    temNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    try {
                                        Thread.sleep(80);
                                    } catch (InterruptedException e) {

                                    }
                                } while (StateEnum.fetching.name().equalsIgnoreCase(nowStage));
                            }
                        }) .start();
                        return;
                    }
                }
            }
            else {
                nowStage = StateEnum.fetched.name();
            }
        }
    }
}

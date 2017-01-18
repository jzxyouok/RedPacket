package com.hsj.redpacket.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hsj.redpacket.base.BaseInfo;
import com.hsj.redpacket.view.SwitchButton;
import com.hsj.redpacket.R;

/**
 * @Company: ****科技有限公司
 * @Class: MeFragment
 * @Description:
 * @Author: HSJ
 * @Version:
 * @Date: 2017/1/10 18:29
 * @E-mail: mr.ajun@foxmail.com
 */
public class MeFragment extends Fragment implements SwitchButton.OnCheckedChangeListener {

    private final String TAG = "MeFragment";
    protected View view;
    private SwitchButton qq;    //开启qq红包按钮
    private SwitchButton weChat;//开启微信红包按钮
    private SwitchButton aliPay;//开启支付宝红包按钮
    private SwitchButton autoBack;//抢到红包自动返回按钮
    private TextView tv_qq;
    private TextView tv_weChat;
    private TextView tv_aliPay;
    private TextView tv_back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_me, null);

            tv_qq = (TextView) view.findViewById(R.id.tv_qq);
            tv_weChat = (TextView) view.findViewById(R.id.tv_weChat);
            tv_aliPay = (TextView) view.findViewById(R.id.tv_aliPay);
            tv_back = (TextView) view.findViewById(R.id.tv_auto_back);
            qq = (SwitchButton) view.findViewById(R.id.sb_qq);
            weChat = (SwitchButton) view.findViewById(R.id.sb_weChat);
            aliPay = (SwitchButton) view.findViewById(R.id.sb_aliPay);
            autoBack = (SwitchButton) view.findViewById(R.id.sb_auto_back);
            qq.setOnCheckedChangeListener(this);
            weChat.setOnCheckedChangeListener(this);
            aliPay.setOnCheckedChangeListener(this);
            autoBack.setOnCheckedChangeListener(this);

        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initButtonState();
    }

    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.sb_qq:
            case R.id.sb_weChat:
            case R.id.sb_aliPay:
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(getActivity(), "请开启红包服务", Toast.LENGTH_LONG).show();
                break;
            case R.id.sb_auto_back:
                String title = tv_back.getText().toString();
                if (getResources().getString(R.string.btn_auto_back_start).equalsIgnoreCase(title)) {
                    BaseInfo.autoBackFlag = true;
                    tv_back.setText(R.string.btn_auto_back_close);
                } else {
                    BaseInfo.autoBackFlag = false;
                    tv_back.setText(R.string.btn_auto_back_start);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化各个按钮的状态
     */
    private void initButtonState() {
        if (BaseInfo.qqAsHasOpened) {
            tv_qq.setText(R.string.btn_qq_assist_close);
        } else {
            tv_qq.setText(R.string.btn_qq_assist_start);
        }

        if (BaseInfo.wechatAsHasOpened) {
            tv_weChat.setText(R.string.btn_wechat_assist_close);
        } else {
            tv_weChat.setText(R.string.btn_wechat_assist_start);
        }

        if (BaseInfo.xiuAsHasOpened) {
            tv_aliPay.setText(R.string.btn_alipay_xiu_assist_close);
        } else {
            tv_aliPay.setText(R.string.btn_alipay_xiu_assist_start);
        }

        if (BaseInfo.autoBackFlag) {
            tv_back.setText(R.string.btn_auto_back_close);
        } else {
            tv_back.setText(R.string.btn_auto_back_start);
        }
    }

}

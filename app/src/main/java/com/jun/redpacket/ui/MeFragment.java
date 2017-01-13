package com.jun.redpacket.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.jun.redpacket.R;
import com.jun.redpacket.base.BaseInfo;

/**
 * @Company: ****科技有限公司
 * @Class: MeFragment
 * @Description:
 * @Author: HSJ
 * @Version: RedPacket V1.0
 * @Date: 2017/1/10 18:29
 * @E-mail: mr.ajun@foxmail.com
 */
public class MeFragment extends Fragment implements View.OnClickListener {

    private final String TAG = "MeFragment";
    protected View view;
    private Button qq;       //开启qq红包按钮
    private Button weChat;   //开启微信红包按钮
    private Button aliPay;   //开启支付宝红包按钮
    private Button autoBack; //抢到红包自动返回按钮

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_me, null);
            qq = (Button) view.findViewById(R.id.btn_qq);
            weChat = (Button) view.findViewById(R.id.btn_weChat);
            aliPay = (Button) view.findViewById(R.id.btn_aliPay);
            autoBack = (Button) view.findViewById(R.id.btn_auto_back);
            qq.setOnClickListener(this);
            weChat.setOnClickListener(this);
            aliPay.setOnClickListener(this);
            autoBack.setOnClickListener(this);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initButtonState();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_qq:
            case R.id.btn_weChat:
            case R.id.btn_aliPay:

                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);

                startActivity(intent);

                Toast.makeText(getActivity(), "请开启红包服务", Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_auto_back:
                String title = autoBack.getText().toString();
                if (getResources().getString(R.string.btn_auto_back_start).equalsIgnoreCase(title)) {
                    BaseInfo.autoBackFlag = true;
                    autoBack.setText(R.string.btn_auto_back_close);
                } else {
                    BaseInfo.autoBackFlag = false;
                    autoBack.setText(R.string.btn_auto_back_start);
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
            qq.setText(R.string.btn_qq_assist_close);
        } else {
            qq.setText(R.string.btn_qq_assist_start);
        }

        if (BaseInfo.wechatAsHasOpened) {
            weChat.setText(R.string.btn_wechat_assist_close);
        } else {
            weChat.setText(R.string.btn_wechat_assist_start);
        }

        if (BaseInfo.xiuAsHasOpened) {
            aliPay.setText(R.string.btn_alipay_xiu_assist_close);
        } else {
            aliPay.setText(R.string.btn_alipay_xiu_assist_start);
        }

        if (BaseInfo.autoBackFlag) {
            autoBack.setText(R.string.btn_auto_back_close);
        } else {
            autoBack.setText(R.string.btn_auto_back_start);
        }
    }

}

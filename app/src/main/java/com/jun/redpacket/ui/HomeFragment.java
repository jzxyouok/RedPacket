package com.jun.redpacket.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jun.redpacket.R;

/**
 * @Company: ****科技有限公司
 * @Class: HomeFragment
 * @Description:
 * @Author: HSJ
 * @Version: RedPacket V1.0
 * @Date: 2017/1/10 18:29
 * @E-mail: mr.ajun@foxmail.com
 */
public class HomeFragment extends Fragment{

    private final String TAG = "HomeFragment";
    protected View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, null);

        }
        return view;
    }

}

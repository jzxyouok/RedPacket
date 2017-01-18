package com.hsj.redpacket.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.hsj.redpacket.R;
import java.util.ArrayList;
import java.util.List;

/**
 * @Company: ****科技有限公司
 * @Class: MainActivity
 * @Description:
 * @Author: HSJ
 * @Version:
 * @Date: 2017/1/9 11:35
 * @E-mail: mr.ajun@foxmail.com
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    List<Fragment> fragments = new ArrayList();
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initData();

    }

    private void initView() {

        fragments.add(new HomeFragment());
        fragments.add(new MeFragment());

        radioGroup = (RadioGroup) findViewById(R.id.main_rg);
        radioGroup.setOnCheckedChangeListener(checkListener);
        ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
    }

    private void initData() {
        //TODO 逻辑操作
    }

    /**
     * RadioGroup改变监听
     */
    private RadioGroup.OnCheckedChangeListener checkListener = new RadioGroup.OnCheckedChangeListener() {
        /**
         * @param group         设置了监听的控件
         * @param checkedId     被勾选的RadioButton的id
         */
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            View child = group.findViewById(checkedId);
            int index = group.indexOfChild(child);
            Fragment fragment = fragments.get(index);

            replaceFragment(fragment);
        }
    };

    /**
     * 替代fragment
     *
     * @param fragment
     */
    private void replaceFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment)
                .commit();
    }


}

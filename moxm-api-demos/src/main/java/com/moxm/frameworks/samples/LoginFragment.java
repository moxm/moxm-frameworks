package com.moxm.frameworks.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.moxm.frameworks.samples.event.LoginSuccessEvent;
import com.moxm.frameworks.samples.otto.BusProvider;

/**
 * Created by Richard on 15/6/2.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {


    private Button mButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_login, container, false);
        init(savedInstanceState);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillView();
        setListener();
        loadData();
    }

    protected void init(Bundle savedInstanceState) {

    }

    /**
     * 获取View对象
     */
    protected void fillView() {
        mButton = (Button) getView().findViewById(R.id.btn_login);
    }
    /**
     * 设置监听事件
     */
    protected void setListener() {
        mButton.setOnClickListener(this);
    }
    /**
     * 加载填充数据
     */
    protected void loadData() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                BusProvider.getInstance().post(new LoginSuccessEvent());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

}

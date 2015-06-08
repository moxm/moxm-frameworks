package com.moxm.frameworks.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.moxm.frameworks.samples.event.LoginSuccessEvent;
import com.moxm.frameworks.samples.otto.BusProvider;
import com.squareup.otto.Subscribe;

/**
 * Created by Richard on 15/6/8.
 */
public class LoginActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(savedInstanceState);
        fillView();
        setListener();
        loadData();
    }

    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.item_login);
    }

    /**
     * 获取View对象
     */
    protected void fillView() {

    }
    /**
     * 设置监听事件
     */
    protected void setListener() {
    }
    /**
     * 加载填充数据
     */
    protected void loadData() {
    }

    @Subscribe
    public void onLoginSuccessEvent(LoginSuccessEvent event) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

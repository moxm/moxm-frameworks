package com.moxm.frameworks.samples;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


/**
 * Created by Richard on 15/5/29.
 */
public class ToolbarActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(savedInstanceState);
        fillView();
        setListener();
        loadData();

    }
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.item_toolbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

}

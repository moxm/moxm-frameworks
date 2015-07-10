package com.moxm.frameworks.samples.view.design;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moxm.frameworks.samples.R;
import com.moxm.frameworks.samples.event.ContentFragmentEvent;
import com.moxm.frameworks.samples.otto.BusProvider;


/**
 * Created by Richard on 15/6/2.
 */
public class TabFragment extends Fragment {


    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_tab, container, false);
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
        mTabLayout = (TabLayout) getView().findViewById(R.id.tab);
        mViewPager = (ViewPager) getView().findViewById(R.id.pager);

        mTabLayout.addTab(mTabLayout.newTab().setText("Home"), true);
        mTabLayout.addTab(mTabLayout.newTab().setText("All"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Mark"));
        mTabLayout.setupWithViewPager(mViewPager);
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
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


//    @Produce
    public ContentFragmentEvent produceContentEvent(Fragment content){
        return new ContentFragmentEvent(content);
    }


}

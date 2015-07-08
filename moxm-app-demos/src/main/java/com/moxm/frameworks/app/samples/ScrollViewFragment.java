package com.moxm.frameworks.app.samples;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created by Richard on 15/6/2.
 */
public class ScrollViewFragment extends Fragment {



    private ScrollView mScrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_scroll, container, false);
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
        mScrollView = (ScrollView) getView().findViewById(R.id.scorll);
    }
    /**
     * 设置监听事件
     */
    protected void setListener() {
        mScrollView.setOnTouchListener(onTouchListener);
    }
    /**
     * 加载填充数据
     */
    protected void loadData() {
    }


    /*
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() != R.id.scorll)
            return false;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int scrollY = v.getScrollY();
                int height = v.getHeight();
                int scrollViewMeasuredHeight = mScrollView.getChildAt(0).getMeasuredHeight();
                if(scrollY == 0){
                    System.out.println("滑动到了顶端 view.getScrollY()="+scrollY);
                }
                if((scrollY + height) == scrollViewMeasuredHeight){
                    System.out.println("滑动到了底部 scrollY="+scrollY);
                    System.out.println("滑动到了底部 height="+height);
                    System.out.println("滑动到了底部 scrollViewMeasuredHeight="+scrollViewMeasuredHeight);
                }
                break;
            case MotionEvent.ACTION_UP:
        }
        return false;
    }
    */

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        private int lastY = 0;
        private int touchEventId = -9983761;
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                View scroller = (View) msg.obj;
                if (msg.what == touchEventId) {
                    if (lastY == scroller.getScrollY()) {
                        handleStop(scroller);
                    } else {
                        handler.sendMessageDelayed(handler.obtainMessage(touchEventId, scroller), 5);
                        lastY = scroller.getScrollY();
                    }
                }
            }
        };


        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.sendMessageDelayed(handler.obtainMessage(touchEventId, v), 5);
            }
            return false;
        }


        private void handleStop(Object view) {
            ScrollView scroller = (ScrollView) view;
            int scrollY = scroller.getScrollY();
        }
    };

}

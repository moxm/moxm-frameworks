package cn.neel.sample.compat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.neel.sample.compat.event.ContentFragmentEvent;
import cn.neel.sample.compat.otto.BusProvider;

/**
 * Created by Richard on 15/6/2.
 */
public class BehindFragment extends Fragment implements View.OnClickListener {


    private Button button1;
    private Button button2;
    private Button button3;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_behind, container, false);
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
        button1 = (Button) getView().findViewById(R.id.button1);
        button2 = (Button) getView().findViewById(R.id.button2);
        button3 = (Button) getView().findViewById(R.id.button3);
    }
    /**
     * 设置监听事件
     */
    protected void setListener() {
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                Fragment content1 = new VelloyFragment();
                BusProvider.getInstance().post(produceContentEvent(content1));
                break;
            case R.id.button2:
                Fragment content2 = new Content2Fragment();
                BusProvider.getInstance().post(produceContentEvent(content2));
                break;
            case R.id.button3:
                Fragment content3 = new Content3Fragment();
                BusProvider.getInstance().post(produceContentEvent(content3));
                break;
        }
    }

//    @Produce
    public ContentFragmentEvent produceContentEvent(Fragment content){
        return new ContentFragmentEvent(content);
    }
}

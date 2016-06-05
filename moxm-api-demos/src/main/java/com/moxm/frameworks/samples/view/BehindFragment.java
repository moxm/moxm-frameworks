package com.moxm.frameworks.samples.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.moxm.frameworks.samples.R;
import com.moxm.frameworks.samples.event.ContentFragmentEvent;
import com.moxm.frameworks.samples.otto.BusProvider;
import com.moxm.frameworks.samples.view.design.DesignFragment;
import com.moxm.frameworks.samples.view.volley.VolleyFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Richard on 15/6/2.
 */
public class BehindFragment extends Fragment implements AdapterView.OnItemClickListener {


    private ListView mListView;

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
        mListView = (ListView) getView().findViewById(R.id.list);
    }
    /**
     * 设置监听事件
     */
    protected void setListener() {
        mListView.setOnItemClickListener(this);
    }
    /**
     * 加载填充数据
     */
    protected void loadData() {
        mListView.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                getData()));
    }

    private List<String> getData() {
        List<String> data = new ArrayList<String>();
        data.add("Design");
        data.add("Volley");
        data.add("Toolbar");
        return data;
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayAdapter<String> adapter = (ArrayAdapter)parent.getAdapter();
        String value = adapter.getItem(position);
        if (value.equals("Design")) {
            Fragment content = new DesignFragment();
            BusProvider.getInstance().post(produceContentEvent(content));
            return;
        }
        if (value.equals("Volley")) {
            Fragment content = new VolleyFragment();
            BusProvider.getInstance().post(produceContentEvent(content));
            return;
        }
        if (value.equals("Toolbar")) {
            Intent intent = new Intent(getActivity(), ToolbarActivity.class);
            startActivity(intent);
            return;
        }
    }

    /*
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
                Fragment content3 = new UiFragment();
                BusProvider.getInstance().post(produceContentEvent(content3));
                break;
            case R.id.button4:
                Fragment content4 = new ScrollViewFragment();
                BusProvider.getInstance().post(produceContentEvent(content4));
                break;
        }
    }*/

//    @Produce
    public ContentFragmentEvent produceContentEvent(Fragment content){
        return new ContentFragmentEvent(content);
    }


}

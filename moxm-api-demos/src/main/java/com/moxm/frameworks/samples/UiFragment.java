package com.moxm.frameworks.samples;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richard on 15/6/2.
 */
public class UiFragment extends Fragment implements AdapterView.OnItemClickListener {


    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_ui, container, false);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getData());
        mListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String action = (String) parent.getItemAtPosition(position);
        if(action.equals("Toolbar")) {
            Intent intent = new Intent(getActivity(), ToolbarActivity.class);
            startActivity(intent);
            return;
        }
    }


    private List<String> getData() {
        List<String> data = new ArrayList<String>();
        data.add("Actionbar");
        data.add("Toolbar");
        return data;
    }
}

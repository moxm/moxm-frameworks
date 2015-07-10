package com.moxm.frameworks.samples.view.design;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Richard on 15/6/2.
 */
public class DesignFragment extends Fragment implements AdapterView.OnItemClickListener {


    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_design, container, false);
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
        data.add("Snackbar");
        data.add("TextInputLayout");
        data.add("TabLayout");
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
        if (value.equals("Snackbar")) {
            showSnackbar(view);
            return;
        }
        if (value.equals("TextInputLayout")) {
            Fragment content = new TextInputFragment();
            BusProvider.getInstance().post(produceContentEvent(content));
            return;
        }
        if (value.equals("TabLayout")) {
            Fragment content = new TabFragment();
            BusProvider.getInstance().post(produceContentEvent(content));
            return;
        }
    }


    private void showSnackbar(View view) {
        Snackbar.make(view, "Snackbar comes out", Snackbar.LENGTH_LONG)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

//    @Produce
    public ContentFragmentEvent produceContentEvent(Fragment content){
        return new ContentFragmentEvent(content);
    }


}

package com.moxm.frameworks.samples.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import com.moxm.frameworks.samples.R;



/**
 * Created by Richard on 15/6/2.
 */
public class VelloyFragment extends Fragment implements View.OnClickListener {


    private Button button1;
    private Button button2;


    private RequestQueue mQueue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_velloy, container, false);
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
        mQueue = Volley.newRequestQueue(getActivity());
    }

    /**
     * 获取View对象
     */
    protected void fillView() {
        button1 = (Button) getView().findViewById(R.id.button1);
        button2 = (Button) getView().findViewById(R.id.button2);
    }
    /**
     * 设置监听事件
     */
    protected void setListener() {
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
    }
    /**
     * 加载填充数据
     */
    protected void loadData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                StringRequest getRequest = new StringRequest(Method.GET, "http://m.weather.com.cn/data/101010100.html",  listener, errorListener);
                mQueue.add(getRequest);
//                mQueue.start();
                break;
            case R.id.button2:
                StringRequest postRequest = new StringRequest(Method.POST, "http://m.weather.com.cn/data/101010100.html",  listener, errorListener) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("params1", "value1");
                        map.put("params2", "value2");
                        return map;
                    }
                };
                mQueue.add(postRequest);
                break;
        }
    }

    private Response.Listener<String> listener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.d("Listener", "++++++" + response);
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.d("ErrorListener", "++++++" + volleyError.getMessage());
        }
    };

}

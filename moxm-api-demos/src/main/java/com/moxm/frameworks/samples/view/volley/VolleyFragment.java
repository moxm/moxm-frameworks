package com.moxm.frameworks.samples.view.volley;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moxm.frameworks.samples.R;
import com.moxm.frameworks.samples.http.HttpExecute;
import com.moxm.frameworks.samples.http.ImageFile;
import com.moxm.frameworks.volley.toolbox.MultipartEntity;
import com.moxm.frameworks.volley.toolbox.MultipartStringRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Richard on 15/6/2.
 */
public class VolleyFragment extends Fragment implements View.OnClickListener {


    private static final String DOMAIN = "http://192.168.1.112:8080";

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private Button button9;


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
        button3 = (Button) getView().findViewById(R.id.button3);
        button4 = (Button) getView().findViewById(R.id.button4);
        button5 = (Button) getView().findViewById(R.id.button5);
        button6 = (Button) getView().findViewById(R.id.button6);
        button7 = (Button) getView().findViewById(R.id.button7);
        button8 = (Button) getView().findViewById(R.id.button8);
        button9 = (Button) getView().findViewById(R.id.button9);
    }
    /**
     * 设置监听事件
     */
    protected void setListener() {
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);
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
                StringRequest getRequest = new StringRequest(Method.GET, DOMAIN + "/testGet?senderNumber=13500000001&messageBody=测试GET&receiveTime=2015-03-04 14:22:00",  listener, errorListener);
                mQueue.add(getRequest);
//                mQueue.start();
                break;
            case R.id.button2:
                StringRequest postRequest = new StringRequest(Method.POST, DOMAIN + "/testBody",  listener, errorListener) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("senderNumber", "13500000001");
                        map.put("messageBody", "测试");
                        map.put("receiveTime", "2015-03-04 14:22:00");
                        return map;
                    }
                };
                mQueue.add(postRequest);
                break;
            case R.id.button3:
                MultipartStringRequest multiRequest = new MultipartStringRequest(Method.POST, DOMAIN + "/testBody",  listener, errorListener);
                MultipartEntity entity = multiRequest.getMultiPartEntity();

                entity.addStringPart("senderNumber", "13500000001");
                entity.addStringPart("messageBody", "测试");
                entity.addStringPart("receiveTime", "2015-03-04 14:22:00");

                mQueue.add(multiRequest);
                break;
            case R.id.button4:
                multiRequest = new MultipartStringRequest(Method.POST, DOMAIN + "/testBody",  listener, errorListener);
                entity = multiRequest.getMultiPartEntity();

                Map<String, String> data = new HashMap<String, String>();
                data.put("senderNumber", "13500000001");
                data.put("messageBody", "测试");
                data.put("receiveTime", "2015-03-04 14:22:00");

                JSONObject json = new JSONObject(data);

//                entity.addJsonPart(json.toString());

                mQueue.add(multiRequest);
                break;


            case R.id.button8:
                Map<String, String> map = new HashMap<String, String>();
                map.put("senderNumber", "13500000001");
                map.put("messageBody", "测试");
                map.put("receiveTime", "2015-03-04 14:22:00");

                JSONObject jsonObject = new JSONObject(map);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.POST, DOMAIN + "/testBody", jsonObject, jsonListener, errorListener);

                mQueue.add(jsonObjectRequest);
                break;
            case R.id.button9:
                new DataTask().execute();
                break;

            case R.id.button6:
                multiRequest = new MultipartStringRequest(Method.POST, DOMAIN + "/testMulti",  listener, errorListener);
                entity = multiRequest.getMultiPartEntity();

                entity.addStringPart("senderNumber", "13500000001");
                entity.addStringPart("messageBody", "测试");
                entity.addStringPart("receiveTime", "2015-03-04 14:22:00");

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc_ab_share_pack_mtrl_alpha);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                entity.addBinaryPart("images", byteArray, "abc_ab_share_pack_mtrl_alpha_1.png");

                mQueue.add(multiRequest);
                break;

            case R.id.button7:
                multiRequest = new MultipartStringRequest(Method.POST, DOMAIN + "/testMulti",  listener, errorListener) {


                    @Override
                    protected void setParams(MultipartEntity entity) throws AuthFailureError {
                        entity.addStringPart("senderNumber", "13500000001");
                        entity.addStringPart("senderNumber", "13500000002");
                        entity.addStringPart("senderNumber", "13500000003");
                        entity.addStringPart("messageBody", "测试");
                        entity.addStringPart("receiveTime", "2015-03-04 14:22:00");

                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/1441454427634.jpg");
                        entity.addFilePart("images", file);
                        /*
                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/P50713-183019.jpg");
                        entity.addFilePart("images", file);

                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/P50723-224405.jpg");
                        entity.addFilePart("images", file);*/

                        Log.d("DCIM", "--------->" + Environment.getExternalStorageDirectory().getAbsolutePath());
                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/IMG_20150606_104258.jpg");
                        entity.addFilePart("images", file);

                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/IMG_20150905_201850.jpg");
                        entity.addFilePart("images", file);

                        Map<String, byte[]> params = new HashMap<String, byte[]>();
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc_ab_share_pack_mtrl_alpha);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        entity.addBinaryPart("binary", byteArray, "abc_ab_share_pack_mtrl_alpha.png");
                    }
                };
                //设置请求参数，例如超时时间，重连次数
                multiRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                entity = multiRequest.getMultiPartEntity();
//
//                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Screenshots/S50715-153423.jpg");
//                entity.addFilePart("images", file);

                mQueue.add(multiRequest);
                break;
        }
    }

    private Response.Listener<String> listener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.d("Response.Listener", "++++++" + response);
        }
    };


    private Response.Listener<JSONObject> jsonListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d("Response.Listener", "++++++" + response.toString());
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.d("Response.ErrorListener", "++++++" + volleyError.getMessage());
        }
    };


    private class DataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc_ab_share_pack_mtrl_alpha);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            ImageFile formFile = new ImageFile(byteArray, "images");
            formFile.setName("abc_ab_share_pack_mtrl_alpha.png");

            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("senderNumber", "13500000001"));
            postParameters.add(new BasicNameValuePair("messageBody", "测试"));
            postParameters.add(new BasicNameValuePair("receiveTime", "2015-03-04 14:22:00"));

            try {
                String json = HttpExecute.post(DOMAIN + "/testMulti", postParameters, formFile);
                Log.d("++++++++++++DataTask", json);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


}

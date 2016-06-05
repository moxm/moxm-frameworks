package com.moxm.frameworks.volley.toolbox;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Richard on 15/7/24.
 */
public abstract class MultipartRequest<T> extends Request<T> {

    protected MultipartEntity mMultiPartEntity = new MultipartEntity();

    private final Listener<T> mListener;

    public MultipartRequest(String url, Listener<T> listener, ErrorListener errorListener) {
        this(Method.DEPRECATED_GET_OR_POST, url, listener, errorListener);
    }

    public MultipartRequest(int method, String url, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    /**
     * @return
     */
    public MultipartEntity getMultiPartEntity() {
        return mMultiPartEntity;
    }

    @Override
    public String getBodyContentType() {
        return mMultiPartEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {

        /*
        //-------------添加File类型文件--------------
        if (this.getFileParams() != null && !this.getFileParams().isEmpty()) {
            Iterator uee = this.getFileParams().entrySet().iterator();
            while(uee.hasNext()) {
                Map.Entry<String, File> entry = (Map.Entry<String, File>)uee.next();
                mMultiPartEntity.addFilePart(entry.getKey(), entry.getValue());
            }
        }
        //-------------添加文本类型文件--------------
        if (this.getParams() != null && !this.getParams().isEmpty()) {
            Iterator uee = this.getParams().entrySet().iterator();
            while(uee.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>)uee.next();
                mMultiPartEntity.addStringPart(entry.getKey(), entry.getValue());
            }
        }

        //-------------添加二进制参数, 例如Bitmap的字节流参数--------------
        if (this.getBinaryParams() != null && !this.getBinaryParams().isEmpty()) {
            Iterator uee = this.getBinaryParams().entrySet().iterator();
            while(uee.hasNext()) {
                Map.Entry<String, byte[]> entry = (Map.Entry<String, byte[]>)uee.next();
                mMultiPartEntity.addBinaryPart(entry.getKey(), entry.getValue());
            }
        }
        */

        //添加参数
        setParams(mMultiPartEntity);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            // 将mMultiPartEntity中的参数写入到bos中
            mMultiPartEntity.writeTo(bos);
        } catch (IOException e) {
            Log.e("", "IOException writing to ByteArrayOutputStream");
        }

        return bos.toByteArray();
    }




    @Override
    protected void deliverResponse(T response) {
        this.mListener.onResponse(response);
    }

    @Override
    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);


    protected void setParams(MultipartEntity entity) throws AuthFailureError {

    }

}

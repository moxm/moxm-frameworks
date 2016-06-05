package com.moxm.frameworks.volley.toolbox;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

/**
 * Created by Richard on 15/7/24.
 */
public class MultipartStringRequest extends MultipartRequest<String> {

    public MultipartStringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
        this(Method.DEPRECATED_GET_OR_POST, url, listener, errorListener);
    }

    public MultipartStringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException var4) {
            parsed = new String(response.data);
        }

        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

}

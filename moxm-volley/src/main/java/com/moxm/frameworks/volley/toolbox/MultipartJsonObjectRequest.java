package com.moxm.frameworks.volley.toolbox;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Richard on 15/7/24.
 */
public class MultipartJsonObjectRequest extends MultipartRequest<JSONObject> {

    public MultipartJsonObjectRequest(String url, Listener<JSONObject> listener, ErrorListener errorListener) {
        this(Method.DEPRECATED_GET_OR_POST, url, listener, errorListener);
    }

    public MultipartJsonObjectRequest(int method, String url, Listener<JSONObject> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String je = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            return Response.success(new JSONObject(je), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

}

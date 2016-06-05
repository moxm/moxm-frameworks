package com.moxm.frameworks.volley.toolbox;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created by Richard on 15/7/24.
 */
public class MultipartJsonArrayRequest extends MultipartRequest<JSONArray> {

    public MultipartJsonArrayRequest(String url, Listener<JSONArray> listener, ErrorListener errorListener) {
        this(Method.DEPRECATED_GET_OR_POST, url, listener, errorListener);
    }

    public MultipartJsonArrayRequest(int method, String url, Listener<JSONArray> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String je = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            return Response.success(new JSONArray(je), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException var3) {
            return Response.error(new ParseError(var3));
        } catch (JSONException var4) {
            return Response.error(new ParseError(var4));
        }
    }

}

package com.moxm.frameworks.volley.toolbox;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * Created by Richard on 15/8/02.
 */
public class GsonBodyRequest<T> extends JsonRequest<T> {

    private Type mTypeOfT;


    public GsonBodyRequest(int method, String url, String requestBody, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
        this.mTypeOfT = typeOfT;
    }

    public GsonBodyRequest(String url, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        super(0, url, (String) null, listener, errorListener);
        this.mTypeOfT = typeOfT;
    }

    public GsonBodyRequest(int method, String url, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, (String)null, listener, errorListener);
        this.mTypeOfT = typeOfT;
    }

    public GsonBodyRequest(int method, String url, JSONObject jsonRequest, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, jsonRequest == null ? null : jsonRequest.toString(), listener, errorListener);
        this.mTypeOfT = typeOfT;
    }

    public GsonBodyRequest(String url, JSONObject jsonRequest, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        this(jsonRequest == null?0:1, url, jsonRequest, typeOfT, listener, errorListener);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            Gson gson = new Gson();
            String je = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            T parsed = gson.fromJson(je, this.mTypeOfT);
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException je) {
            return Response.error(new ParseError(je));
        }
    }

}

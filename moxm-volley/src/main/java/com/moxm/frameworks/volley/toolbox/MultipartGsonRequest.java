package com.moxm.frameworks.volley.toolbox;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * Created by Richard on 15/7/24.
 */
public class MultipartGsonRequest<T> extends MultipartRequest<T> {

    private Type mTypeOfT;
    private Gson mGson;

    public MultipartGsonRequest(String url, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        this(Method.DEPRECATED_GET_OR_POST, url, typeOfT, listener, errorListener);
    }

    public MultipartGsonRequest(int method, String url, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.mTypeOfT = typeOfT;
        this.mGson = new Gson();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String je = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            T parsed = mGson.fromJson(je, this.mTypeOfT);
            return Response.success(parsed,
                    HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException je) {
            return Response.error(new ParseError(je));
        }
    }

}

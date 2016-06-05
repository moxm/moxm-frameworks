package com.moxm.frameworks.volley.toolbox;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by Richard on 15/8/02.
 */
public class GsonRequest<T> extends Request<T> {

    private Type mTypeOfT;
    private Gson mGson;
    private final Listener<T> mListener;

    public GsonRequest(int method, String url, Type typeOfT, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mTypeOfT = typeOfT;
        this.mGson = new Gson();
    }

    public GsonRequest(String url, Class<T> cls, Listener<T> listener, ErrorListener errorListener) {
        this(Method.POST, url, cls, listener, errorListener);
    }

    protected void deliverResponse(T response) {
        this.mListener.onResponse(response);
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

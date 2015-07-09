package com.moxm.frameworks.samples.view.design;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.moxm.frameworks.samples.R;
import com.moxm.frameworks.samples.event.ContentFragmentEvent;
import com.moxm.frameworks.samples.otto.BusProvider;


/**
 * Created by Richard on 15/6/2.
 */
public class TextInputFragment extends Fragment {


    private TextInputLayout mInputUsername;
    private TextInputLayout mInputPassword;
    private EditText mEditUsername;
    private EditText mEditPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_text_input, container, false);
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
        mInputUsername = (TextInputLayout) getView().findViewById(R.id.input_username);
        mInputPassword = (TextInputLayout) getView().findViewById(R.id.input_password);
        mEditUsername = mInputUsername.getEditText();
        mEditPassword = mInputPassword.getEditText();
    }
    /**
     * 设置监听事件
     */
    protected void setListener() {

        mEditUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() > 4) {
                    mInputUsername.setError("Username error");
                    mInputUsername.setErrorEnabled(true);
                } else {
                    mInputUsername.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    /**
     * 加载填充数据
     */
    protected void loadData() {
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


//    @Produce
    public ContentFragmentEvent produceContentEvent(Fragment content){
        return new ContentFragmentEvent(content);
    }


}

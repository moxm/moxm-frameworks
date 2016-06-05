package com.moxm.frameworks.alert;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.moxm.frameworks.progress.ProgressWheel;

public class SweetLoading extends Dialog {


    private View mDialogView;
    private TextView mMessageView;

    private CharSequence message;


    private ProgressHelper mProgressHelper;

    public SweetLoading(Context context) {
        super(context, R.style.alert_dialog);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mProgressHelper = new ProgressHelper(context);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog);

        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mMessageView = (TextView)findViewById(R.id.content_text);
        mProgressHelper.setProgressWheel((ProgressWheel)findViewById(R.id.progressWheel));

        if(TextUtils.isEmpty(message)) {
            mMessageView.setVisibility(View.GONE);
        } else {
            mMessageView.setVisibility(View.VISIBLE);
        }
        mMessageView.setText(message);
    }


    /**
     * Creates a {@link SweetLoading} with the arguments supplied to this builder and
     * {@link Dialog#show()}'s the dialog.
     */
    public static SweetLoading show(Context context) {
        return show(context, null);
    }
    public static SweetLoading show(Context context, String message) {
        return show(context, message, false);
    }
    public static SweetLoading show(Context context, String message, boolean cancelable) {
        SweetLoading dialog = new SweetLoading(context).message(message).cancelable(cancelable);
        dialog.show();
        return dialog;
    }
    public SweetLoading cancelable(boolean flag) {
        setCancelable(flag);
        if (flag) {
            setCanceledOnTouchOutside(true);
        }
        return this;
    }

    public SweetLoading message(CharSequence message) {
        if(TextUtils.isEmpty(message) && mMessageView != null) {
            mMessageView.setVisibility(View.GONE);
            mMessageView.setText(message);
        }
        this.message = message;
        return this;
    }


    public ProgressHelper getProgressHelper () {
        return mProgressHelper;
    }
}
package com.moxm.frameworks.support.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.moxm.frameworks.support.R;
//import android.support.design.R.style;
//import android.support.design.R.styleable;

/**
 * 修复google官方design包中android.support.design.widget.TextInputLayout,设置android:hint 无效的BUG
 * Created by Richard on 15/7/9.
 */
public class TextInputLayout extends android.support.design.widget.TextInputLayout {

    public TextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextInputLayout, 0, R.style.Widget_Design_TextInputLayout);
        CharSequence hint = a.getText(R.styleable.TextInputLayout_android_hint);
        if(hint != null) {
            this.setHint(hint);
        }
        a.recycle();
    }
}

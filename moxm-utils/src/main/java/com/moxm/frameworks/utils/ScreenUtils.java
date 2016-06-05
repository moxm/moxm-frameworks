package com.moxm.frameworks.utils;

import android.content.Context;

/**
 * ScreenUtils
 * <ul>
 * <strong>Convert between dp and sp</strong>
 * <li>{@link ScreenUtils#dp2Px(Context, float)}</li>
 * <li>{@link ScreenUtils#px2Dp(Context, float)}</li>
 * </ul>
 * 
 * @author <a href="http://www.moxm.com" target="_blank">Moxm</a> 2014-2-14
 */
public class ScreenUtils {

    private ScreenUtils() {
        throw new AssertionError();
    }

    public static float dp2Px(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float px2Dp(Context context, float px) {
        if (context == null) {
            return -1;
        }
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int dp2PxInt(Context context, float dp) {
        return (int)(dp2Px(context, dp) + 0.5f);
    }

    public static int px2DpCeilInt(Context context, float px) {
        return (int)(px2Dp(context, px) + 0.5f);
    }
}

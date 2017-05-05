package com.blikoon.qrcodescanner.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtils {

    private ScreenUtils() {
        throw new AssertionError();
    }


    public static int getScreenWidth(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }


    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenHeight = dm.heightPixels;
        return screenHeight;
    }

}

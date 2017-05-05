package com.blikoon.qrcodescanner.decode;


import android.app.Activity;
import android.content.DialogInterface;

/**
 * Simple listener used to exit the app in a few cases.
 *
 */
public final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener,
        Runnable {

    private final Activity mActivityToFinish;

    public FinishListener(Activity activityToFinish) {
        this.mActivityToFinish = activityToFinish;
    }

    public void onCancel(DialogInterface dialogInterface) {
        run();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        run();
    }

    public void run() {
        mActivityToFinish.finish();
    }

}

package com.blikoon.qrcodescanner.camera;



import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

final class PreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = PreviewCallback.class.getName();
    private final CameraConfigurationManager mConfigManager;
    private Handler mPreviewHandler;
    private int mPreviewMessage;

    PreviewCallback(CameraConfigurationManager configManager) {
        this.mConfigManager = configManager;
    }

    void setHandler(Handler previewHandler, int previewMessage) {
        this.mPreviewHandler = previewHandler;
        this.mPreviewMessage = previewMessage;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size cameraResolution = mConfigManager.getCameraResolution();
        if (mPreviewHandler != null) {
            Message message =
                    mPreviewHandler.obtainMessage(mPreviewMessage, cameraResolution.width, cameraResolution.height, data);
            message.sendToTarget();
            mPreviewHandler = null;
        } else {
            Log.v(TAG, "no handler callback.");
        }
    }

}
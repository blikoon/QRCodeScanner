package com.blikoon.qrcodescanner.camera;


import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

import com.blikoon.qrcodescanner.utils.ScreenUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

final class CameraConfigurationManager {
    private static final String TAG = CameraConfigurationManager.class.getName();
    private static final int TEN_DESIRED_ZOOM = 10;
    private static final int DESIRED_SHARPNESS = 30;

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private Camera.Size mCameraResolution;
    private Camera.Size mPictureResolution;
    private Context mContext;

    CameraConfigurationManager(Context context) {
        this.mContext = context;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        mCameraResolution = findCloselySize(ScreenUtils.getScreenWidth(mContext), ScreenUtils.getScreenHeight(mContext),
                parameters.getSupportedPreviewSizes());
        Log.e(TAG, "Setting preview size: " + mCameraResolution.width + "-" + mCameraResolution.height);
        mPictureResolution = findCloselySize(ScreenUtils.getScreenWidth(mContext),
                ScreenUtils.getScreenHeight(mContext), parameters.getSupportedPictureSizes());
        Log.e(TAG, "Setting picture size: " + mPictureResolution.width + "-" + mPictureResolution.height);
    }

    /**
     * Sets the camera up to take preview images which are used for both preview and decoding. We detect the preview
     * format here so that buildLuminanceSource() can build an appropriate LuminanceSource subclass. In the future we
     * may want to force YUV420SP as it's the smallest, and the planar Y can be used for barcode scanning without a copy
     * in some cases.
     */
    void setDesiredCameraParameters(Camera camera) {

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mCameraResolution.width, mCameraResolution.height);
        parameters.setPictureSize(mPictureResolution.width, mPictureResolution.height);
        setZoom(parameters);
        camera.setDisplayOrientation(90);
        camera.setParameters(parameters);
    }

    Camera.Size getCameraResolution() {
        return mCameraResolution;
    }

    private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {

        String previewSizeValueString = parameters.get("preview-size-values");
        // saw this on Xperia
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        Point cameraResolution = null;

        if (previewSizeValueString != null) {
            Log.e(TAG, "preview-size-values parameter: " + previewSizeValueString);
            cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
        }

        if (cameraResolution == null) {
            // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
            cameraResolution = new Point((screenResolution.x >> 3) << 3, (screenResolution.y >> 3) << 3);
        }

        return cameraResolution;
    }

    private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                Log.e(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newX;
            int newY;
            try {
                newY = Integer.parseInt(previewSize.substring(0, dimPosition));
                newX = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
        int tenBestValue = 0;
        for (String stringValue : COMMA_PATTERN.split(stringValues)) {
            stringValue = stringValue.trim();
            double value;
            try {
                value = Double.parseDouble(stringValue);
            } catch (NumberFormatException nfe) {
                return tenDesiredZoom;
            }
            int tenValue = (int) (10.0 * value);
            if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
                tenBestValue = tenValue;
            }
        }
        return tenBestValue;
    }

    private void setZoom(Camera.Parameters parameters) {

        String zoomSupportedString = parameters.get("zoom-supported");
        if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
            return;
        }

        int tenDesiredZoom = TEN_DESIRED_ZOOM;

        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null) {
            try {
                int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Bad max-zoom: " + maxZoomString);
            }
        }

        String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
        if (takingPictureZoomMaxString != null) {
            try {
                int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
            }
        }

        String motZoomValuesString = parameters.get("mot-zoom-values");
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
        }

        String motZoomStepString = parameters.get("mot-zoom-step");
        if (motZoomStepString != null) {
            try {
                double motZoomStep = Double.parseDouble(motZoomStepString.trim());
                int tenZoomStep = (int) (10.0 * motZoomStep);
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                }
            } catch (NumberFormatException nfe) {
                // continue
            }
        }

        // Set zoom. This helps encourage the user to pull back.
        // Some devices like the Behold have a zoom parameter
        // if (maxZoomString != null || motZoomValuesString != null) {
        // parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
        // }
        if (parameters.isZoomSupported()) {
            Log.e(TAG, "max-zoom:" + parameters.getMaxZoom());
            parameters.setZoom(parameters.getMaxZoom() / 10);
        } else {
            Log.e(TAG, "Unsupported zoom.");
        }

        // Most devices, like the Hero, appear to expose this zoom parameter.
        // It takes on values like "27" which appears to mean 2.7x zoom
        // if (takingPictureZoomMaxString != null) {
        // parameters.set("taking-picture-zoom", tenDesiredZoom);
        // }
    }

    public static int getDesiredSharpness() {
        return DESIRED_SHARPNESS;
    }

    protected Camera.Size findCloselySize(int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {

        //
        // int ReqTmpWidth = surfaceHeight;
        // int ReqTmpHeight = surfaceWidth;
        //
        // //
        // for (Size size : preSizeList) {
        // if ((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)) {
        // return size;
        // }
        // }
        //
        // //
        // float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
        // float curRatio, deltaRatio;
        // float deltaRatioMin = Float.MAX_VALUE;
        // Size retSize = null;
        // for (Size size : preSizeList) {
        // curRatio = ((float) size.width) / size.height;
        // deltaRatio = Math.abs(reqRatio - curRatio);
        // if (deltaRatio < deltaRatioMin) {
        // deltaRatioMin = deltaRatio;
        // retSize = size;
        // }
        // }
        Collections.sort(preSizeList, new SizeComparator(surfaceWidth, surfaceHeight));
        return preSizeList.get(0);
    }

    /**
     */
    private static class SizeComparator implements Comparator<Camera.Size> {

        private final int width;
        private final int height;
        private final float ratio;

        SizeComparator(int width, int height) {
            if (width < height) {
                this.width = height;
                this.height = width;
            } else {
                this.width = width;
                this.height = height;
            }
            this.ratio = (float) this.height / this.width;
        }

        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            int width1 = size1.width;
            int height1 = size1.height;
            int width2 = size2.width;
            int height2 = size2.height;

            float ratio1 = Math.abs((float) height1 / width1 - ratio);
            float ratio2 = Math.abs((float) height2 / width2 - ratio);
            int result = Float.compare(ratio1, ratio2);
            if (result != 0) {
                return result;
            } else {
                int minGap1 = Math.abs(width - width1) + Math.abs(height - height1);
                int minGap2 = Math.abs(width - width2) + Math.abs(height - height2);
                return minGap1 - minGap2;
            }
        }
    }
}

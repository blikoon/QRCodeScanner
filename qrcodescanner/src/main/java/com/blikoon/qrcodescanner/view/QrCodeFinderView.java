package com.blikoon.qrcodescanner.view;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.blikoon.qrcodescanner.R;
import com.blikoon.qrcodescanner.utils.ScreenUtils;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial transparency outside
 * it, as well as the laser scanner animation and result points.
 */
public final class QrCodeFinderView extends RelativeLayout {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;
    public boolean showText = true;
    public boolean showLaser = true;
    public boolean showCorners = true;
    private Context mContext;
    private Paint mPaint;
    private int mScannerAlpha;
    private int mMaskColor;
    private int mFrameColor;
    private int mLaserColor;
    private int mTextColor;
    private Rect mFrameRect;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;

    public QrCodeFinderView(Context context) {
        this(context, null);
    }

    public QrCodeFinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QrCodeFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mPaint = new Paint();


        Resources resources = getResources();
        mMaskColor = resources.getColor(R.color.qr_code_finder_mask);
        mFrameColor = resources.getColor(R.color.qr_code_finder_frame);
        mLaserColor = resources.getColor(R.color.qr_code_finder_laser);
        mTextColor = resources.getColor(R.color.qr_code_white);

        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;
        mScannerAlpha = 0;
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }
        setWillNotDraw(false);
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.layout_qr_code_scanner, this);
        FrameLayout frameLayout = (FrameLayout) relativeLayout.findViewById(R.id.qr_code_fl_scanner);
        mFrameRect = new Rect();
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) frameLayout.getLayoutParams();
        mFrameRect.left = (ScreenUtils.getScreenWidth(context) - layoutParams.width) / 2;
        mFrameRect.top = layoutParams.topMargin;
        mFrameRect.right = mFrameRect.left + layoutParams.width;
        mFrameRect.bottom = mFrameRect.top + layoutParams.height;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        Rect frame = mFrameRect;
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        drawFocusRect(canvas, frame);
        drawAngle(canvas, frame);
        drawText(canvas, frame);
        drawLaser(canvas, frame);

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }


    private void drawFocusRect(Canvas canvas, Rect rect) {
        int angleLength = mAngleLength;
        if (!showCorners) {
            angleLength = 0;
        }
        mPaint.setColor(mFrameColor);
        //Up
        canvas.drawRect(rect.left + angleLength, rect.top, rect.right - angleLength, rect.top + mFocusThick, mPaint);
        //Left
        canvas.drawRect(rect.left, rect.top + angleLength, rect.left + mFocusThick, rect.bottom - angleLength, mPaint);
        //Right
        canvas.drawRect(rect.right - mFocusThick, rect.top + angleLength, rect.right, rect.bottom - angleLength, mPaint);
        //Down
        canvas.drawRect(rect.left + angleLength, rect.bottom - mFocusThick, rect.right - angleLength, rect.bottom, mPaint);
    }

    /**
     * Draw four purple angles
     *
     * @param canvas
     * @param rect
     */
    private void drawAngle(Canvas canvas, Rect rect) {
        if (showCorners) {
            mPaint.setColor(mLaserColor);
            mPaint.setAlpha(OPAQUE);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(mAngleThick);
            int left = rect.left;
            int top = rect.top;
            int right = rect.right;
            int bottom = rect.bottom;
            // Top left angle
            canvas.drawRect(left, top, left + mAngleLength, top + mAngleThick, mPaint);
            canvas.drawRect(left, top, left + mAngleThick, top + mAngleLength, mPaint);
            // Top right angle
            canvas.drawRect(right - mAngleLength, top, right, top + mAngleThick, mPaint);
            canvas.drawRect(right - mAngleThick, top, right, top + mAngleLength, mPaint);
            // bottom left angle
            canvas.drawRect(left, bottom - mAngleLength, left + mAngleThick, bottom, mPaint);
            canvas.drawRect(left, bottom - mAngleThick, left + mAngleLength, bottom, mPaint);
            // bottom right angle
            canvas.drawRect(right - mAngleLength, bottom - mAngleThick, right, bottom, mPaint);
            canvas.drawRect(right - mAngleThick, bottom - mAngleLength, right, bottom, mPaint);
        }
    }

    private void drawText(Canvas canvas, Rect rect) {
        if (showText) {
            int margin = 40;
            mPaint.setColor(mTextColor);
            mPaint.setTextSize(getResources().getDimension(R.dimen.text_size_13sp));
            String text = getResources().getString(R.string.qr_code_auto_scan_notification);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
            float offY = fontTotalHeight / 2 - fontMetrics.bottom;
            float newY = rect.bottom + margin + offY;


            float screenScale = mContext.getResources().getDisplayMetrics().density;

            float left = (ScreenUtils.getScreenWidth(mContext) - (mPaint.getTextSize()) * text.length()) / 2;
            /*
                correctedLeft is hack to force the text in the middle of the width of the screen
                55 is an experimental value and it takes into account the scale of the screen.
             */
            float correctedLeft = left + (55 * screenScale);
            canvas.drawText(text, correctedLeft, newY, mPaint);
        }
    }

    private void drawLaser(Canvas canvas, Rect rect) {
        if (showLaser) {
            mPaint.setColor(mLaserColor);
            mPaint.setAlpha(SCANNER_ALPHA[mScannerAlpha]);
            mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.length;
            int middle = rect.height() / 2 + rect.top;
            canvas.drawRect(rect.left + 2, middle - 1, rect.right - 1, middle + 2, mPaint);
        }
    }
}

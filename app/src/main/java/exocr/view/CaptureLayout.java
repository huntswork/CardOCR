package exocr.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;

public final class CaptureLayout extends FrameLayout {

    public CaptureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**********************************************************************************************/

    private final DashPathEffect mDashPathEffect = new DashPathEffect(new float[]{20f, 10f}, 0);
    private final int[] laserAlpha = {0, 64, 128, 192, 255, 192, 128, 64};
    private int laserAlphaIndex = 0;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        final int canvasHeight = canvas.getHeight();
        final int canvasWidth = canvas.getWidth();
        Log.e("kalu1", "canvasHeight = " + canvasHeight + ", canvasWidth = " + canvasWidth);

        final Paint mPaint = new Paint();

        mPaint.clearShadowLayer();
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor("#cccccc"));
        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        final float stroke = 2 * metrics.density;
        mPaint.setStrokeWidth(stroke);
        mPaint.setPathEffect(mDashPathEffect);

        if (canvasWidth < canvasHeight) {

            final float layerWidth = canvasWidth * 0.9f;
            final float layerHeight = layerWidth / 1.6f;
            final float layerLeft = (canvasWidth - layerWidth) / 2;
            final float layerTop = (canvasHeight - layerHeight) / 2;
            final float layerRight = layerLeft + layerWidth;
            final float layerBottom = layerTop + layerHeight;
            // step1
            canvas.drawRect(layerLeft, layerTop, layerRight, layerBottom, mPaint);
            // step2
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#66666666"));
            canvas.drawRect(0, 0, layerLeft - stroke / 2, canvasHeight, mPaint);
            canvas.drawRect(layerRight + stroke / 2, 0, canvasWidth, canvasHeight, mPaint);
            // step3
            canvas.drawRect(layerLeft - stroke / 2, 0, layerRight + stroke / 2, layerTop - stroke / 2, mPaint);
            canvas.drawRect(layerLeft - stroke / 2, layerBottom + stroke / 2, layerRight + stroke / 2, canvasHeight, mPaint);
            // step4
            mPaint.setColor(Color.RED);
            mPaint.setAlpha(laserAlpha[laserAlphaIndex]);
            mPaint.setPathEffect(mDashPathEffect);
            laserAlphaIndex = (laserAlphaIndex + 1) % laserAlpha.length;
            final int left = (int) (layerLeft + stroke / 2);
            final int top = (int) (canvasHeight / 2 - stroke / 2);
            final int right = (int) (layerRight - stroke / 2);
            final int bottom = (int) (canvasHeight / 2 + stroke / 2);
            canvas.drawRect(left, top, right, bottom, mPaint);
            // postInvalidateDelayed(100, left, top, right, bottom);

        } else {

            final float layerHeight = canvasHeight * 0.9f;
            final float layerWidth = layerHeight * 1.6f;
            final float layerLeft = (canvasWidth - layerWidth) / 2;
            final float layerTop = (canvasHeight - layerHeight) / 2;
            final float layerRight = layerLeft + layerWidth;
            final float layerBottom = layerTop + layerHeight;
            // step1
            canvas.drawRect(layerLeft, layerTop, layerRight, layerBottom, mPaint);
            // step2
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#66666666"));
            canvas.drawRect(0, 0, layerLeft - stroke / 2, canvasHeight, mPaint);
            canvas.drawRect(layerRight + stroke / 2, 0, canvasWidth, canvasHeight, mPaint);
            // step3
            canvas.drawRect(layerLeft - stroke / 2, 0, layerRight + stroke / 2, layerTop - stroke / 2, mPaint);
            canvas.drawRect(layerLeft - stroke / 2, layerBottom + stroke / 2, layerRight + stroke / 2, canvasHeight, mPaint);
            // step4
            mPaint.setColor(Color.RED);
            mPaint.setAlpha(laserAlpha[laserAlphaIndex]);
            mPaint.setPathEffect(mDashPathEffect);
            laserAlphaIndex = (laserAlphaIndex + 1) % laserAlpha.length;
            final int left = (int) (layerLeft + stroke / 2);
            final int top = (int) (canvasHeight / 2 - stroke / 2);
            final int right = (int) (layerRight - stroke / 2);
            final int bottom = (int) (canvasHeight / 2 + stroke / 2);
            canvas.drawRect(left, top, right, bottom, mPaint);
            // postInvalidateDelayed(1000, left, top, right, bottom);
        }
    }

    /**********************************************************************************************/

    @Override
    public void setBackground(Drawable background) {
    }

    @Override
    public void setBackgroundColor(int color) {
    }

    @Override
    public void setBackgroundResource(int resid) {
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
    }

    @Override
    public void setBackgroundTintList(@Nullable ColorStateList tint) {
    }

    @Override
    public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    }

    /**********************************************************************************************/
}
    

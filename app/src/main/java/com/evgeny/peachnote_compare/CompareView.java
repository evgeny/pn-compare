package com.evgeny.peachnote_compare;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


/**
 * TODO: document your custom view class.
 */
public class CompareView extends View {
    private Paint mSegmentPaint;

    MainActivity.Alignment alignment;
    private ArrayList<Bitmap> mBitmapBars;
    private Map<String, double[][]> gradients;

    public CompareView(Context context) {
        super(context);
        init(null, 0);
    }

    public CompareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CompareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CompareView, defStyle, 0);

        a.recycle();

        mSegmentPaint = new Paint();

        // set rects
        invalidateSegments();
    }

    private void invalidateSegments() {
        System.out.println("invalidateSegments()");
        mBitmapBars = new ArrayList<>();
        if (alignment == null) return;

        double[] scoreTimeAxis = alignment.localTimeMaps[0][0];
        double[] videoSegmentAxis = alignment.localTimeMaps[0][1];

//        float points[] = new float[videoSegmentAxis.length];
//        for (int i = 0; i < videoSegmentAxis.length; i++) {
//            points[i] = (float) videoSegmentAxis[i];
//        }
        System.out.println("videoSegmentAxis= " + Arrays.toString(videoSegmentAxis));

        //segment bat height
        int height = 50;

        //orignial lines
        ArrayList<int[]> scoreColors = getGradientColors();

        for (int[] colors : scoreColors) {
            // bar bitmap array
            int cc[] = new int[height * colors.length];

            /**
             * make the bar the {@link height} px height by copying the original line {@link height} times
             */
            for (int i = 0; i < height; i++) {
                System.arraycopy(colors, 0, cc, i * colors.length, colors.length);
            }

            // create bitmap
            mBitmapBars.add(Bitmap.createBitmap(cc, colors.length, height, Bitmap.Config.ARGB_8888));
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmapBars.size() == 0) return;

        int top = 0;
        for (Bitmap bitmapBar : mBitmapBars) {
            canvas.drawBitmap(bitmapBar, 0, top, mSegmentPaint);
            top += bitmapBar.getHeight() + 25;
        }
    }

    /**
     * Set pixel colors differ by opacity.
     *
     * @return colors array
     */
    private ArrayList<int[]> getGradientColors() {
        ArrayList<int[]> scoreColors = new ArrayList<>();

        for (Map.Entry<String, double[][]> entry : gradients.entrySet()) {
            double[][] segmentGradient = entry.getValue();

            ArrayList<Integer> colors = new ArrayList<>();
            for (int j = 0; j < segmentGradient.length; j++) {
                for (int i = 0; i < segmentGradient[0].length; i++) {
                    // FIXME adjust bar width on view width
                    colors.add(Color.argb((int) (segmentGradient[0][i] * 100), 255, 0, 0));
                    colors.add(Color.argb((int) (segmentGradient[0][i] * 100), 255, 0, 0));
                    colors.add(Color.argb((int) (segmentGradient[0][i] * 100), 255, 0, 0));
                    colors.add(Color.argb((int) (segmentGradient[0][i] * 100), 255, 0, 0));
                }
            }

            scoreColors.add(Ints.toArray(colors));
        }

        return scoreColors;
    }

    public void setAlignments(MainActivity.Alignment alignment) {
        this.alignment = alignment;
        invalidateSegments();
    }

    public void setGradients(Map<String, double[][]> gradients) {
        this.gradients = gradients;
    }
}

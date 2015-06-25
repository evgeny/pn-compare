package com.evgeny.peachnote_compare;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


/**
 * TODO: document your custom view class.
 */
public class CompareView extends View {
    private Paint mSegmentPaint;

    private ArrayList<Rect> mRow;

    //    Map<String, MainActivity.Alignment> alignments;
    MainActivity.Alignment alignment;
    private Bitmap bitmap;
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
//        mSegmentPaint.setColor(Color.CYAN);
        mRow = new ArrayList<>();

        if (alignment == null) return;

//        MainActivity.Alignment alignment = alignments.get("");
        double[] scoreTimeAxis = alignment.localTimeMaps[0][0];
        double[] videoSegmentAxis = alignment.localTimeMaps[0][1];

        Rect rect = new Rect((int) videoSegmentAxis[0], 50, (int) videoSegmentAxis[videoSegmentAxis.length - 1], 150);
        mRow.add(rect);

        float points[] = new float[videoSegmentAxis.length];
        for (int i = 0; i < videoSegmentAxis.length; i++) {
            points[i] = (float) videoSegmentAxis[i];
        }
        System.out.println("videoSegmentAxis= " + Arrays.toString(videoSegmentAxis));
//        Shader shader = new LinearGradient(0, 0, 0, 100, getGradientColors(points.length), points, Shader.TileMode.MIRROR);
//        Matrix matrix = new Matrix();
//        matrix.setRotate(90);
//        shader.setLocalMatrix(matrix);
//
//        mSegmentPaint.setShader(shader);

        //segment bat height
        int height = 100;

        //orignial line
        int[] colors = getGradientColors();

        // bar bitmap array
        int cc[] = new int[height * colors.length];

        /**
         * make the bar the {@link height} px height by copying the original line {@link height} times
         */
        for (int i = 0; i < height; i++) {
            System.arraycopy(colors, 0, cc, i * colors.length, colors.length);
        }

        // create bitmap
        bitmap = Bitmap.createBitmap(cc, colors.length, height, Bitmap.Config.ARGB_8888);

//        Rect newRectangle = new Rect(10, 10, 50, 50);
//        mRow.add(newRectangle);
//
//        rect = new Rect(110, 10, 150, 50);
//        mRow.add(rect);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, mSegmentPaint);
        }
    }

    /**
     * get gradient line for FIRST SEGMENT of FIRST video id
     *
     * @return
     */
    private int[] getGradientColors() {
        int[] colors = new int[0];
        for (Map.Entry<String, double[][]> entry : gradients.entrySet()) {
            double[][] segmentGradient = entry.getValue();

            colors = new int[segmentGradient[0].length];
            for (int i = 0; i < segmentGradient[0].length; i++) {
                colors[i] = Color.argb((int) (segmentGradient[0][i] * 100), 255, 0, 0);
            }
        }

        System.out.println("getGradientColors()=" + Arrays.toString(colors));
        return colors;
    }

    public void setAlignments(MainActivity.Alignment alignment) {
        this.alignment = alignment;
        invalidateSegments();
    }

    public void setGradients(Map<String, double[][]> gradients) {
        this.gradients = gradients;
    }
}

package com.evgeny.peachnote_compare;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * TODO: document your custom view class.
 */
public class CompareView extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private Paint mSegmentPaint;
    private float mTextWidth;
    private float mTextHeight;

    private ArrayList<Rect> mRow;

//    Map<String, MainActivity.Alignment> alignments;
    MainActivity.Alignment alignment;

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

        mExampleString = a.getString(
                R.styleable.CompareView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.CompareView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.CompareView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.CompareView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.CompareView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mSegmentPaint = new Paint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        // set rects
        invalidateSegments();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
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
        Shader shader = new LinearGradient(0, 0, 0, 100, getGradientColors(points.length), points, Shader.TileMode.MIRROR);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        shader.setLocalMatrix(matrix);

        mSegmentPaint.setShader(shader);

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

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }

//        System.out.println("onDraw");
        for(Rect rect : mRow) {
//            System.out.println("draw rect of size=" + rect.left + ", " + rect.right);
            canvas.drawRect(rect, mSegmentPaint);
        }
    }

    private int[] getGradientColors(int length) {
        int[] colors = new int[length];
        int color1 = getResources().getColor(android.R.color.holo_red_light);
        int color2 = getResources().getColor(android.R.color.holo_green_dark);
        for (int i=0; i<length; i+=2) {
            colors[i] = color1;
            colors[i+1] = color2;
        }

        System.out.println("getGradientColors()=" + Arrays.toString(colors));
        return colors;
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    public void setAlignments(MainActivity.Alignment alignment) {
        this.alignment = alignment;
        invalidateSegments();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}

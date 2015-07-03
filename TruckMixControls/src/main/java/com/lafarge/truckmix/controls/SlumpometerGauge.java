package com.lafarge.truckmix.controls;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SlumpometerGauge extends View {

    private static final String TAG = SlumpometerGauge.class.getSimpleName();

    public static final double DEFAULT_MAX_SPEED = 100.0;
    public static final double DEFAULT_MAJOR_TICK_STEP = 20.0;
    public static final int DEFAULT_MINOR_TICKS = 1;
    public static final int DEFAULT_LABEL_TEXT_SIZE_DP = 12;

    private double maxSpeed = DEFAULT_MAX_SPEED;
    private double speed = 0;
    private int defaultColor = Color.rgb(0, 0, 0);//Color.rgb(180, 180, 180);
    private double majorTickStep = DEFAULT_MAJOR_TICK_STEP;
    private int minorTicks = DEFAULT_MINOR_TICKS;
    private LabelConverter labelConverter;

    private List<ColoredRange> ranges = new ArrayList<ColoredRange>();

    private Paint backgroundPaint;
    private Paint backgroundMaskPaint;
    private Paint backgroundConcretPaint;
    private Paint backgroundTolerancePaint;
    private Paint maskPaint;
    private Paint needlePaint;
//    private Paint ticksPaint;
    private Paint txtPaint;
//    private Paint colorLinePaint;
    private int labelTextSize;

//    private Bitmap mMask;

    public SlumpometerGauge(Context context) {
        super(context);
        init();

        float density = getResources().getDisplayMetrics().density;
        setLabelTextSize(Math.round(DEFAULT_LABEL_TEXT_SIZE_DP * density));
    }

    public SlumpometerGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        float density = getResources().getDisplayMetrics().density;
        setLabelTextSize(Math.round(DEFAULT_LABEL_TEXT_SIZE_DP * density));
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        if (maxSpeed <= 0)
            throw new IllegalArgumentException("Non-positive value specified as max speed.");
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0)
            throw new IllegalArgumentException("Non-positive value specified as a speed.");
        if (speed > maxSpeed)
            speed = maxSpeed;
        this.speed = speed;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double speed, long duration, long startDelay) {
        if (speed < 0)
            throw new IllegalArgumentException("Negative value specified as a speed.");

        if (speed > maxSpeed)
            speed = maxSpeed;

        ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + fraction * (endValue - startValue);
            }
        }, getSpeed(), speed);

        va.setDuration(duration);
        va.setStartDelay(startDelay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Double value = (Double) animation.getAnimatedValue();
                if (value != null)
                    setSpeed(value);
            }
        });
        va.start();
        return va;
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, boolean animate) {
        return setSpeed(progress, 1500, 200);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        invalidate();
    }

    public double getMajorTickStep() {
        return majorTickStep;
    }

    public void setMajorTickStep(double majorTickStep) {
        if (majorTickStep <= 0)
            throw new IllegalArgumentException("Non-positive value specified as a major tick step.");
        this.majorTickStep = majorTickStep;
        invalidate();
    }

    public int getMinorTicks() {
        return minorTicks;
    }

    public void setMinorTicks(int minorTicks) {
        this.minorTicks = minorTicks;
        invalidate();
    }

    public LabelConverter getLabelConverter() {
        return labelConverter;
    }

    public void setLabelConverter(LabelConverter labelConverter) {
        this.labelConverter = labelConverter;
        invalidate();
    }

    public void clearColoredRanges() {
        ranges.clear();
        invalidate();
    }

    public void addColoredRange(double begin, double end, int color) {
        if (begin >= end)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (begin < - 5.0/160* maxSpeed)
            begin = - 5.0/160* maxSpeed;
        if (end > maxSpeed * (5.0/160 + 1))
            end = maxSpeed * (5.0/160 + 1);
        ranges.add(new ColoredRange(color, begin, end));
        invalidate();
    }

    public int getLabelTextSize() {
        return labelTextSize;
    }

    public void setLabelTextSize(int labelTextSize) {
        this.labelTextSize = labelTextSize;
        if (txtPaint != null) {
            txtPaint.setTextSize(labelTextSize);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        // Draw Metallic Arc and background
        drawBackground(canvas);

        // Draw Ticks and colored arc
        drawTicks(canvas);

        // Draw Needle
        drawNeedle(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            width = widthSize;
        } else {
            width = -1;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            height = heightSize;
        } else {
            height = -1;
        }

        if (height >= 0 && width >= 0) {
            width = Math.min(height, width);
            height = width/2;
        } else if (width >= 0) {
            height = width/2;
        } else if (height >= 0) {
            width = height*2;
        } else {
            width = 0;
            height = 0;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    private void drawNeedle(Canvas canvas) {
        RectF oval = getOval(canvas, 1);
        float radius = oval.width()*0.35f + 10;
        RectF smallOval = getOval(canvas, 0.2f);

        float angle = 10 + (float) (getSpeed()/ getMaxSpeed()*160);
        canvas.drawLine(
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * smallOval.width()*0.5f),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * smallOval.width()*0.5f),
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius)),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius)),
                needlePaint
        );

//        canvas.drawArc(smallOval, 180, 180, true, backgroundPaint);
    }

    private void drawTicks(Canvas canvas) {
        float availableAngle = 160;
        float majorStep = (float) (majorTickStep/ maxSpeed *availableAngle);
        float minorStep = majorStep / (1 + minorTicks);

        float majorTicksLength = 30;
        float minorTicksLength = majorTicksLength/2;

        RectF oval = getOval(canvas, 1);
        float radius = oval.width()*0.35f;

        float currentAngle = 10;
        double curProgress = 0;
        while (currentAngle <= 170) {

//            canvas.drawLine(
//                    (float) (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius-majorTicksLength/2)),
//                    (float) (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius-majorTicksLength/2)),
//                    (float) (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius+majorTicksLength/2)),
//                    (float) (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius+majorTicksLength/2)),
//                    ticksPaint
//            );

            for (int i=1; i<=minorTicks; i++) {
                float angle = currentAngle + i*minorStep;
                if (angle >= 170 + minorStep/2) {
                    break;
                }
//                canvas.drawLine(
//                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius),
//                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * radius),
//                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius + minorTicksLength)),
//                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius + minorTicksLength)),
//                        ticksPaint
//                );
            }

            if (labelConverter != null) {

                canvas.save();
                canvas.rotate(180 + currentAngle, oval.centerX(), oval.centerY());
                float txtX = oval.centerX() + radius + majorTicksLength/2 + 8;
                float txtY = oval.centerY();
                canvas.rotate(+90, txtX, txtY);
                canvas.drawText(labelConverter.getLabelFor(curProgress, maxSpeed), txtX, txtY, txtPaint);
                canvas.restore();
            }

            currentAngle += majorStep;
            curProgress += majorTickStep;
        }

//        RectF smallOval = getOval(canvas, 0.7f);
//        colorLinePaint.setColor(defaultColor);
//        canvas.drawArc(smallOval, 185, 170, false, colorLinePaint);
//
//        for (ColoredRange range: ranges) {
//            colorLinePaint.setColor(range.getColor());
//            canvas.drawArc(smallOval, (float) (190 + range.getBegin()/ maxSpeed *160), (float) ((range.getEnd() - range.getBegin())/ maxSpeed *160), false, colorLinePaint);
//        }
    }

    private RectF getOval(Canvas canvas, float factor) {
        RectF oval;
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        if (canvasHeight*2 >= canvasWidth) {
            oval = new RectF(0, 0, canvasWidth*factor, canvasWidth*factor);
        } else {
            oval = new RectF(0, 0, canvasHeight*2*factor, canvasHeight*2*factor);
        }

        oval.offset((canvasWidth-oval.width())/2 + getPaddingLeft(), (canvasHeight*2-oval.height())/2 + getPaddingTop());

        return oval;
    }

    private void drawBackground(Canvas canvas) {
        final int concreteTypeNb = 5;

        final int concreteType = 4;
        final int tolerance = 10;
        // TODO: Check 1 >= concreteType <= 5

        final float toleranceAngle = (tolerance * 180) / 300;
        final float concreteAngle = 180 / concreteTypeNb;

        RectF oval = getOval(canvas, 1.0f);
        canvas.drawArc(oval, 180, 180, true, backgroundPaint);

        RectF toleranceOval = getOval(canvas, 1.0f);
        canvas.drawArc(toleranceOval, 180 + ((concreteType - 1) * (concreteAngle)), concreteAngle + (2 * toleranceAngle), true, backgroundTolerancePaint);

        RectF concreteOval = getOval(canvas, 1.0f);
        canvas.drawArc(concreteOval, 180 + ((concreteType - 1) * concreteAngle) + toleranceAngle, concreteAngle, true, backgroundConcretPaint);

        RectF maskOval = getOval(canvas, 0.6f);
        canvas.drawArc(maskOval , 180, 180, true, backgroundMaskPaint);

//        Bitmap mask = Bitmap.createScaledBitmap(mMask, (int) (oval.width() * 1.1), (int) (oval.height() * 1.1) / 2, true);
//        canvas.drawBitmap(mask, oval.centerX() - oval.width()*1.1f/2, oval.centerY()-oval.width()*1.1f/2, maskPaint);
    }

    @SuppressWarnings("NewApi")
    private void init() {
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.rgb(235, 242, 244));

        backgroundMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundMaskPaint.setStyle(Paint.Style.FILL);
        backgroundMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        backgroundMaskPaint.setColor(Color.rgb(255, 255, 255));

        backgroundConcretPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundConcretPaint.setStyle(Paint.Style.FILL);
        backgroundConcretPaint.setColor(Color.rgb(0, 168, 110));

        backgroundTolerancePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundTolerancePaint.setStyle(Paint.Style.FILL);
        backgroundTolerancePaint.setColor(Color.rgb(255, 220, 32));

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(labelTextSize);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setLinearText(true);

//        mMask = BitmapFactory.decodeResource(getResources(), R.drawable.m);
//        mMask = Bitmap.createBitmap(mMask, 0, 0, mMask.getWidth(), mMask.getHeight() / 2);

//        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        maskPaint.setDither(true);

//        ticksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        ticksPaint.setStrokeWidth(3.0f);
//        ticksPaint.setStyle(Paint.Style.STROKE);
//        ticksPaint.setColor(defaultColor);

//        colorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        colorLinePaint.setStyle(Paint.Style.STROKE);
//        colorLinePaint.setStrokeWidth(5);
//        colorLinePaint.setColor(defaultColor);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStrokeWidth(5);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setColor(Color.argb(200, 255, 0, 0));
    }


    public static interface LabelConverter {

        String getLabelFor(double progress, double maxProgress);

    }

    public static class ColoredRange {

        private int color;
        private double begin;
        private double end;

        public ColoredRange(int color, double begin, double end) {
            this.color = color;
            this.begin = begin;
            this.end = end;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public double getBegin() {
            return begin;
        }

        public void setBegin(double begin) {
            this.begin = begin;
        }

        public double getEnd() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }
    }

}

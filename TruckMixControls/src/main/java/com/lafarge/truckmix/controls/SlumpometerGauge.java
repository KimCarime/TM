package com.lafarge.truckmix.controls;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class SlumpometerGauge extends View {

    public static final double MAX_SLUMP = 300.0;
    public static final double DEFAULT_MAJOR_TICK_STEP = 20.0;
    public static final int DEFAULT_MINOR_TICKS = 1;
    public static final int CONCRETE_RANGE_LABEL_TEXT_DIZE_DP = 12;
    public static final int CONCRETE_CODE_LABEL_TEXT_DIZE_DP = 18;
    public static final int CURRENT_SLUMP_LABEL_TEXT_DIZE_DP = 28;
    public static final int CURRENT_SLUMP_UNIT_LABEL_TEXT_DIZE_DP = 12;

    private static final int SLUMP_NORMAL_COLOR = Color.BLACK;
    private static final int SLUMP_OUTOFBOUND_COLOR = Color.rgb(206, 37, 32);

    private double speed = 0;
    private double majorTickStep = DEFAULT_MAJOR_TICK_STEP;
    private int minorTicks = DEFAULT_MINOR_TICKS;

    private int concreteRangeMin;
    private int concreteRangeMax;
    private int tolerance;
    private String concreteCode;

    private Paint backgroundPaint;
    private Paint backgroundMaskPaint;
    private Paint backgroundConcretPaint;
    private Paint backgroundTolerancePaint;
    private Paint needlePaint;
    private Paint needleBottomPaint;
    private Paint concreteRangeTextPaint;
    private Paint concreteCodeTextPaint;
    private Paint currentSlumpPaint;
    private Paint currentSlumpUnitPaint;

    //
    // Constructor
    //

    public SlumpometerGauge(Context context) {
        super(context);
        init();
    }

    public SlumpometerGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //
    // Configuration
    //

    public void setConcreteCode(String concreteCode) {
        this.concreteCode = concreteCode;
    }

    public void setConcreteRange(int min, int max) {
        if (min < 0 || max < 0)
            throw new IllegalArgumentException("Non-positive value for min or max concrete range.");
        if (min > MAX_SLUMP || max > MAX_SLUMP)
            throw new IllegalArgumentException("Value greater than MAX_SLUMP specified for min or max range.");
        if (min > max)
            throw new IllegalArgumentException("Min range value cannot be greater than Max range value.");
        this.concreteRangeMin = min;
        this.concreteRangeMax = max;
    }

    public void setTolerance(int tolerance) {
        if (tolerance < 0)
            throw new IllegalArgumentException("Non-positive value specified for tolerance.");
        this.tolerance = tolerance;
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

    //
    // Animation
    //

    public double getSlump() {
        return getSpeed();
    }

    public void setSlump(double slump) {
        setSpeed(slump);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0)
            throw new IllegalArgumentException("Non-positive value specified as a slump.");
        if (speed > MAX_SLUMP)
            speed = MAX_SLUMP;
        this.speed = speed;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double speed, long duration, long startDelay) {
        if (speed < 0)
            throw new IllegalArgumentException("Negative value specified as a slump.");

        if (speed > MAX_SLUMP)
            speed = MAX_SLUMP;

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

    //
    // Canvas
    //

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        // Draw background arc with concrete range and tolerance
        drawBackground(canvas);

        // Draw static text with concrete range and tolerance
        drawTicks(canvas);

        // Draw Needle and current slump
        drawNeedleAndCurrentSlump(canvas);
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

    private void drawNeedleAndCurrentSlump(Canvas canvas) {
        RectF oval = getOval(canvas, 1.0f);
        float radius = oval.width()*0.33f;
        RectF smallOval = getOval(canvas, 0.35f);
        RectF smallOvalMask = getOval(canvas, 0.33f);
        smallOvalMask.left = smallOval.left;
        smallOvalMask.right = smallOval.right;
        smallOvalMask.inset(-1, 0);
        final float triangleOffset = 3;

        double slump = getSpeed();

        float angle = 10 + (float) (slump / MAX_SLUMP * 160);

        // Sorry future me or whoever you are...
        float startLeftX = (float) (oval.centerX() + Math.cos((180 - angle - triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
        float startRightX = (float) (oval.centerX() + Math.cos((180 - angle + triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
        float startLeftY = (float) (oval.centerY() + 10.f - Math.sin((angle + triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
        float startRightY = (float) (oval.centerY() + 10.f - Math.sin((angle - triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
        float endX = (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius));
        float endY = (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius));

        Point point1_draw = new Point(Math.round(startLeftX), Math.round(startLeftY));
        Point point2_draw = new Point(Math.round(endX), Math.round(endY));
        Point point3_draw = new Point(Math.round(startRightX), Math.round(startRightY));

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1_draw.x, point1_draw.y);
        path.lineTo(point2_draw.x, point2_draw.y);
        path.lineTo(point3_draw.x, point3_draw.y);
        path.lineTo(point1_draw.x, point1_draw.y);
        path.close();

        canvas.drawPath(path, needlePaint);
        canvas.drawOval(smallOval, needleBottomPaint);
        canvas.drawOval(smallOvalMask, backgroundMaskPaint);

        if (slump < concreteRangeMin - tolerance || slump > concreteRangeMax + tolerance) {
            currentSlumpPaint.setColor(SLUMP_OUTOFBOUND_COLOR);
            currentSlumpUnitPaint.setColor(SLUMP_OUTOFBOUND_COLOR);
        } else {
            currentSlumpPaint.setColor(SLUMP_NORMAL_COLOR);
            currentSlumpUnitPaint.setColor(SLUMP_NORMAL_COLOR);
        }
        canvas.drawText(String.format("%.0f", slump), oval.centerX(), smallOval.centerY() - 40.f, currentSlumpPaint);
        canvas.drawText("mm", oval.centerX(), smallOval.centerY(), currentSlumpUnitPaint);
    }

    private void drawTicks(Canvas canvas) {
        float majorTicksLength = 30;

        RectF oval = getOval(canvas, 1.f);
        float radius = oval.width()*0.35f;

        final float startConcreteAngle = 180 * concreteRangeMin / (float)MAX_SLUMP;
        final float endConcreteAngle = 180 * concreteRangeMax / (float)MAX_SLUMP;

        float txtX = oval.centerX() + radius + majorTicksLength/2 + 8;
        float txtY = oval.centerY();
        float offset = 115.f;

        canvas.save();
        canvas.rotate(180 + startConcreteAngle, oval.centerX(), oval.centerY());
        canvas.rotate(+90, txtX, txtY);
        canvas.drawText(String.format("%d", concreteRangeMin), txtX, txtY - offset, concreteRangeTextPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(180 + endConcreteAngle, oval.centerX(), oval.centerY());
        canvas.rotate(+90, txtX, txtY);
        canvas.drawText(String.format("%d", concreteRangeMax), txtX, txtY - offset, concreteRangeTextPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(180 + (startConcreteAngle + endConcreteAngle) * 0.5f, oval.centerX(), oval.centerY());
        canvas.rotate(+90, txtX, txtY);
        canvas.drawText(concreteCode, txtX, txtY, concreteCodeTextPaint);
        canvas.restore();
    }

    private RectF getOval(Canvas canvas, float factor) {
        RectF oval;
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        if (canvasHeight*2 >= canvasWidth) {
            oval = new RectF(0, 15, canvasWidth*factor, canvasWidth*factor);
        } else {
            oval = new RectF(0, 15, canvasHeight*2*factor, canvasHeight*2*factor);
        }

        oval.offset((canvasWidth-oval.width())/2 + getPaddingLeft(), (canvasHeight*2-oval.height())/2 + getPaddingTop());

        return oval;
    }

    private void drawBackground(Canvas canvas) {
        final float startConcreteAngle = 180 * concreteRangeMin / (float)MAX_SLUMP;
        final float endConcreteAngle = (180 * concreteRangeMax / (float)MAX_SLUMP) - startConcreteAngle;

        RectF oval = getOval(canvas, 1.0f);
        canvas.drawArc(oval, 180, 180, true, backgroundPaint);

        if (tolerance > 0) {
            final float toleranceAngle = (180 * tolerance) / (float)MAX_SLUMP;
            RectF toleranceOval = getOval(canvas, 1.0f);
            canvas.drawArc(toleranceOval, 180 + startConcreteAngle - toleranceAngle, endConcreteAngle + 2*toleranceAngle, true, backgroundTolerancePaint);
        }

        RectF concreteOval = getOval(canvas, 1.0f);
        canvas.drawArc(concreteOval, 180 + startConcreteAngle, endConcreteAngle, true, backgroundConcretPaint);

        RectF maskOval = getOval(canvas, 0.6f);
        canvas.drawArc(maskOval , 180, 180, true, backgroundMaskPaint);
    }

    //
    // Private stuff
    //

    @SuppressWarnings("NewApi")
    private void init() {
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        final float density = getResources().getDisplayMetrics().density;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.rgb(235, 242, 244));

        backgroundMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundMaskPaint.setStyle(Paint.Style.FILL);
        backgroundMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        backgroundConcretPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundConcretPaint.setStyle(Paint.Style.FILL);
        backgroundConcretPaint.setColor(Color.rgb(0, 168, 110));

        backgroundTolerancePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundTolerancePaint.setStyle(Paint.Style.FILL);
        backgroundTolerancePaint.setColor(Color.rgb(255, 220, 32));

        needleBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needleBottomPaint.setStyle(Paint.Style.FILL);
        needleBottomPaint.setColor(Color.BLACK);

        concreteRangeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        concreteRangeTextPaint.setColor(Color.BLACK);
        concreteRangeTextPaint.setTextSize(Math.round(CONCRETE_RANGE_LABEL_TEXT_DIZE_DP * density));
        concreteRangeTextPaint.setTextAlign(Paint.Align.CENTER);
        concreteRangeTextPaint.setLinearText(true);

        concreteCodeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        concreteCodeTextPaint.setColor(Color.WHITE);
        concreteCodeTextPaint.setTypeface(Typeface.create("Helvetica", Typeface.BOLD));
        concreteCodeTextPaint.setTextSize(Math.round(CONCRETE_CODE_LABEL_TEXT_DIZE_DP * density));
        concreteCodeTextPaint.setTextAlign(Paint.Align.CENTER);
        concreteCodeTextPaint.setLinearText(true);

        currentSlumpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentSlumpPaint.setColor(SLUMP_NORMAL_COLOR);
        currentSlumpPaint.setTypeface(Typeface.create("Helvetica", Typeface.BOLD));
        currentSlumpPaint.setTextSize(Math.round(CURRENT_SLUMP_LABEL_TEXT_DIZE_DP * density));
        currentSlumpPaint.setTextAlign(Paint.Align.CENTER);
        currentSlumpPaint.setLinearText(true);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(Color.BLACK);
        currentSlumpUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentSlumpUnitPaint.setColor(SLUMP_NORMAL_COLOR);
        currentSlumpUnitPaint.setTextSize(Math.round(CURRENT_SLUMP_UNIT_LABEL_TEXT_DIZE_DP * density));
        currentSlumpUnitPaint.setTextAlign(Paint.Align.CENTER);
        currentSlumpUnitPaint.setLinearText(true);
    }
}

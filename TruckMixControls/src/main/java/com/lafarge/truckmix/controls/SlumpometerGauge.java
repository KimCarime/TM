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
    public static final int CONCRETE_RANGE_LABEL_TEXT_DIZE_DP = 13;
    public static final int CONCRETE_CODE_LABEL_TEXT_DIZE_DP = 18;
    public static final int CURRENT_SLUMP_LABEL_TEXT_DIZE_DP = 28;
    public static final int CURRENT_SLUMP_UNIT_LABEL_TEXT_DIZE_DP = 12;

    private static final int BACKGROUND_COLOR = Color.rgb(235, 242, 244);
    private static final int NEEDLE_COLOR = Color.BLACK;
    private static final int SLUMP_NORMAL_COLOR = Color.BLACK;
    private static final int SLUMP_OUTOFBOUND_COLOR = Color.rgb(206, 37, 32);
    private static final int BACKGROUND_CONTRETE = Color.rgb(0, 168, 110);
    private static final int BACKGROUND_TOLERANCE = Color.rgb(255, 220, 32);

    private double slump = -1;

    private int concreteRangeMin;
    private int concreteRangeMax;
    private int tolerance;
    private String concreteCode = "";

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
        invalidate();
    }

    public String getConcreteCode() {
        return concreteCode;
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
        invalidate();
    }

    public int getConcreteRangeMin() {
        return concreteRangeMin;
    }

    public int getConcreteRangeMax() {
        return concreteRangeMax;
    }

    public void setTolerance(int tolerance) {
        if (tolerance < 0)
            throw new IllegalArgumentException("Non-positive value specified for tolerance.");
        this.tolerance = tolerance;
        invalidate();
    }

    public int getTolerance() {
        return tolerance;
    }

    public boolean isSlumpOutOfRange(double slump) {
        return slump < concreteRangeMin - tolerance || slump > concreteRangeMax + tolerance;
    }

    //
    // Animation
    //

    public double getSlump() {
        return slump;
    }

    public void setSlump(double slump) {
        if (slump < 0)
            throw new IllegalArgumentException("Non-positive value specified as a slump.");
        if (slump > MAX_SLUMP)
            slump = MAX_SLUMP;
        this.slump = slump;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setSlump(double slump, long duration, long startDelay) {
        if (slump < 0)
            throw new IllegalArgumentException("Negative value specified as a slump.");
        if (slump > MAX_SLUMP)
            slump = MAX_SLUMP;

        ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + fraction * (endValue - startValue);
            }
        }, getSlump() < 0 ? 0 : getSlump(), slump);

        va.setDuration(duration);
        va.setStartDelay(startDelay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Double value = (Double) animation.getAnimatedValue();
                if (value != null)
                    setSlump(value);
            }
        });
        va.start();
        return va;
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
        RectF smallOval = getOval(canvas, 0.38f);
        RectF smallOvalMask = getOval(canvas, 0.36f);
        smallOvalMask.left = smallOval.left;
        smallOvalMask.right = smallOval.right;
        smallOvalMask.inset(-1, 0);
        final float triangleOffset = 3;

        double slump = getSlump();
        boolean noSlumpReceivedYet = (slump < 0);

        float angle = (float) (slump / MAX_SLUMP * 180);

        // Sorry future me or whoever you are...
        float startLeftX = (float) (oval.centerX() + Math.cos((180 - angle - triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
        float startRightX = (float) (oval.centerX() + Math.cos((180 - angle + triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
        float startLeftY = (float) (oval.centerY() - Math.sin((angle + triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
        float startRightY = (float) (oval.centerY() - Math.sin((angle - triangleOffset) / 180 * Math.PI) * smallOval.width() * 0.5f);
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

        String slumpString;
        if (noSlumpReceivedYet) {
            slumpString = "--";
        } else {
            slumpString = String.format("%.0f", slump);
            if (isSlumpOutOfRange(slump)) {
                currentSlumpPaint.setColor(SLUMP_OUTOFBOUND_COLOR);
                currentSlumpUnitPaint.setColor(SLUMP_OUTOFBOUND_COLOR);
            } else {
                currentSlumpPaint.setColor(SLUMP_NORMAL_COLOR);
                currentSlumpUnitPaint.setColor(SLUMP_NORMAL_COLOR);
            }
        }

        canvas.drawText(slumpString, oval.centerX(), (smallOval.top + smallOval.bottom) * 0.46f, currentSlumpPaint);
        canvas.drawText("mm", oval.centerX(), smallOval.centerY(), currentSlumpUnitPaint);
    }

    private void drawTicks(Canvas canvas) {
        RectF oval = getOval(canvas, 1.f);
        float rangeRadius = oval.width()*0.51f;
        float concreteCodeRadius = oval.width()*0.37f;

        final float startConcreteAngle = 180 * concreteRangeMin / (float)MAX_SLUMP;
        final float endConcreteAngle = 180 * concreteRangeMax / (float)MAX_SLUMP;

        canvas.save();
        canvas.rotate(180 + startConcreteAngle, oval.centerX(), oval.centerY());
        float txtX1 = oval.centerX() + rangeRadius;
        float txtY1 = oval.centerY();
        canvas.rotate(+90, txtX1, txtY1);
        canvas.drawText(String.format("%d", concreteRangeMin), txtX1, txtY1, concreteRangeTextPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(180 + endConcreteAngle, oval.centerX(), oval.centerY());
        float txtX2 = oval.centerX() + rangeRadius;
        float txtY2 = oval.centerY();
        canvas.rotate(+90, txtX2, txtY2);
        canvas.drawText(String.format("%d", concreteRangeMax), txtX2, txtY2, concreteRangeTextPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(180 + (startConcreteAngle + endConcreteAngle) * 0.5f, oval.centerX(), oval.centerY());
        float txtX3 = oval.centerX() + concreteCodeRadius;
        float txtY3 = oval.centerY();
        canvas.rotate(+90, txtX3, txtY3);
        canvas.drawText(concreteCode, txtX3, txtY3, concreteCodeTextPaint);
        canvas.restore();
    }

    private RectF getOval(Canvas canvas, float factor) {
        factor -= 0.05;
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
        backgroundPaint.setColor(BACKGROUND_COLOR);

        backgroundMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundMaskPaint.setStyle(Paint.Style.FILL);
        backgroundMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        backgroundConcretPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundConcretPaint.setStyle(Paint.Style.FILL);
        backgroundConcretPaint.setColor(BACKGROUND_CONTRETE);

        backgroundTolerancePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundTolerancePaint.setStyle(Paint.Style.FILL);
        backgroundTolerancePaint.setColor(BACKGROUND_TOLERANCE);

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

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(NEEDLE_COLOR);

        needleBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needleBottomPaint.setStyle(Paint.Style.FILL);
        needleBottomPaint.setColor(Color.BLACK);

        currentSlumpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentSlumpPaint.setColor(SLUMP_NORMAL_COLOR);
        currentSlumpPaint.setTypeface(Typeface.create("Helvetica", Typeface.BOLD));
        currentSlumpPaint.setTextSize(Math.round(CURRENT_SLUMP_LABEL_TEXT_DIZE_DP * density));
        currentSlumpPaint.setTextAlign(Paint.Align.CENTER);
        currentSlumpPaint.setLinearText(true);

        currentSlumpUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentSlumpUnitPaint.setColor(SLUMP_NORMAL_COLOR);
        currentSlumpUnitPaint.setTextSize(Math.round(CURRENT_SLUMP_UNIT_LABEL_TEXT_DIZE_DP * density));
        currentSlumpUnitPaint.setTextAlign(Paint.Align.CENTER);
        currentSlumpUnitPaint.setLinearText(true);
    }
}

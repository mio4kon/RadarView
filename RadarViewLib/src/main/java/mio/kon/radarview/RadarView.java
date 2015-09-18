package mio.kon.radarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;


import java.util.Map;



/**
 * Created by mio on 15-9-14.
 */
public class RadarView extends View implements Observer {

    private Paint linePaint;
    private int mRadius;
    private int mAbilityCount;
    private float mDegree;
    private RadarMap mAbility;
    private Paint textPaint;
    private int mTextBlank, mPadding, mHeightPadding, mWidthPadding;
    private Paint abilityPaint, abilityLinePaint;
    private String testPaddingString = "测试字体";
    private Path abilityPath;
    private Path edgePath;
    private boolean hasSetAbility = false;

    public RadarView(Context context) {
        this (context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        super (context, attrs);
        init (attrs);

    }


    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext ().obtainStyledAttributes (attrs, R.styleable.RadarView);
        int lineColor = typedArray
                .getColor (R.styleable.RadarView_lineColor, Color.parseColor ("#223344"));
        int textColor = typedArray
                .getColor (R.styleable.RadarView_textColor, Color.BLACK);
        int abilityColor = typedArray
                .getColor (R.styleable.RadarView_abilityColor, Color.GRAY);
        int abilityLineColor = typedArray
                .getColor (R.styleable.RadarView_abilityLineColor, Color.WHITE);
        float textSize = typedArray.getDimension (R.styleable.RadarView_textSize, 12);
        mRadius = (int) typedArray.getDimension (R.styleable.RadarView_radius, dip2px (100));
        //path
        edgePath = new Path ();
        abilityPath = new Path ();
        //paint
        linePaint = new Paint (Paint.STRIKE_THRU_TEXT_FLAG);
        linePaint.setColor (lineColor);
        linePaint.setStyle (Paint.Style.STROKE);
        textPaint = new Paint ();
        textPaint.setTextSize (textSize);
        textPaint.setColor (textColor);
        abilityPaint = new Paint ();
        abilityLinePaint = new Paint ();
        abilityLinePaint.setStyle (Paint.Style.STROKE);
        abilityLinePaint.setStrokeWidth (2);
        abilityLinePaint.setColor (abilityLineColor);
        abilityPaint.setStyle (Paint.Style.FILL);
        abilityPaint.setColor (abilityColor);
        mPadding = (int) textPaint.measureText (testPaddingString);
        mTextBlank = dip2px (5);
    }


    /**
     * ability 需要一个Map
     *      key      - 名称
     *      value    - 数值(0~100)
     *
     * @param ability 能力
     */
    public void setAbility(RadarMap ability) {
        if (ability == null || ability.size () <= 3) {
            throw new IllegalArgumentException (
                    "you must set your ability and make sure ability's size > 3");
        }
        mAbility = ability;
        if(!hasSetAbility){
            mAbility.registerObserver (this);
        }
        hasSetAbility = true;
        mAbilityCount = mAbility.size ();
        mDegree = 360f / mAbilityCount;
        postInvalidate ();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec
                .getSize (widthMeasureSpec) - getPaddingLeft () - getPaddingRight ();
        int heightSize = MeasureSpec
                .getSize (heightMeasureSpec) - getPaddingBottom () - getPaddingBottom ();
        int widthMode = MeasureSpec.getMode (widthMeasureSpec);
        int heightMode = MeasureSpec.getMode (heightMeasureSpec);
        int width;
        int height;
        int radius = mRadius;
        //2p+2r = w或h
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
            radius = width / 2 - mPadding;
        } else {
            width = mRadius * 2 + mPadding * 2;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
            radius = Math.min (height / 2 - mPadding, radius);
        } else {
            height = mRadius * 2 + mPadding * 2;
        }
        mRadius = radius;
        mWidthPadding = (width - 2 * mRadius) / 2;
        mHeightPadding = (height - 2 * mRadius) / 2;
        setMeasuredDimension (width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mAbility == null) {
            return;
        }
        canvas.save ();
        canvas.translate (mWidthPadding, mHeightPadding);
        canvas.rotate (-mDegree / 2f, mRadius, mRadius);  //保证尖部的不再上方
        drawInnerLine (canvas);
        drawEdge (canvas, mRadius);
        drawEdge (canvas, mRadius * 2 / 3);
        drawEdge (canvas, mRadius / 3);
        drawAbility (canvas, mAbility);
        canvas.restore ();

    }

    private void drawAbility(Canvas canvas, Map<String, Float> mAbility) {
        abilityPath.reset ();
        int i = 0;
        for(Map.Entry<String, Float> entry : mAbility.entrySet ()) {
            Float abilityValue = entry.getValue ();
            if (abilityValue < 0 || abilityValue > 100) {
                throw new IllegalArgumentException ("your map value must in [0,100] !");
            }
            String abilityText = entry.getKey ();
            float distance = (abilityValue / 100f) * mRadius;
            Point point = calculatePoint (i * mDegree, distance, mRadius);
            drawText (canvas, abilityText, calculatePoint (i * mDegree, mRadius, mRadius), i);
            if (i == 0) {
                abilityPath.moveTo (point.x, point.y);
            } else {
                abilityPath.lineTo (point.x, point.y);
            }

            if (i == mAbilityCount - 1) {
                abilityPath.close ();
                canvas.drawPath (abilityPath, abilityPaint);
                canvas.drawPath (abilityPath, abilityLinePaint);
            }
            i++;
        }

    }

    private void drawText(Canvas canvas, String abilityText, Point point, int index) {
        canvas.save ();
        canvas.rotate (mDegree / 2, point.x, point.y);
        float textWidth = textPaint.measureText (abilityText);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics ();
        float textHegiht = -fontMetrics.ascent;
        float fakeDegree = mDegree * index - mDegree / 2;
        if (fakeDegree == 0) {
            canvas.drawText (abilityText, point.x - textWidth / 2,
                             point.y - mTextBlank, textPaint);
        } else if (fakeDegree > 0 && fakeDegree < 90) {

            canvas.drawText (abilityText, point.x + mTextBlank,
                             point.y, textPaint);
        } else if (fakeDegree >= 90 && fakeDegree < 180) {

            canvas.drawText (abilityText, point.x + mTextBlank,
                             point.y + mTextBlank,
                             textPaint);
        } else if (fakeDegree == 180) {
            canvas.drawText (abilityText, point.x - textWidth / 2,
                             point.y + mTextBlank + textHegiht / 2,
                             textPaint);
        } else if (fakeDegree > 180 && fakeDegree < 270) {
            canvas.drawText (abilityText, point.x - mTextBlank - textWidth,
                             point.y + mTextBlank,
                             textPaint);
        } else {
            canvas.drawText (abilityText, point.x - mTextBlank - textWidth, point.y,
                             textPaint);
        }
        canvas.restore ();
    }


    private void drawEdge(Canvas canvas, float distance) {
        for(int i = 0; i < mAbilityCount; i++) {
            Point point = calculatePoint (i * mDegree, distance, mRadius);
            if (i == 0) {
                edgePath.moveTo (point.x, point.y);
            } else {
                edgePath.lineTo (point.x, point.y);
            }

            if (i == mAbilityCount - 1) {
                edgePath.close ();
                canvas.drawPath (edgePath, linePaint);
            }
        }

    }

    private void drawInnerLine(Canvas canvas) {
        for(int i = 0; i < mAbilityCount; i++) {
            Point point = calculatePoint (i * mDegree, mRadius, mRadius);
            canvas.drawLine (mRadius, mRadius, point.x, point.y, linePaint);
        }
    }

    @Override
    public void update(RadarMap ability) {
        setAbility (ability);
    }


    class Point {
        public float x;
        public float y;
    }

    private int dip2px(float dpValue) {
        final float scale = getResources ().getDisplayMetrics ().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * @param degree   顺时针的角度 0°~360°
     * @param distance 距圆心的距离
     * @param radius   圆半径
     * @return
     */
    public Point calculatePoint(float degree, float distance, float radius) {
        float radian;
        Point resultP = new Point ();
        if (degree <= 90) {
            radian = (float) Math.toRadians (degree);
            resultP.x = radius + distance * (float) Math.sin (radian);
            resultP.y = radius - distance * (float) Math.cos (radian);
        } else if (degree > 90 && degree <= 180) {
            radian = (float) Math.toRadians (degree - 90);
            resultP.x = radius + distance * (float) Math.cos (radian);
            resultP.y = radius + distance * (float) Math.sin (radian);
        } else if (degree > 180 && degree <= 270) {
            radian = (float) Math.toRadians (degree - 180);
            resultP.x = radius - distance * (float) Math.sin (radian);
            resultP.y = radius + distance * (float) Math.cos (radian);
        } else {
            radian = (float) Math.toRadians (degree - 270);
            resultP.x = radius - distance * (float) Math.cos (radian);
            resultP.y = radius - distance * (float) Math.sin (radian);

        }

        return resultP;
    }

    @Override
    protected void onDetachedFromWindow() {
        if(mAbility !=null){
            mAbility.removeObserver (this);
        }
        super.onDetachedFromWindow ();
    }
}

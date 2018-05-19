package com.example.adria.testavimai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Switch;

public class KnobView extends View implements GestureDetector.OnGestureListener{
    private GestureDetector mGestureDetector;
    private float radius;
    private float _theta = 0.0f;
    private RectF _knobRect = new RectF();
    private OnAngleChangedListener _angleChangedListener = null;
    private float mDeltaVal;

    public Paint getKnobCoulour() {
        return mKnobPaint;
    }

    public void setKnobColour(Paint mKnobPaint) {
        this.mKnobPaint = mKnobPaint;
    }

    private Paint mKnobPaint;
    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean mEnabled) {
        this.mEnabled = mEnabled;
    }

    public float getmProgress() {
        return mProgress;
    }

    public void setmProgress(float mProgress) {
        this.mProgress = mProgress;
    }

    public float getmMinValue() {
        return mMinValue;
    }

    public void setmMinValue(float mMinValue) {
        this.mMinValue = mMinValue;
        mDeltaVal = 360.0f / (mMaxValue - mMinValue);
    }

    public float getmMaxValue() {
        return mMaxValue;
    }

    public void setmMaxValue(float mMaxValue) {
        this.mMaxValue = mMaxValue;
        mDeltaVal = 360.0f / (mMaxValue - mMinValue);
    }

    private boolean mEnabled;
    private float mProgress;
    private float mMinValue;
    private float mMaxValue;
    PointF touchPoint2 = new PointF();




    private void init()
    {

        mGestureDetector = new GestureDetector(getContext(),this);
        mProgress = 0f;
        mMinValue = 0f;
        mMaxValue = 100f;
        mEnabled = true;
        mDeltaVal = 360.0f / (mMaxValue - mMinValue);
        mKnobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }



    public interface OnAngleChangedListener  {
        void onAngleChanged(float theta);

    }

    public KnobView(Context context){
        super(context);
        init();
    }

    public float getTheta() {
        return _theta;
    }

    public void setTheta(float theta) {
        _theta = theta;
        invalidate();
    }

    public void setOnAngleChangedListener(OnAngleChangedListener listener){
        _angleChangedListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mEnabled) {
            float distance2center = distanceToCenter(event.getX(), event.getY());
            if (distance2center <= radius*0.63f && distance2center >= radius * 0.35f) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else {

                    return super.onTouchEvent(event);
                }

            }
            else
                return false;
        }
        else
            return false;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Center
        _knobRect.left = getPaddingLeft();
        _knobRect.top = getPaddingTop();
        _knobRect.right = getWidth() - getPaddingRight();
        _knobRect.bottom = _knobRect.width();

        float offset = (getHeight() - _knobRect.height()) * 0.0f;
        _knobRect.top += offset;
        _knobRect.bottom += offset;

        radius = _knobRect.width() * 0.80f;

        //Empty middle
        float middleRadius = radius * 0.35f;

        RectF middleRect = new RectF();
        middleRect.left = _knobRect.centerX() - middleRadius;
        middleRect.top = _knobRect.centerY() - middleRadius;
        middleRect.right = _knobRect.centerX() + middleRadius;
        middleRect.bottom = _knobRect.centerY() + middleRadius;


//        PointF nibCenter = new PointF();
//        nibCenter.x = _knobRect.centerX() + radius * (float)Math.cos((double)_theta);
//        nibCenter.y = _knobRect.centerY() +  radius * (float)Math.sin((double)_theta);

//        float nibRadius = radius * 0.2f;

//        RectF nibRect = new RectF();
//        nibRect.left = nibCenter.x - nibRadius;
//        nibRect.top = nibCenter.y - nibRadius;
//        nibRect.right = nibCenter.x + nibRadius;
//        nibRect.bottom = nibCenter.y + nibRadius;


        if (mEnabled){

            mKnobPaint.setColor(Color.GREEN);

        }

//        Paint nibPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        nibPaint.setColor(Color.YELLOW);

        Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setColor(Color.LTGRAY);

        canvas.drawOval(_knobRect, mKnobPaint);
     //   canvas.drawOval(nibRect, nibPaint);
        canvas.drawOval(middleRect, background);


    }
    @Override
    public boolean onDown(MotionEvent e) {
        touchPoint2.x = e.getX();
        touchPoint2.y = e.getY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        PointF touchPoint = new PointF();
        touchPoint.x = e2.getX();
        touchPoint.y = e2.getY();

        double dTheta2 = Math.atan2(touchPoint2.y - _knobRect.centerY(),
                touchPoint2.x - _knobRect.centerX());
        double dTheta = Math.atan2(touchPoint.y - _knobRect.centerY(),
                touchPoint.x - _knobRect.centerX());
        setTheta((float)dTheta);
        dTheta = dTheta > 0? Math.toDegrees(dTheta) : 360 +Math.toDegrees(dTheta);

        dTheta2 = dTheta2 > 0? Math.toDegrees(dTheta2) : 360 +Math.toDegrees(dTheta2);





        touchPoint2 = touchPoint;
        if  (dTheta > 280.0f || dTheta < 70.0f) {
            if (dTheta - dTheta2 < 0.0f && distanceY < 0.0f) {
                return true;
            }
            else if (dTheta - dTheta2 > 0.0f && distanceY > 0.0f) {
                return  true;

            }
        }
        updateProgress((float)(dTheta - dTheta2));

        return true;

    }
    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


        return false;
    }

    private void updateProgress(float progressAngle) {
        // calculate the touch-angle
       // mTouchAngle = getAngle(x, y);

        // calculate the new value depending on angle
        mProgress += progressAngle / mDeltaVal;
        mProgress = Math.min(mProgress, mMaxValue);
        mProgress = Math.max(mProgress, mMinValue);
      //  mProgress += newVal;
        _angleChangedListener.onAngleChanged(mProgress);
    }

    private float distanceToCenter(float x, float y) {
       // PointF c = getCenter();
        return (float) Math.sqrt(Math.pow(x - _knobRect.centerX(), 2.0) + Math.pow(y -  _knobRect.centerY(), 2.0));
    }







}


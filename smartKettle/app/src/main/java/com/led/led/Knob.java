package com.led.led;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

public class Knob extends View {
    //Gravity _gravity = 0;

    float _theta = 0.0f;

    public Knob(Context context){
        super(context);
    }

//    public void setGravity(Gravity gravity){
//        _gravity = gravity;
//    }

    public float getTheta() {
        return _theta;
    }

    public void setTheta(float theta) {
        _theta = theta;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF knobRect = new RectF();
        knobRect.left = getPaddingLeft();
        knobRect.top = getPaddingTop();
        knobRect.right = getWidth() - getPaddingRight();
        knobRect.bottom = knobRect.width();

        float offset = (getHeight() - knobRect.height()) * 0.5f;
        knobRect.top += offset;
        knobRect.bottom += offset;

        float radius = knobRect.width() * 0.35f;

        PointF nibCenter = new PointF();
        nibCenter.x = knobRect.centerX() + radius * (float)Math.cos((double)_theta);
        nibCenter.y = knobRect.centerY() +  radius * (float)Math.sin((double)_theta);

        float nibRadius = radius * 0.2f;

        RectF nibRect = new RectF();
        nibRect.left = nibCenter.x - nibRadius;
        nibRect.top = nibCenter.y - nibRadius;
        nibRect.right = nibCenter.x + nibRadius;
        nibRect.bottom = nibCenter.y + nibRadius;

        Paint knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        knobPaint.setColor(Color.GREEN);
        Paint nibPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nibPaint.setColor(Color.YELLOW);
        canvas.drawOval(knobRect, knobPaint);
        canvas.drawOval(nibRect, nibPaint);
    }
}

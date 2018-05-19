package com.example.adria.testavimai;

import android.graphics.Color;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements KnobView.OnAngleChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final KnobView knob = new KnobView(this);
        knob.setBackgroundColor(Color.LTGRAY);
        //knob.setPadding(100, 50, 100, 100);
        knob.setTheta((float)Math.PI * 4.0f);
        //knob.setGravity(Gravity.CENTER_VERTICAL);
        knob.setOnAngleChangedListener(this);
        setContentView(knob);
       // knob.setEnabled(false);

    }

    public void onAngleChanged(float theta) {
        float volume = theta;
        Log.i("TAG", "Volume changed to: " + volume);

    }
}

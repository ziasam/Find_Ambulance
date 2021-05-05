package com.brainfluence.pickmeuprebuild;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private ImageView ambulance,call;
    private TextView textView,textView2;
    private Animation left_to_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ambulance = findViewById(R.id.ambulance);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        left_to_right = AnimationUtils.loadAnimation(SplashActivity.this,R.anim.left_to_right);
        ambulance.setAnimation(left_to_right);


        new CountDownTimer(2500, 1000) {
            @Override
            public void onFinish() {


                startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                finish();


            }
            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
//        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
//                call,
//                PropertyValuesHolder.ofFloat("scaleX",1.2f),
//                PropertyValuesHolder.ofFloat("scaleY",1.2f)
//        );
//
//
//
//
//
//
//        objectAnimator.setDuration(1000);
//        objectAnimator.setRepeatCount(2);
//        objectAnimator.start();

    }
}
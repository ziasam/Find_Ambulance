package com.brainfluence.pickmeuprebuild;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import static com.brainfluence.pickmeuprebuild.LoginActivity.IS_LOGGED_IN;
import static com.brainfluence.pickmeuprebuild.LoginActivity.SHARED_PREFS;

public class SplashActivity extends AppCompatActivity {
    private ImageView ambulance,call;
    private TextView textView,textView2;
    private Animation left_to_right;
    private SharedPreferences sharedPref;
    private Boolean isLoggedIn;
    private Intent intent ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ambulance = findViewById(R.id.ambulance);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        left_to_right = AnimationUtils.loadAnimation(SplashActivity.this,R.anim.left_to_right);
        ambulance.setAnimation(left_to_right);
        sharedPref = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        isLoggedIn = sharedPref.getBoolean(IS_LOGGED_IN,false);


        Log.d("login", "onCreate: "+isLoggedIn);
        if(isLoggedIn)
        {
            intent = new Intent(SplashActivity.this,HomeActivity.class);

        }
        else {
            intent = new Intent(SplashActivity.this,LoginActivity.class);

        }

        new CountDownTimer(2500, 1000) {
            @Override
            public void onFinish() {


                startActivity(intent);
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
package com.example.zhangbin.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

/**
 * Created by zhangbin on 2016/12/25.
 * 用来显示进入app后的闪屏功能
 */

public class WelcomeActivity extends Activity {
    private View welcomePic;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_splash);
        welcomePic =  findViewById(R.id.splashPic);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f,1.0f);
        alphaAnimation.setDuration(5000);
        welcomePic.startAnimation(alphaAnimation);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },5000);
    }
}

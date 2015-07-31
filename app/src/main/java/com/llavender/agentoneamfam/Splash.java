package com.llavender.agentoneamfam;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


public class Splash extends Activity implements Runnable {

    Thread mThread;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        frameLayout = (FrameLayout) findViewById(R.id.progress_frame);

        frameLayout.setVisibility(View.VISIBLE);
        frameLayout.setBackgroundColor(Color.argb(160, 0, 0, 0));

        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            startActivity(new Intent(Splash.this, MainActivity.class));
            finish();
        }
    }
}
package com.llavender.agentoneamfam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class Splash extends Activity implements Runnable {

    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

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
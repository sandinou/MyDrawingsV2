package com.mydrawingsv2;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    public static final long INTERVAL=60000;//variable for execute services every 1 minute
    private Handler mHandler=new Handler(); // run on another Thread to avoid crash
    private Timer mTimer=null; // timer handling

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // Cancel if service is already existed
        if (mTimer!=null)
            mTimer.cancel();
        else
            mTimer=new Timer(); // recreate new timer

        mTimer.scheduleAtFixedRate(new TimeDisplayTask(),0,INTERVAL);   // schedule task
    }


    @Override
    public void onDestroy() {
        mTimer.cancel();    // cancel the timer
    }

    // inner class of TimeDisplayTimerTask
    private class TimeDisplayTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast every minute
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

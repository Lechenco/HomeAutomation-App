package utfpr.biagini.integrationapp;

import java.util.Date;
import java.util.Timer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import static java.lang.System.currentTimeMillis;


public class TimerHandler {

    private static long MAX_DELAY = 10000;
    private OnTimeoutListener onTimeoutListener;

    final Handler handler = new Handler();

    public void start() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                long startTimeMilis = System.currentTimeMillis();
                Log.d("TimerHandler", "Timer Start");

                while (System.currentTimeMillis() - startTimeMilis <= MAX_DELAY);

                timeOut();
            }
        });
    }

    //Listener methods
    public void timeOut(){
        if(onTimeoutListener != null)
            onTimeoutListener.timeOut();
    }

    public void setOnTimeOutListener(OnTimeoutListener onTimeOutListener){
        this.onTimeoutListener = onTimeOutListener;
    }
}

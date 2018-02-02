package com.huang.cn.horizontalprogressbar;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    MixTextProgressBar mixTextProgressBar;
    private int progress=0;
    private int MAX=30;
    private MyHandler handler;

    private class MyHandler extends Handler {

        private WeakReference<Context> reference;

        MyHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity downloadActivity = (MainActivity) reference.get();
            if (downloadActivity != null) {
                switch (msg.what) {
                    case 0:
                        if (progress <= 15) {
                            mixTextProgressBar.setProgress(progress);
                            mixTextProgressBar.setText(progress+"/"+MAX);
                            handler.sendEmptyMessageDelayed(0, 50);
                            progress++;
                        }
                        break;

                    default:
                        break;
                }
            }
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mixTextProgressBar = findViewById(R.id.mixTextProgressBar);
        mixTextProgressBar.setMax(MAX);
        mixTextProgressBar.setProgress(0);
        handler = new MyHandler(this);
        handler.sendEmptyMessageDelayed(0, 500);

    }

}

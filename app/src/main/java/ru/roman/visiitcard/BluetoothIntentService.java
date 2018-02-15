package ru.roman.visiitcard;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

public class BluetoothIntentService extends IntentService {

    Handler mHandler;

    public BluetoothIntentService() {
        super("BluetoothIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "BluetoothIntentService запущен!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}

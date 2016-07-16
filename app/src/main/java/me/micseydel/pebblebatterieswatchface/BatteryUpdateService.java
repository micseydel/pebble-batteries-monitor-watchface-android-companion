package me.micseydel.pebblebatterieswatchface;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class BatteryUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRepeatingTask();
        return Service.START_STICKY;
    }


    private void sendBatteryUpdate() {
        final Integer batteryLevel = getBatteryLevel();
        if (batteryLevel != null) {
            PebbleDictionary dict = new PebbleDictionary();
            dict.addInt32(0, batteryLevel); // TODO battery

            final UUID appUuid = UUID.fromString("1843c73a-1f77-43a8-85d2-ae824f508766");

            // Send the dictionary
            PebbleKit.sendDataToPebble(getApplicationContext(), appUuid, dict);
            Log.i("mike", "sent " + batteryLevel + " to watch");
        } else {
            // TODO: toast + log
            Log.i("mike", "battery level was unexpectedly null");
            Toast.makeText(this, "battery level was unexpectedly null", Toast.LENGTH_LONG).show();
        }
    }

    private Integer getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        // TODO; charging state can be extracted https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            Log.i("mike", "level = " + level);
            Log.i("mike", "scale = " + scale);

            float batteryPct = 100 * level / (float)scale;
            Log.i("mike", "batteryPct= " + batteryPct);
            return (int) batteryPct;
        }
        return null;
    }

    private final static int INTERVAL = 1000 * 60; // every minute
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            sendBatteryUpdate();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }
}

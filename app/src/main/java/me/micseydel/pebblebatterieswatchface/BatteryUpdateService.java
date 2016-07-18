package me.micseydel.pebblebatterieswatchface;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class BatteryUpdateService extends Service {
    private static final String TAG = BatteryUpdateService.class.getName();

    private static final UUID APP_UUID = UUID.fromString("1843c73a-1f77-43a8-85d2-ae824f508766");

    private static final int INTERVAL = 1000 * 60; // every minute

    private static final int BATTERY_KEY = 0;

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        sendBatteryUpdate();
        startRepeatingTask();
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataReceiver);
        return Service.START_STICKY;
    }

    private void sendBatteryUpdate() {
        final Integer batteryLevel = getBatteryLevel();
        if (batteryLevel != null) {
            final PebbleDictionary dict = new PebbleDictionary();
            dict.addInt32(BATTERY_KEY, batteryLevel);

            // Send the dictionary
            PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, dict);
            Log.i(TAG, String.format("Sent batteryLevel %s to watch", batteryLevel));
        } else {
            Log.i(TAG, "Battery level was unexpectedly null");
            Toast.makeText(this, "Battery level was unexpectedly null", Toast.LENGTH_LONG).show();
        }
    }

    private Integer getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        // TODO: communicate battery charging state as well as the current level
        // See: extracted https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level == -1 || scale == -1) {
                return null;
            }

            return (int) (100 * level / (float) scale);
        }

        return null;
    }

    private final Handler mHandler = new Handler();

    private final Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            sendBatteryUpdate();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask() {
        mHandlerTask.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    // Create a new receiver to get AppMessages from the C app
    private final PebbleDataReceiver dataReceiver = new PebbleDataReceiver(APP_UUID) {
        @Override
        public void receiveData(final Context context, final int transaction_id,
                                final PebbleDictionary dict) {
            sendBatteryUpdate();
            // A new AppMessage was received, tell Pebble
            PebbleKit.sendAckToPebble(context, transaction_id);
        }

    };
}

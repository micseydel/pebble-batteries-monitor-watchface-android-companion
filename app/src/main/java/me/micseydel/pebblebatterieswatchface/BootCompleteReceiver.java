package me.micseydel.pebblebatterieswatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, BatteryUpdateService.class);
        context.startService(service);
    }
}

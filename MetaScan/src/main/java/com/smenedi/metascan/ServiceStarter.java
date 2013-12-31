package com.smenedi.metascan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by smenedi on 12/27/13.
 */
public class ServiceStarter extends BroadcastReceiver {
    private SharedPreferences settingsPreferences;
    private static final String TAG_ASCAN_NEW_FILES = "ascan_new_files";
    @Override
    public void onReceive(Context context, Intent intent) {
        settingsPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent bgSvc = new Intent(context, NewFileDetectorSvc.class);
            //save auto scan preferences in shared preferences.
            //SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
            if (settingsPreferences.getBoolean(TAG_ASCAN_NEW_FILES, false)) {
                //stop background service
                context.startService(bgSvc);
            }
        }
    }

}

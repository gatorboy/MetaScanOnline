package com.smenedi.metascan;


import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;


/**
 * Created by smenedi on 12/25/13.
 */
public class NewFileDetectorSvc extends Service {

    public RecursiveNewFileObserver fileObserver;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("SERVICE STATUS", "STARTED");
        fileObserver = new RecursiveNewFileObserver(this, Environment.getExternalStorageDirectory().getAbsolutePath(), FileObserver.CREATE|FileObserver.MODIFY);
        fileObserver.startWatching();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.e("SERVICE STATUS", "STARTED ONSTARTCOMMAND");
        //Toast.makeText(getApplicationContext(), "Autoscan Enabled", Toast.LENGTH_LONG).show();
        //notifyNotification("","");
        //fileObserver = new RecursiveNewFileObserver(this, Environment.getExternalStorageDirectory().getAbsolutePath(), FileObserver.CREATE);
        //fileObserver.startWatching();
        return Service.START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("SERVICE STATUS", "CLOSED");
        //Toast.makeText(getApplicationContext(), "Autoscan Disabled", Toast.LENGTH_LONG).show();
        fileObserver.stopWatching();
    }
}

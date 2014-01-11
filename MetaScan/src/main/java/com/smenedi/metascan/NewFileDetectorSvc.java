package com.smenedi.metascan;


import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;


/**
 * Created by smenedi on 12/25/13.
 */
public class NewFileDetectorSvc extends Service {

    public RecursiveNewFileObserver fileObserver;
    AsyncTask<Void, Void, Void> startWatch;

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
        fileObserver = new RecursiveNewFileObserver(this, Environment.getExternalStorageDirectory().getAbsolutePath(), FileObserver.CREATE | FileObserver.MODIFY);
        startWatch = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                fileObserver.startWatching();
                return null;
            }
        };
        startWatch.execute();

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
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                fileObserver.stopWatching();
                return null;
            }
        }.execute();

    }
}

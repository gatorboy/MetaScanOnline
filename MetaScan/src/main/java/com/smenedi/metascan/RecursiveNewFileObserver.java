package com.smenedi.metascan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecursiveNewFileObserver {
    List<NewFileObserver> mObservers;
    String mPath;
    int mMask;
    ArrayList<Result> threatFiles;
    NewFileDetectorSvc newFileDetectorSvc;

    //Scan parameters
    private MetaScanAPI metaScanAPI;
    private String API_KEY;
    private static final String TAG_HTTP_CODE = "httpResponseCode";
    private static final String TAG_DATA_ID = "data_id";
    private static final String SCAN_RESULT_DATAID = "https://api.metascan-online.com/v1/file/";
    private static final String SCAN_RESULT_BROWSER = "https://www.metascan-online.com/en/scanresult/file/";
    private static final String TAG_PROGRESS_PERCENTAGE = "progress_percentage";
    private static final String TAG_SCAN_RESULT_STATUS = "scan_all_result_a";
    private static final String TAG_SCAN_RESULTS = "scan_results";
    private static final String TAG_API_KEY = "apikey";
    private static final String TAG_ASCAN_NEW_FILES = "ascan_new_files";
    private static final String TAG_SOUND_NOTIFICATION = "sound_notification";
    private static final String TAG_VIBE_NOTIFICATION = "vibe_notification";
    private static final String TAG_CLEAR_THREATS = "clearthreats";
    private SharedPreferences settingsPreferences;

    public RecursiveNewFileObserver() {
        super();
    }


    /**
     * Created by smenedi on 12/25/13.
     */
    class NewFileObserver extends FileObserver {
        public String absolutePath;

        public NewFileObserver(String path, int mask) {
            super(path, mask);
            absolutePath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & FileObserver.CREATE) != 0) {
                RecursiveNewFileObserver.this.onEvent(event, absolutePath + File.separator + path);
            } else if ((event & FileObserver.MODIFY) != 0)
                RecursiveNewFileObserver.this.onEvent(event, absolutePath + File.separator + path);
        }

    }

    public RecursiveNewFileObserver(NewFileDetectorSvc newFileDetectorSvc, String path, int mask) {
        //super(path, mask);
        this.newFileDetectorSvc = newFileDetectorSvc;
        mPath = path;
        mMask = mask;
        threatFiles = new ArrayList<Result>();

        //initialize scan parameters
        metaScanAPI = new MetaScanAPI();
        settingsPreferences = newFileDetectorSvc.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    //@Override
    public synchronized void startWatching() {
        if (mObservers != null)
            return;

        try {
            mObservers = new ArrayList<NewFileObserver>();
            Stack stack = new Stack();
            stack.push(mPath);

            while (!stack.isEmpty()) {
                String parent = String.valueOf(stack.pop());
                mObservers.add(new NewFileObserver(parent, mMask));
                File path = new File(parent);
                File[] files = path.listFiles();
                if (null == files)
                    continue;
                for (File f : files) {
                    if (f.isDirectory() && !f.getName().equals(".") && !f.getName()
                            .equals("..")) {
                        stack.push(f.getPath());
                    }
                }
            }

            for (NewFileObserver sfo : mObservers) {
                sfo.startWatching();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Override
    public synchronized void stopWatching() {
        if (mObservers == null)
            return;
        for (NewFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }
        mObservers.clear();
        mObservers = null;
    }

    public void onEvent(int event, String path) {
        if ((event & FileObserver.CREATE) != 0 || (event & FileObserver.MODIFY) != 0 || (event & FileObserver.MOVED_TO) != 0) {
            //Check if the threatFiles need to be recreated.
            if (settingsPreferences.getBoolean(TAG_CLEAR_THREATS, false)) {
                SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
                prefsEditor.putBoolean(TAG_CLEAR_THREATS, false);
                prefsEditor.commit();
                threatFiles = new ArrayList<Result>();
            }
            File file = new File(path);
            if (file.canRead() && file.isFile() && file.exists())
                new ScanNewFile().execute(path);
        }
    }


    public class ScanNewFile extends AsyncTask<String, Void, JSONObject> {
        File file;

        @Override
        protected JSONObject doInBackground(String... params) {
            String data_id;
            API_KEY = settingsPreferences.getString(TAG_API_KEY, "");
            JSONObject result = null;
            for (String file : params) {
                try {
                    JSONObject scanRequestJSON = metaScanAPI.requestFileScan(API_KEY, file, null);
                    if (scanRequestJSON.getInt(TAG_HTTP_CODE) == 200) {
                        data_id = scanRequestJSON.getString(TAG_DATA_ID);
                        JSONObject scanResultJSON = metaScanAPI.resultFileScan(API_KEY, data_id);
                        Log.e("scanRequestJSON1", scanRequestJSON.toString());
                        while (true) {
                            if (scanResultJSON.getInt(TAG_HTTP_CODE) == 200) {
                                //Log.e("RESULT: ", scanResultJSON.getString("source"));
                                if (scanResultJSON.getJSONObject(TAG_SCAN_RESULTS).getInt(TAG_PROGRESS_PERCENTAGE) != 100) {
                                    scanResultJSON = metaScanAPI.resultFileScan(API_KEY, data_id);
                                } else
                                    break;
                            } else {
                                break;
                            }
                        }
                        //JSONObject json = new JSONObject();
                        result = new JSONObject();
                        result.put("filename", file);
                        result.put("fileJSON", scanResultJSON);
                        //result.add(json);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            int size = threatFiles.size();
            if (jsonObject != null) {
                try {
                    String fileName = jsonObject.getString("filename");
                    JSONObject fileJSON = jsonObject.getJSONObject("fileJSON");
                    String status = fileJSON.getJSONObject(TAG_SCAN_RESULTS).getString(TAG_SCAN_RESULT_STATUS);
                    String data_id = fileJSON.getJSONObject(TAG_SCAN_RESULTS).getString(TAG_DATA_ID);
                    if (!status.equalsIgnoreCase("clean") && fileJSON.getInt(TAG_HTTP_CODE) == 200) {
                        threatFiles.add(new Result(fileName, status, data_id));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (threatFiles.size() > size)
                notifyNotification();
        }

    }

    public void notifyNotification() {
        Log.i("notificaiton", "called");
        Intent notificationIntent = new Intent(newFileDetectorSvc, AutoScanResultsActivity.class);
        notificationIntent.putParcelableArrayListExtra("threats", threatFiles);
        PendingIntent pendingIntent = PendingIntent.getActivity(newFileDetectorSvc, 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(newFileDetectorSvc)
                .setContentTitle("Metascan Online")
                .setContentText("Threats Detected.")
                        //.setSubText("Third line")
                .setContentInfo(String.valueOf(threatFiles.size()))
                .setSmallIcon(R.drawable.opswat_mark)
                .setTicker("Threats Detected")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int defaults = 0;
        if (settingsPreferences.getBoolean(TAG_SOUND_NOTIFICATION, false)) {
            //Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // builder.setSound(uri);
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (settingsPreferences.getBoolean(TAG_VIBE_NOTIFICATION, false)) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        builder.setDefaults(defaults);
        NotificationManager notificationManager = (NotificationManager) newFileDetectorSvc.getSystemService(newFileDetectorSvc.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        //builder.flags |= Notification.FLAG_AUTO_CANCEL;
        //int i = (int) Math.random() * 100;
        int i = 0;

        notificationManager.notify(i, builder.build());
    }
}


package com.smenedi.metascan;

/**
 * Created by smenedi on 12/19/13.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.smenedi.metascan.results.ResultsAdapter;
import com.smenedi.metascan.results.ResultsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class ResultsFragment extends ListFragment {

    private DirectoryFragment directoryFragment;
    private Set<File> scanSet = null;
    private static final String SCAN_RESULT_DATAID = "https://api.metascan-online.com/v1/file/";
    private static final String SCAN_RESULT_BROWSER = "https://www.metascan-online.com/en/scanresult/file/";
    private static final String TAG_PROGRESS_PERCENTAGE = "progress_percentage";
    private static final String TAG_SCAN_RESULT_STATUS = "scan_all_result_a";
    private static final String TAG_SCAN_RESULTS = "scan_results";
    private static final String TAG_API_KEY = "apikey";
    private static final String TAG_INVALID_KEY = "Invalid API Key";
    private static final String TAG_EXCEEDED_USAGE = "Exceeded maximum API usage";
    private static final String TAG_SERVER_ERROR = "Server temporary unavailable. Try again later.";
    private static final String TAG_SCAN_CANCELLED = "Scan Cancelled";
    private static final String TAG_UPLOAD_CANCELLED = "Upload Cancelled";
    private static final String TAG_UNKONWN_STATE = "Unknown State";
    public static String API_KEY = null;
    private static final String TAG_HTTP_CODE = "httpResponseCode";
    private static final String TAG_DATA_ID = "data_id";
    private ArrayList<ResultsItem> resultList;
    private ResultsAdapter resultsAdapter;
    private ListView resultListView;
    private MetaScanAPI metaScanAPI;
    private Button cancelButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences settingsPreferences;
    //private LoadResults loadResults;
    private UploadFiles uploadFiles;
    private UpdateStatus updateStatus;
    private static boolean scanCancelled = false;
    private ConcurrentHashMap<File, FileUploadState> fileUpload;
    private InternetConnectionDetector internetConnection;

    private static ResultsFragment instance;

    public static ResultsFragment getInstance() {
        if (instance == null) {
            instance = new ResultsFragment();
        }
        return instance;
    }

    private ResultsFragment() {
    }

    private boolean isScanCancelled() {
        return scanCancelled;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        directoryFragment = DirectoryFragment.getInstance();
        internetConnection = new InternetConnectionDetector(getActivity());
        scanSet = new TreeSet<File>();
        metaScanAPI = new MetaScanAPI();
        sharedPreferences = getActivity().getSharedPreferences("dataids", Context.MODE_PRIVATE);
        fileUpload = new ConcurrentHashMap<File, FileUploadState>();
        /*for(File file: scanSet){
            resultList.add(new ResultsItem(file.getAbsolutePath(),"Clean", "Pass"));
        }*/
        //refreshResults(scanSet);
        getDefaultResults();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //Check internet connection
        if (!internetConnection.isConnectingToInternet()) {
            Toast.makeText(getActivity(), "No Network Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        openBrowser(position);
    }

    //code to show the results when activity created.
    public void getDefaultResults() {
        String content = readFromFile();
        resultList = new ArrayList<ResultsItem>();
        if (!content.equals("FILENOTFOUND")) {
            JSONArray jsonContent = null;
            // try parse the string to a JSON object
            try {
                jsonContent = new JSONArray(content);
                for (int i = 0; i < jsonContent.length(); i++) {
                    try {
                        String fileName = jsonContent.getJSONObject(i).getString("filename");
                        JSONObject fileJSON = jsonContent.getJSONObject(i).getJSONObject("fileJSON");
                        String status = fileJSON.getJSONObject(TAG_SCAN_RESULTS).getString(TAG_SCAN_RESULT_STATUS);
                        String percentage = fileJSON.getJSONObject(TAG_SCAN_RESULTS).getString(TAG_PROGRESS_PERCENTAGE);
                        resultList.add(new ResultsItem(fileName, status, percentage));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //jObj.put(TAG_HTTP_CODE, httpResponseCode);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
                resultsAdapter = new ResultsAdapter(getActivity(), R.layout.results_item, resultList);
                resultListView.setAdapter(resultsAdapter);
            }

        }
        resultsAdapter = new ResultsAdapter(getActivity(), R.layout.results_item, resultList);
        resultListView.setAdapter(resultsAdapter);
    }

    /*
    public void refreshResults(Set<File> files) {
        String data_id;
        String result = "clean";
        for (File file : files) {
            try {
                JSONObject scanRequestJSON = metaScanAPI.requestFileScan(API_KEY, file.getAbsolutePath(), null);
                if (scanRequestJSON.getInt(TAG_HTTP_CODE) == 200) {
                    data_id = scanRequestJSON.getString(TAG_DATA_ID);
                    JSONObject scanResultJSON = metaScanAPI.resultFileScan(API_KEY, data_id);
                    if (scanResultJSON.getInt(TAG_HTTP_CODE) == 200) {
                        result = scanRequestJSON.getString("source");
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        resultList = new ArrayList<ResultsItem>();
        for (File file : files) {
            resultList.add(new ResultsItem(file.getAbsolutePath(), result, "Pass"));
        }
        resultsAdapter = new ResultsAdapter(getActivity(), R.layout.results_item, resultList);
        resultListView.setAdapter(resultsAdapter);
    }
    */

    public void refreshResults(Set<File> files) {
        //Initial loading of the resultspage with files to be scanned and default progress.
        resultList = new ArrayList<ResultsItem>();
        for (File file : files) {
            resultList.add(new ResultsItem(file.getAbsolutePath(), "In Progress", "0%"));
        }
        resultsAdapter = new ResultsAdapter(getActivity(), R.layout.results_item, resultList);
        resultListView.setAdapter(resultsAdapter);

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        API_KEY = settingsPreferences.getString(TAG_API_KEY, "");
        prefsEditor.clear().commit();

        //loadResults = new LoadResults();
        File[] filesArray = files.toArray(new File[files.size()]);
        int filesSize = files.size();
        //loadResults.LoadResultsData(filesArray);
        fileUpload = new ConcurrentHashMap<File, FileUploadState>();
        uploadFiles = new UploadFiles();
        updateStatus = new UpdateStatus();
        uploadFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filesArray);
        updateStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filesArray);
        cancelButton.setVisibility(View.VISIBLE);
    }

    public void openBrowser(Integer position) {
        String DATA_ID = sharedPreferences.getString(resultsAdapter.getItem(position).fileName, "empty");
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SCAN_RESULT_BROWSER + DATA_ID));
        //Bundle bundle = new Bundle();
        //bundle.putString(TAG_API_KEY, API_KEY);
        //browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);
        startActivity(browserIntent);
    }

    class Progress {
        Integer postion;
        Integer percentage;
        String result;

        Progress(int postion, int percentage, String result) {
            this.postion = postion;
            this.percentage = percentage;
            this.result = result;
        }
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput("cache", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("FILE WRITE ERROR", "File write failed: " + e.toString());
        }

    }

    private String readFromFile() {
        String ret = "";
        try {
            InputStream inputStream = getActivity().openFileInput("cache");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("FILE NOTFOUND ERROR", "File not found: " + e.toString());
            return "FILENOTFOUND";
        } catch (IOException e) {
            Log.e("FILE READ ERROR", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public enum FileUploadState {
        UPLOAD_UPLOADED, UPLOAD_INVALIDKEY, UPLOAD_SERVERERROR, UPLOAD_EXCEEDEDUSAGE, UPLOAD_CANCELLED,
        SCAN_SCANNED, SCAN_INVALIDKEY, SCAN_SERVERERROR, SCAN_EXCEEDEDUSAGE, SCAN_CANCELLED, UNKNOWN
    }

    public class UploadFiles extends AsyncTask<File, Progress, Void> {
        @Override
        protected Void doInBackground(File... params) {
            int count = 0;
            boolean errorStatus = false;
            int errorCode = 0;
            for (File file : params) {
                if (isScanCancelled()) {
                    fileUpload.put(file, FileUploadState.UPLOAD_CANCELLED);
                    publishProgress(new Progress(count, 0, TAG_UPLOAD_CANCELLED));
                } else if (errorStatus) {
                    if (errorCode == 401) {
                        fileUpload.put(file, FileUploadState.UPLOAD_INVALIDKEY);
                        publishProgress(new Progress(count, 0, TAG_INVALID_KEY));
                    } else if (errorCode == 403) {
                        fileUpload.put(file, FileUploadState.UPLOAD_EXCEEDEDUSAGE);
                        publishProgress(new Progress(count, 0, TAG_EXCEEDED_USAGE));
                    } else if (errorCode == 500) {
                        fileUpload.put(file, FileUploadState.UPLOAD_SERVERERROR);
                        publishProgress(new Progress(count, 0, TAG_SERVER_ERROR));
                    }
                } else {
                    try {
                        JSONObject scanRequestJSON = metaScanAPI.requestFileScan(API_KEY, file.getAbsolutePath(), null);
                        if (scanRequestJSON.getInt(TAG_HTTP_CODE) == 200) {
                            String data_id = scanRequestJSON.getString(TAG_DATA_ID);
                            publishProgress(new Progress(count, 0, "Uploading.."));
                            //save data id in shared preferences.
                            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                            prefsEditor.putString(file.getAbsolutePath(), data_id);
                            prefsEditor.commit();
                            fileUpload.put(file, FileUploadState.UPLOAD_UPLOADED);
                        } else if (scanRequestJSON.getInt(TAG_HTTP_CODE) == 401 || scanRequestJSON.getInt(TAG_HTTP_CODE) == 403 || scanRequestJSON.getInt(TAG_HTTP_CODE) == 500) {
                            errorStatus = true;
                            errorCode = scanRequestJSON.getInt(TAG_HTTP_CODE);
                            if (errorCode == 401) {
                                fileUpload.put(file, FileUploadState.UPLOAD_INVALIDKEY);
                                publishProgress(new Progress(count, 0, TAG_INVALID_KEY));
                            } else if (errorCode == 403) {
                                fileUpload.put(file, FileUploadState.UPLOAD_EXCEEDEDUSAGE);
                                publishProgress(new Progress(count, 0, TAG_EXCEEDED_USAGE));
                            } else if (errorCode == 500) {
                                fileUpload.put(file, FileUploadState.UPLOAD_SERVERERROR);
                                publishProgress(new Progress(count, 0, TAG_SERVER_ERROR));
                            }
                        } else {
                            fileUpload.put(file, FileUploadState.UNKNOWN);
                            publishProgress(new Progress(count, 0, TAG_UNKONWN_STATE));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                count++;
            }
            Log.e("ASYNC", "OUT OF FILE UPLOAD TASK");
            return null;
        }

        @Override
        protected void onProgressUpdate(Progress... progresses) {
            //super.onProgressUpdate(progresses);
            Log.e("FILE UPLOAD TASK", progresses[0].result);
            ResultsItem item = resultsAdapter.getItem(progresses[0].postion);
            item.scanStatus = progresses[0].percentage.toString()+"%";
            item.scanResult = progresses[0].result;
            resultsAdapter.setResultsItem(progresses[0].postion, item);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class UpdateStatus extends AsyncTask<File, Progress, Void> {
        //HashMap<File, Boolean> fileUpload = new HashMap<File, Boolean>();

        @Override
        protected Void doInBackground(File... params) {
            int filesSize = params.length;
            boolean errorStatus = false;
            int errorCode = 0;
            boolean search = true;
            while (search) {
                if (filesSize <= 0)
                    break;
                int count = 0;
                for (File file : params) {
                    if (fileUpload.containsKey(file)) {
                        if (fileUpload.get(file) == FileUploadState.UPLOAD_UPLOADED) {
                            String data_id = sharedPreferences.getString(file.getAbsolutePath(), "");
                            if (isScanCancelled()) {
                                fileUpload.put(file, FileUploadState.SCAN_CANCELLED);
                                publishProgress(new Progress(count, 0, TAG_SCAN_CANCELLED));
                                filesSize--;
                            } else {
                                try {
                                    JSONObject scanResultJSON = metaScanAPI.resultFileScan(API_KEY, data_id);
                                    if (scanResultJSON.getInt(TAG_HTTP_CODE) == 200) {
                                        if (scanResultJSON.getJSONObject(TAG_SCAN_RESULTS).getInt(TAG_PROGRESS_PERCENTAGE) != 100) {
                                            publishProgress(new Progress(count, scanResultJSON.getJSONObject(TAG_SCAN_RESULTS).getInt(TAG_PROGRESS_PERCENTAGE), "Scanning.."));
                                            //scanResultJSON = metaScanAPI.resultFileScan(API_KEY, data_id);
                                        } else {
                                            publishProgress(new Progress(count, scanResultJSON.getJSONObject(TAG_SCAN_RESULTS).getInt(TAG_PROGRESS_PERCENTAGE), scanResultJSON.getJSONObject(TAG_SCAN_RESULTS).getString(TAG_SCAN_RESULT_STATUS)));
                                            fileUpload.put(file, FileUploadState.SCAN_SCANNED);
                                            filesSize--;
                                        }
                                    } else if (scanResultJSON.getInt(TAG_HTTP_CODE) == 401 || scanResultJSON.getInt(TAG_HTTP_CODE) == 403 || scanResultJSON.getInt(TAG_HTTP_CODE) == 500) {
                                        errorStatus = true;
                                        errorCode = scanResultJSON.getInt(TAG_HTTP_CODE);
                                        if (errorCode == 401) {
                                            fileUpload.put(file, FileUploadState.SCAN_INVALIDKEY);
                                            publishProgress(new Progress(count, 0, TAG_INVALID_KEY));
                                        } else if (errorCode == 403) {
                                            fileUpload.put(file, FileUploadState.SCAN_EXCEEDEDUSAGE);
                                            publishProgress(new Progress(count, 0, TAG_EXCEEDED_USAGE));
                                        } else if (errorCode == 500) {
                                            fileUpload.put(file, FileUploadState.SCAN_SERVERERROR);
                                            publishProgress(new Progress(count, 0, TAG_SERVER_ERROR));
                                        }
                                        filesSize--;
                                    } else {
                                        fileUpload.put(file, FileUploadState.UNKNOWN);
                                        publishProgress(new Progress(count, 0, TAG_UNKONWN_STATE));
                                        filesSize--;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (fileUpload.get(file) == FileUploadState.UPLOAD_EXCEEDEDUSAGE || fileUpload.get(file) == FileUploadState.UPLOAD_INVALIDKEY || fileUpload.get(file) == FileUploadState.UPLOAD_SERVERERROR || fileUpload.get(file) == FileUploadState.UPLOAD_CANCELLED) {
                            filesSize--;
                        }
                    }
                    count++;
                }
            }
            Log.e("ASYNC", "OUT OF STATUS UPDATE TASK");
            return null;
        }

        @Override
        protected void onProgressUpdate(Progress... progresses) {
            //super.onProgressUpdate(progresses);
            Log.e("RESULTS UPDATE", progresses[0].result);
            ResultsItem item = resultsAdapter.getItem(progresses[0].postion);
            item.scanStatus = progresses[0].percentage.toString()+"%";
            item.scanResult = progresses[0].result;
            resultsAdapter.setResultsItem(progresses[0].postion, item);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           /* while (uploadFiles.getStatus() != Status.FINISHED) {
                Log.e("WAITING...... UPDATE", uploadFiles.getStatus().toString());
            }*/
            setScanCancel(false);
            cancelButton.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_results, container, false);
        settingsPreferences = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        resultListView = (ListView) rootView.findViewById(android.R.id.list);

        API_KEY = settingsPreferences.getString(TAG_API_KEY, "");
        cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.GONE);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadResults.cancel(true);
                setScanCancel(true);
            }
        });
        String myTag = getTag();
        ((MainActivity) getActivity()).setTabFragmentResults(myTag);
        return rootView;
    }

    public void setResultData(Set<File> scanningFiles) {
        scanSet = scanningFiles;
        refreshResults(scanningFiles);
    }

    private void setScanCancel(boolean scanStatus) {
        scanCancelled = scanStatus;
    }
}
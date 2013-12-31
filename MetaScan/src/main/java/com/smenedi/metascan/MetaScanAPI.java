package com.smenedi.metascan;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smenedi on 12/21/13.
 */
public class MetaScanAPI {
    private JSONParser jsonParser;
    private static final String SCAN_REQUEST = "https://api.metascan-online.com/v1/file";
    private static final String SCAN_RESULT_DATAID = "https://api.metascan-online.com/v1/file/";
    private static final String SCAN_RESULT_HASH = "https://api.metascan-online.com/v1/hash/";
    private static final String TAG_API_KEY = "apikey";
    private static final String TAG_FILE_NAME = "filename";
    private static final String TAG_ARCHIVE_PWD = "archivepwd";
    private static final String TAG_FILE_CONTENT = "content";
    private static final String TAG_DATA_ID = "data_id";


    public MetaScanAPI() {
        jsonParser = new JSONParser();
    }

    public JSONObject requestFileScan(String API_KEY, String FILE_NAME, String ARCHIVE_PWD) {
        List<NameValuePair> headers = new ArrayList<NameValuePair>();
        headers.add(new BasicNameValuePair(TAG_API_KEY, API_KEY));
        headers.add(new BasicNameValuePair(TAG_FILE_NAME, (new File(FILE_NAME)).getName()));
        headers.add(new BasicNameValuePair(TAG_ARCHIVE_PWD, ARCHIVE_PWD));

        //List<NameValuePair> params = new ArrayList<NameValuePair>();
        //String FILE_CONTENT = fileToString(FILE_NAME);
        //params.add(new BasicNameValuePair(TAG_FILE_CONTENT, FILE_CONTENT));
        //Log.e("FILE CONTENT", FILE_NAME+"--"+FILE_CONTENT);

        JSONObject json = jsonParser.getJSONFromUrlPOST(SCAN_REQUEST, headers, new File(FILE_NAME));
        Log.e("SCANREQUESTJSON", json.toString());
        return json;
    }

    private String fileToString(String filename) {

        StringBuilder builder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            // For every line in the file, append it to the string builder
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(ls);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public JSONObject resultFileScan(String API_KEY, String DATA_ID) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        List<NameValuePair> headers = new ArrayList<NameValuePair>();
        headers.add(new BasicNameValuePair(TAG_API_KEY, API_KEY));

        JSONObject json = jsonParser.getJSONFromUrlGET(SCAN_RESULT_DATAID + DATA_ID, headers);
        Log.e("SCANRESULTSJSON", json.toString());
        return json;
    }


}

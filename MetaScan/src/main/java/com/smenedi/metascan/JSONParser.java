package com.smenedi.metascan;

/**
 * Created by smenedi on 12/21/13.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

    private static InputStream is;
    private static JSONObject jObj;
    private static String json;
    private static String url;
    private static final String TAG_HTTP_CODE = "httpResponseCode";
    // Progress Dialog
    //private ProgressDialog pDialog;
    // constructor

    public JSONParser() {
        is = null;
        jObj = null;
        json = null;
        url = null;
    }

    public JSONObject getJSONFromUrlPOST(String url, List<NameValuePair> headers, File file) {
        // Making HTTP request
        int httpResponseCode = -1;
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            for (NameValuePair pairs : headers)
                httpPost.setHeader(pairs.getName(), pairs.getValue());

            //httpPost.setEntity(new UrlEncodedFormEntity(params));

            //dummycode
            MultipartEntity entity = new MultipartEntity();
            entity.addPart("data", new FileBody(file));
            httpPost.setEntity(entity);



            Log.e("HTTPPOST", httpPost.getURI().toString());
            HttpResponse httpResponse = httpClient.execute(httpPost);
            httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            //httpResponse.getStatusLine().getStatusCode()
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            if(httpResponseCode==200){
                jObj = new JSONObject(json);
                jObj.put(TAG_HTTP_CODE, httpResponseCode);
            } else{
                jObj = new JSONObject();
                jObj.put(TAG_HTTP_CODE, httpResponseCode);
            }
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
       // Log.e("HTTP_POST_JSON", json.toString());
        return jObj;

    }


    public JSONObject getJSONFromUrlGET(String url, List<NameValuePair> headers) {
        // Making HTTP request
        int httpResponseCode = -1;
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            for (NameValuePair pairs : headers)
                httpGet.setHeader(pairs.getName(), pairs.getValue());

            Log.e("HTTPGET", httpGet.getURI().toString());
            HttpResponse httpResponse = httpClient.execute(httpGet);
            httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
            jObj.put(TAG_HTTP_CODE, httpResponseCode);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        Log.e("HTTP_GET_JSON", json);

        return jObj;

    }

}


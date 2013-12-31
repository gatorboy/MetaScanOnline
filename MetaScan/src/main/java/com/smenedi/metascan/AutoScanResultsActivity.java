package com.smenedi.metascan;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smenedi on 12/25/13.
 */
public class AutoScanResultsActivity extends ListActivity {
    //ArrayList<Result> threatList;
    private static final String SCAN_RESULT_BROWSER = "https://www.metascan-online.com/en/scanresult/file/";
    private static final String TAG_CLEAR_THREATS = "clearthreats";
    SharedPreferences settingsPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autoscan);
        settingsPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Button clear = (Button)findViewById(R.id.clearAutoScanAct);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // RecursiveNewFileObserver.ScanNewFile
                SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
                prefsEditor.putBoolean(TAG_CLEAR_THREATS, true);
                prefsEditor.commit();
                finish();
            }
        });

        Button cancel = (Button)findViewById(R.id.cancelAutoScanAct);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ArrayList<Result> threatList = getIntent().getParcelableArrayListExtra("threats");
        List<Map<String, String>> adapterList = new ArrayList<Map<String, String>>();

        for(Result r:threatList ){
            Map<String,String> map = new HashMap<String, String>();
            map.put("file", r.file);
            map.put("status", r.status);
            map.put("dataid", r.dataid);
            adapterList.add(map);
        }
        ListAdapter adapter = new SimpleAdapter(
                AutoScanResultsActivity.this, adapterList,
                R.layout.autoscan_item, new String[] { "file",
                "status", "dataid" }, new int[] { R.id.file_name,
                R.id.scan_result, R.id.status });
        // updating listview
        setListAdapter(adapter);

    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        HashMap<String, String> itemAtPosition = (HashMap<String, String>)getListView().getItemAtPosition(position);
        String data_id = itemAtPosition.get("dataid");
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SCAN_RESULT_BROWSER + data_id));
        startActivity(browserIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}


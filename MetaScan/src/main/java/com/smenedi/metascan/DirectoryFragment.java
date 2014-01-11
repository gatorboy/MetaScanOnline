package com.smenedi.metascan;

/**
 * Created by smenedi on 12/19/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.smenedi.metascan.adapter.DirectoryAdapter;
import com.smenedi.metascan.adapter.DirectoryItem;
import com.smenedi.metascan.adapter.DirectoryNode;
import com.smenedi.metascan.adapter.FileState;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;


public class DirectoryFragment extends android.support.v4.app.ListFragment implements View.OnClickListener {
    private static final String TAG_API_KEY = "apikey";
    private SharedPreferences settingsPreferences;
    private static final String TAG_PREF_SETTINGS = "settings";
    private static final File ROOT = new File(Environment.getExternalStorageDirectory() + "");
    private static DirectoryNode rootDirectoryNode;
    public File location = null;
    private InternetConnectionDetector internetConnection;
    private ListView itemListView;
    private ArrayList<DirectoryItem> itemList;
    private ProgressBar spinner;
    //public HashMap<String, Integer> scanMap;//add code to get the data of checked item
    public DirectoryNode directoryNode;

    ImageView backButton;
    Button clearAll;
    ImageView checkBoxImage;
    Button systemScanButton;
    Button scanButton;
    DirectoryAdapter directoryAdapter;
    private static DirectoryFragment instance;

    public static DirectoryFragment getInstance() {
        if (instance == null) {
            instance = new DirectoryFragment();
        }
        return instance;
    }

    private DirectoryFragment() {
    }

    public String getPath() {
        return location.getAbsolutePath();
    }

    public void setPath(File path) {
        location = path;
    }

    public void refreshDirectory(DirectoryNode currentFolder, boolean onClick) {
        //fileExplorer = new FileExplorer(path);
        itemList = new ArrayList<DirectoryItem>();
        //String parent = path.getAbsolutePath();
        //int parentState;
        FileState derivedCheckBoxState;

        if (currentFolder.state == FileState.CHECK_FULL) {
            derivedCheckBoxState = FileState.CHECK_FULL;
        } else if (currentFolder.state == FileState.CHECK_NONE) {
            derivedCheckBoxState = FileState.CHECK_NONE;
        } else {
            derivedCheckBoxState = FileState.CHECK_UNKNOWN;
        }

        for (File file : currentFolder.getAllFilesInFolder()) {
            FileState childBoxStateChange = derivedCheckBoxState;
            if (derivedCheckBoxState == FileState.CHECK_UNKNOWN) {
                childBoxStateChange = currentFolder.map.get(file).state;
            }
            currentFolder.insert(file, childBoxStateChange);
            int drawableItem = file.isDirectory() ? R.drawable.folder : R.drawable.file;
            int checkBoxItem;
            if (childBoxStateChange == FileState.CHECK_FULL) {
                checkBoxItem = R.drawable.check0;
            } else if (childBoxStateChange == FileState.CHECK_HALF) {
                checkBoxItem = R.drawable.check1;
            } else {
                checkBoxItem = R.drawable.check2;
            }

            itemList.add(new DirectoryItem(checkBoxItem, drawableItem, file.getName()));
        }
        directoryAdapter = new DirectoryAdapter(getActivity(), R.layout.directory_item, itemList);
        //directoryAdapter.

        //To make sure the selected position is not changed
        int index, top;
        directoryAdapter.notifyDataSetChanged();
        index = itemListView.getFirstVisiblePosition();
        View v = itemListView.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
        itemListView.setAdapter(directoryAdapter);
        if (onClick)
            itemListView.setSelectionFromTop(index, top);

        //Handle clear All button
        if(itemList.size()==0)
            clearAll.setVisibility(View.GONE);
        else
            clearAll.setVisibility(View.VISIBLE);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //map for checked state items
        //scanMap = new HashMap<String, Integer>();
        internetConnection = new InternetConnectionDetector(getActivity());
        settingsPreferences = getActivity().getSharedPreferences(TAG_PREF_SETTINGS, Context.MODE_PRIVATE);
        setPath(new File(Environment.getExternalStorageDirectory() + ""));
        rootDirectoryNode = new DirectoryNode(ROOT, FileState.CHECK_NONE, null);
        directoryNode = rootDirectoryNode;
        refreshDirectory(directoryNode, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
        //Toast.makeText(getActivity(), "clicked onListItemClick", Toast.LENGTH_SHORT).show();
        File newLocation = new File(directoryNode.file, itemList.get(position).title);


        if (newLocation.isDirectory()) {
            Toast.makeText(getActivity(), newLocation.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            if (newLocation.canRead()) {
                if (!directoryNode.map.containsKey(newLocation)) {
                    directoryNode.insert(newLocation, FileState.CHECK_NONE);
                }
                directoryNode = directoryNode.map.get(newLocation);
                refreshDirectory(directoryNode, false);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //refreshDirectory(directoryNode, scanMap, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_directory, container, false);
        spinner = (ProgressBar) rootView.findViewById(R.id.spinnerProgressBar);
        backButton = (ImageView) rootView.findViewById(R.id.backButton);
        scanButton = (Button) rootView.findViewById(R.id.scanButton);
        systemScanButton = (Button) rootView.findViewById(R.id.systemScan);
        clearAll = (Button) rootView.findViewById(R.id.clearAll);
        //checkBoxImage = (ImageView)rootView.findViewById(R.id.cb_item_select);
        backButton.setOnClickListener(this);
        clearAll.setOnClickListener(this);
        scanButton.setOnClickListener(this);
        systemScanButton.setOnClickListener(this);

        itemListView = (ListView) rootView.findViewById(android.R.id.list);
        return rootView;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            if (directoryNode.file == ROOT)
                return;
            directoryNode = directoryNode.getParentNode();
            refreshDirectory(directoryNode, false);
            //} else if (v.getId() == R.id.cb_item_select){
            // Toast.makeText(getActivity(), "clicked onClick", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.scanButton) {
            //Check internet connection
            if (!internetConnection.isConnectingToInternet()) {
                Toast.makeText(getActivity(), "No Network Connection", Toast.LENGTH_SHORT).show();
                return;
            }

            //Check if API Key is set
            if(settingsPreferences.getString(TAG_API_KEY, "").equals("")){
                Toast.makeText(getActivity(), "Please set the API Key in Settings tab.", Toast.LENGTH_SHORT).show();
                return;
            }

            scanFiles();
            //ResultsFragment.getInstance();
        } else if (v.getId() == R.id.systemScan) {
            if (!internetConnection.isConnectingToInternet()) {
                Toast.makeText(getActivity(), "No Network Connection", Toast.LENGTH_SHORT).show();
                return;
            }
            //Check if API Key is set
            if(settingsPreferences.getString(TAG_API_KEY, "").equals("")){
                Toast.makeText(getActivity(), "Please set the API Key in Settings tab.", Toast.LENGTH_SHORT).show();
                return;
            }
            systemScan();
        } else if (v.getId() == R.id.clearAll) {
            File selectedFile;
            DirectoryNode selectedNode = null;
            //Toast.makeText(getActivity(), "checkboxselected", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < itemList.size(); i++) {
                selectedFile = new File(directoryNode.file, itemList.get(i).title);
                selectedNode = directoryNode.map.get(selectedFile);
                selectedNode.state = FileState.CHECK_NONE;
                selectedNode.map = new HashMap<File, DirectoryNode>();
            }
            if(selectedNode!=null){
                selectedNode.setAllAncestorsState();
            }
            refreshDirectory(directoryNode, true);
        }
    }

    public void systemScan() {
        //directoryNode = null;
        DirectoryNode selectedNode = rootDirectoryNode;
        directoryNode = rootDirectoryNode;
        selectedNode.state = FileState.CHECK_FULL;
        refreshDirectory(selectedNode, true);
        scanFiles();
    }

    public void scanFiles(){
        new scanAsync().execute();

    }
    /*public void scanFiles() {
        Set<File> scanningFiles = new TreeSet<File>();
        for (DirectoryNode scanningNode : rootDirectoryNode.getAllSelectedDirNodesInFolder()) {
            File currentFile = scanningNode.file;
            if (currentFile.isFile()) {
                scanningFiles.add(currentFile);
            } else {
                scanningFiles.addAll(scanningNode.getRecursiveFiles());
            }
        }

        // Communication with results Fragment(sending the scanning files to results.
        String TabOfFragmentResults = ((MainActivity) getActivity()).getTabFragmentResults();
        ResultsFragment resultFragment = (ResultsFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragmentResults);
        resultFragment.setResultData(scanningFiles);

        //Changing the fragment to results after clicking Scan files.
        ViewPager viewPager = (ViewPager) ((MainActivity) getActivity()).findViewById(R.id.pager);
        viewPager.setCurrentItem(1, true);

    }*/

    class scanAsync extends AsyncTask<Void, Void, Set<File>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Set<File> doInBackground(Void... params) {
            Set<File> scanningFiles = new TreeSet<File>();
            for (DirectoryNode scanningNode : rootDirectoryNode.getAllSelectedDirNodesInFolder()) {
                File currentFile = scanningNode.file;
                if (currentFile.isFile()) {
                    scanningFiles.add(currentFile);
                } else {
                    scanningFiles.addAll(scanningNode.getRecursiveFiles());
                }
            }
            return scanningFiles;
        }

        @Override
        protected void onPostExecute(Set<File> set) {
            //super.onPostExecute(aVoid);
            spinner.setVisibility(View.GONE);
            // Communication with results Fragment(sending the scanning files to results.
            String TabOfFragmentResults = ((MainActivity) getActivity()).getTabFragmentResults();
            ResultsFragment resultFragment = (ResultsFragment) getActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag(TabOfFragmentResults);
            resultFragment.setResultData(set);

            //Changing the fragment to results after clicking Scan files.
            ViewPager viewPager = (ViewPager) ((MainActivity) getActivity()).findViewById(R.id.pager);
            viewPager.setCurrentItem(1, true);
        }
    }


    public void clickCheckBox(int position) {
        File selectedFile = new File(directoryNode.file, itemList.get(position).title);
        //Toast.makeText(getActivity(), selectedFile.getAbsolutePath() + position, Toast.LENGTH_SHORT).show();
        DirectoryNode selectedNode = directoryNode.map.get(selectedFile);
        if (selectedNode.state == FileState.CHECK_FULL || selectedNode.state == FileState.CHECK_HALF) {
            selectedNode.state = FileState.CHECK_NONE;
            selectedNode.map = new HashMap<File, DirectoryNode>(); ///doutful
            selectedNode.setAllAncestorsState();
        } else if (selectedNode.state == FileState.CHECK_NONE) {
            selectedNode.state = FileState.CHECK_FULL;
            selectedNode.setAllAncestorsState();
        }
        refreshDirectory(directoryNode, true);
        //directoryAdapter.notifyDataSetChanged();
    }

}
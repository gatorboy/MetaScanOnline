package com.smenedi.metascan;

/**
 * Created by smenedi on 12/19/13.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;


public class SettingsFragment extends Fragment {

    //private static String API_KEY = "3d6b05e178d9750d043c966db6801030";
    private static String API_KEY = "";
    private static final String TAG_PREF_SETTINGS = "settings";
    private static final String TAG_API_KEY = "apikey";
    private static final String TAG_ASCAN_NEW_FILES = "ascan_new_files";
    private static final String TAG_ASCAN_MODIFIED_FILES = "ascan_mod_files";
    private static final String TAG_OPSWAT_PORTAL = "https://portal.opswat.com/";
    private static final String TAG_SOUND_NOTIFICATION = "sound_notification";
    private static final String TAG_VIBE_NOTIFICATION = "vibe_notification";

    private SharedPreferences settingsPreferences;
    private RelativeLayout autoScanNewFiles;
    private RelativeLayout autoScanModifiedFiles;
    private RelativeLayout soundNotification;
    private RelativeLayout vibeNotification;
    private ViewSwitcher apiKeyViewSwitcher;
    private EditText apiKeyEditText;
    private TextView apiKeyTextView;
    private TextView apikKeyEditOK;
    private ImageView cbNewFiles;
    private ImageView cbModFiles;
    private ImageView cbSoundNotify;
    private ImageView cbVibeNotify;
    private TextView tvInstructions;
    private NewFileDetectorSvc mBoundService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settingsPreferences = getActivity().getSharedPreferences(TAG_PREF_SETTINGS, Context.MODE_PRIVATE);
        apiKeyTextView.setText(settingsPreferences.getString(TAG_API_KEY, ""));

        if (settingsPreferences.getBoolean(TAG_ASCAN_NEW_FILES, false)) {
            cbNewFiles.setImageResource(R.drawable.check0);
        } else
            cbNewFiles.setImageResource(R.drawable.check2);
        if (settingsPreferences.getBoolean(TAG_ASCAN_MODIFIED_FILES, false)) {
            cbModFiles.setImageResource(R.drawable.check0);
        } else
            cbModFiles.setImageResource(R.drawable.check2);

        if (settingsPreferences.getBoolean(TAG_SOUND_NOTIFICATION, false)) {
            cbSoundNotify.setImageResource(R.drawable.check0);
        } else
            cbSoundNotify.setImageResource(R.drawable.check2);
        if (settingsPreferences.getBoolean(TAG_VIBE_NOTIFICATION, false)) {
            cbVibeNotify.setImageResource(R.drawable.check0);
        } else
            cbVibeNotify.setImageResource(R.drawable.check2);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Toast.makeText(getActivity(), "onCreateView", Toast.LENGTH_SHORT).show();
        View rootView = inflater.inflate(R.layout.fragement_settings, container, false);
        autoScanNewFiles = (RelativeLayout) rootView.findViewById(R.id.rl_newfiles);
        autoScanModifiedFiles = (RelativeLayout) rootView.findViewById(R.id.rl_modfiles);
        apiKeyViewSwitcher = (ViewSwitcher) rootView.findViewById(R.id.apiKey_switcher);
        apiKeyTextView = (TextView) rootView.findViewById(R.id.apiKey_textview);
        apiKeyEditText = (EditText) rootView.findViewById(R.id.apiKey_edittext);
        apikKeyEditOK = (TextView) rootView.findViewById(R.id.apiKey_OKButton);
        cbNewFiles = (ImageView) autoScanNewFiles.findViewById(R.id.cb_newfiles);
        cbModFiles = (ImageView) autoScanModifiedFiles.findViewById(R.id.cb_modfiles);

        tvInstructions = (TextView) rootView.findViewById(R.id.apikey_instruction);

        soundNotification = (RelativeLayout) rootView.findViewById(R.id.rl_soundsets);
        vibeNotification = (RelativeLayout) rootView.findViewById(R.id.rl_vibesets);
        cbSoundNotify = (ImageView) soundNotification.findViewById(R.id.cb_soundNotification);
        cbVibeNotify = (ImageView) vibeNotification.findViewById(R.id.cb_vibeNotification);

        soundNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save auto scan preferences in shared preferences.
                SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
                if (settingsPreferences.getBoolean(TAG_SOUND_NOTIFICATION, false)) {
                    prefsEditor.putBoolean(TAG_SOUND_NOTIFICATION, false);
                    cbSoundNotify.setImageResource(R.drawable.check2);
                } else {
                    prefsEditor.putBoolean(TAG_SOUND_NOTIFICATION, true);
                    cbSoundNotify.setImageResource(R.drawable.check0);
                }
                prefsEditor.commit();
            }
        });

        vibeNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save auto scan preferences in shared preferences.
                SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
                if (settingsPreferences.getBoolean(TAG_VIBE_NOTIFICATION, false)) {
                    prefsEditor.putBoolean(TAG_VIBE_NOTIFICATION, false);
                    cbVibeNotify.setImageResource(R.drawable.check2);
                } else {
                    prefsEditor.putBoolean(TAG_VIBE_NOTIFICATION, true);
                    cbVibeNotify.setImageResource(R.drawable.check0);
                }
                prefsEditor.commit();
            }
        });

        //Opens login page to get the API key
        tvInstructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TAG_OPSWAT_PORTAL));
                startActivity(browserIntent);
            }
        });

        apikKeyEditOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
                prefsEditor.putString(TAG_API_KEY, apiKeyEditText.getText().toString().trim()).commit();
                String API_KEY = settingsPreferences.getString(TAG_API_KEY, "");
                //InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.showSoftInput(apiKeyEditText, InputMethodManager.HIDE_IMPLICIT_ONLY);

                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(apiKeyEditText.getWindowToken(), 0);

                apiKeyEditText.setFocusable(false);
                apiKeyTextView.setText(API_KEY);
                apiKeyViewSwitcher.showNext();
            }
        });

        //Switch views for the apikey textbox to apikey editbox.
        apiKeyViewSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyViewSwitcher.getNextView().getId() == R.id.apiKey_edittext_rl) {
                    String API_KEY = settingsPreferences.getString(TAG_API_KEY, "");
                    apiKeyViewSwitcher.showNext();
                    apiKeyEditText.setText(API_KEY);
                    apiKeyEditText.setFocusableInTouchMode(true);
                    apiKeyEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(apiKeyEditText, InputMethodManager.SHOW_IMPLICIT);
                    apiKeyEditText.setSelection(apiKeyEditText.getText().length());
                }
            }
        });
        autoScanNewFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if API Key is set
                if(settingsPreferences.getString(TAG_API_KEY, "").equals("")){
                    Toast.makeText(getActivity(), "Please set the API Key.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Intent bgSvc = new Intent(getActivity(), NewFileDetectorSvc.class);
                //save auto scan preferences in shared preferences.
                SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
                if (settingsPreferences.getBoolean(TAG_ASCAN_NEW_FILES, false)) {
                    prefsEditor.putBoolean(TAG_ASCAN_NEW_FILES, false);
                    cbNewFiles.setImageResource(R.drawable.check2);
                    //stop background service
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent bgSvc = new Intent(getActivity(), NewFileDetectorSvc.class);
                            getActivity().stopService(bgSvc);
                        }
                    }).start();
                } else {
                    prefsEditor.putBoolean(TAG_ASCAN_NEW_FILES, true);
                    cbNewFiles.setImageResource(R.drawable.check0);
                    //start background service
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent bgSvc = new Intent(getActivity(), NewFileDetectorSvc.class);
                            getActivity().startService(bgSvc);
                        }
                    }).start();

                    //getActivity().bindService(bgSvc,connection, Context.BIND_AUTO_CREATE);
                    //getActivity().bindService(bgSvc, mConnection, 0);
                }
                prefsEditor.commit();
            }
        });
        autoScanModifiedFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save auto scan preferences in shared preferences.
                SharedPreferences.Editor prefsEditor = settingsPreferences.edit();
                if (settingsPreferences.getBoolean(TAG_ASCAN_MODIFIED_FILES, false)) {
                    prefsEditor.putBoolean(TAG_ASCAN_MODIFIED_FILES, false);
                    cbModFiles.setImageResource(R.drawable.check2);
                } else {
                    prefsEditor.putBoolean(TAG_ASCAN_MODIFIED_FILES, true);
                    cbModFiles.setImageResource(R.drawable.check0);
                }
                prefsEditor.commit();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
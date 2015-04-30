package com.mendeleypaperreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.mendeleypaperreader.parser.LoadData;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.NetworkUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by pedro on 05/04/15.
 */
public class ServiceIntent extends IntentService {

    private static final String TAG = "ServiceIntent";
    private static final boolean DEBUG = Globalconstant.DEBUG;

   public  static final String ACTION_FIRST_LOAD = "com.mendeleypaperreader.action.FIRST_LOAD";
    public static final String ACTION_SYNC_REQUEST = "com.mendeleypaperreader.action.SYNC_RESQUEST";

    private LoadData load;
    public static boolean serviceState;
    private Preferences preferences;

    public ServiceIntent() {

        super("ServiceIntent");
        if(DEBUG)Log.d(TAG, "In ServiceIntent");
        serviceState = true;
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        if (DEBUG) Log.d(TAG, "In onStartCommand");

        load = new LoadData(getApplicationContext());
        preferences = new Preferences(getApplicationContext());
        String access_token = preferences.LoadPreference("access_token");




        if(intent.getAction().equals(ACTION_FIRST_LOAD))
            firstLoad(access_token);
        else if(intent.getAction().equals(ACTION_SYNC_REQUEST))
            syncRequest(access_token);


    }

    private void SendProgressBroadCast(Intent broadcastIntent, int load, int max ){
        broadcastIntent.setAction(Globalconstant.mBroadcastUpdateProgressBar);
        broadcastIntent.putExtra("Progress", load / ((float) max) * 100);
        if(DEBUG)Log.d(TAG, "SendProgressBroadCast: " + load / ((float) max) * 100);

        sendBroadcast(broadcastIntent);

    }



    private void firstLoad(String access_token){

        Intent broadcastIntent = new Intent();

        load.getProfileInfo(Globalconstant.get_profile + access_token);
        SendProgressBroadCast(broadcastIntent, 1, 10);

        load.getUserLibrary(Globalconstant.get_user_library_url + access_token, false);
        SendProgressBroadCast(broadcastIntent, 2, 10);

        load.getUserFolders(Globalconstant.get_user_folders_url + access_token);
        SendProgressBroadCast(broadcastIntent, 3, 10);

        load.getUserGroups(Globalconstant.get_groups_url + access_token);
        SendProgressBroadCast(broadcastIntent, 4, 10);

        load.getUserLibrary(Globalconstant.get_trash_documents + access_token, true);
        SendProgressBroadCast(broadcastIntent, 5, 10);

        load.getUserDocsInFolders();
        SendProgressBroadCast(broadcastIntent, 6, 10);

        load.getGroupDocs();
        SendProgressBroadCast(broadcastIntent, 7, 10);

        load.getNotes();
        SendProgressBroadCast(broadcastIntent, 8, 10);

        load.getCatalogId();
        SendProgressBroadCast(broadcastIntent, 9, 10);

        load.getFiles(Globalconstant.get_files + access_token);
        SendProgressBroadCast(broadcastIntent, 10, 10);

    }

    private void syncRequest(String access_token){

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        df_ISO8601.setTimeZone(tz);



        String url =   Globalconstant.get_user_library_url_changes_client.replace("#modified_date#", preferences.LoadPreference("LAST_SYNCHRONIZATION_DATE"));
        Intent broadcastIntent = new Intent();
        SendProgressBroadCast(broadcastIntent, 1, 6);
        load.updateUserDocument(url + access_token, false);

        SendProgressBroadCast(broadcastIntent, 2, 6);
        load.getModifiedNotes();
        SendProgressBroadCast(broadcastIntent, 3, 6);

        load.getFilesModified(Globalconstant.get_files_added);
        SendProgressBroadCast(broadcastIntent, 4, 6);
        String trash_url =   Globalconstant.get_trash_documents_since.replace("#modified_date#", preferences.LoadPreference("LAST_SYNCHRONIZATION_DATE"));
        load.updateUserDocument(trash_url + access_token, true);

        SendProgressBroadCast(broadcastIntent, 5, 6);
        load.processRequests();

        SendProgressBroadCast(broadcastIntent, 6, 6);

        preferences.savePreferences("LAST_SYNCHRONIZATION_DATE", df_ISO8601.format(new Date()));
        Log.d(TAG, "syncRequest: " );
    }



    @Override
    public void onDestroy() {
       if(DEBUG)Log.d(TAG, "In onDestroy" + serviceState);

       if( NetworkUtil.getConnectivityStatus(getApplicationContext()) == 0)
           serviceState = true;

        else {
           serviceState = false;

       }

        super.onDestroy();
    }


}

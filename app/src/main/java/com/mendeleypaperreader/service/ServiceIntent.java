package com.mendeleypaperreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.mendeleypaperreader.parser.LoadData;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.NetworkUtil;

/**
 * Created by pedro on 05/04/15.
 */
public class ServiceIntent extends IntentService {

    private static final String TAG = "ServiceIntent";
    private static final boolean DEBUG = Globalconstant.DEBUG;


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

        Intent broadcastIntent = new Intent();

        load.getProfileInfo(Globalconstant.get_profile + access_token);
        SendProgressBroadCast(broadcastIntent, 1, 10);

        load.getUserLibrary(Globalconstant.get_user_library_url + access_token);
        SendProgressBroadCast(broadcastIntent, 2, 10);

        load.getUserFolders(Globalconstant.get_user_folders_url + access_token);
        SendProgressBroadCast(broadcastIntent, 3, 10);

        load.getUserGroups(Globalconstant.get_groups_url + access_token);
        SendProgressBroadCast(broadcastIntent, 4, 10);

        load.getUserDocsInFolders();
        SendProgressBroadCast(broadcastIntent, 5, 10);

        load.getGroupDocs();
        SendProgressBroadCast(broadcastIntent, 6, 10);

        load.getNotes();
        SendProgressBroadCast(broadcastIntent, 7, 10);

        load.getCatalogId();
        SendProgressBroadCast(broadcastIntent, 8, 10);

        load.getFiles(Globalconstant.get_files + access_token);
        SendProgressBroadCast(broadcastIntent, 10, 10);
    }

    private void SendProgressBroadCast(Intent broadcastIntent, int load, int max ){
        broadcastIntent.setAction(Globalconstant.mBroadcastUpdateProgressBar);
        broadcastIntent.putExtra("Progress", load / ((float) max) * 100);
        if(DEBUG)Log.d(TAG, "SendProgressBroadCast: " + load / ((float) max) * 100);

        sendBroadcast(broadcastIntent);

    }




    private void DeleteDataBase(){



    }



    @Override
    public void onDestroy() {
       if(DEBUG)Log.d(TAG, "In onDestroy" + serviceState);

       if( NetworkUtil.getConnectivityStatus(getApplicationContext()) == 0)
           serviceState = true;

        else {

            if(serviceState)
                preferences.savePreferences("IS_DB_CREATED", "YES");

           serviceState = false;

       }

        super.onDestroy();
    }


}

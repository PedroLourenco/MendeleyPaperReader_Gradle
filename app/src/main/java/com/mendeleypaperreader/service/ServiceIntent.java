package com.mendeleypaperreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.mendeleypaperreader.jsonParser.LoadData;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.Globalconstant;
import com.mendeleypaperreader.utl.NetworkUtil;

/**
 * Created by pedro on 05/04/15.
 */
public class ServiceIntent extends IntentService {


    LoadData load;
    public static boolean serviceState;
    private SessionManager sessionManager;

    public ServiceIntent() {

        super("ServiceIntent");
        Log.i(Globalconstant.TAG, "In ServiceIntent");
        serviceState = true;

    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(Globalconstant.TAG, "In onStartCommand");

        load = new LoadData(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        String access_token = sessionManager.LoadPreference("access_token");

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
        broadcastIntent.setAction(Globalconstant.mBroadcastIntegerAction);
        broadcastIntent.putExtra("Progress", load / ((float) max) * 100);
        Log.d(Globalconstant.TAG, "SendProgressBroadCast: " + load / ((float) max) * 100);

        sendBroadcast(broadcastIntent);

    }


    @Override
    public void onDestroy() {
        Log.i(Globalconstant.TAG, "In onDestroy");
       if( NetworkUtil.getConnectivityStatus(getApplicationContext()) == 0)
           serviceState = true;

        else {

           serviceState = false;

           sessionManager.savePreferences("IS_DB_CREATED", "YES");

       }

        super.onDestroy();
    }


}

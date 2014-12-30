package com.mendeleypaperreader.jsonParser;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.Globalconstant;


public class SyncDataAsync extends AsyncTask<String, Integer, String> {

    //Context context;
    Activity activity;
    private static LoadData load;
    ProgressDialog dialog;
    String access_token;
    SessionManager session;


    public SyncDataAsync(Context context, Activity activity) {
        //his.context = context;
        this.activity = activity;
        load = new LoadData(this.activity.getApplicationContext());
        dialog = new ProgressDialog(context);
        session = new SessionManager(this.activity.getApplicationContext());
        access_token = session.LoadPreference("access_token");

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        lockScreenOrientation();
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

    }


    @Override
    protected void onProgressUpdate(Integer... values) {

        dialog.setMessage(this.activity.getResources().getString(R.string.sync_data) + values[0] + "%)");
    }


    @Override
    protected String doInBackground(String... arg0) {

        try {
            syncronizeData();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "Fim do Load Data");


        return null;
    }


    @Override
    protected void onPostExecute(String json) {
        if (dialog.isShowing())
            dialog.dismiss();
        session.savePreferences("IS_DB_CREATED", "YES");
        unlockScreenOrientation();

        if (session.LoadPreference("syncOnLoad").equals("true")){
            load.downloadFiles();
        }

    }

    protected void updateProgress(int progress) {
        dialog.setMessage(this.activity.getResources().getString(R.string.sync_data) + progress + "%)");
    }


    private void syncronizeData() throws JsonProcessingException, IOException {



        publishProgress((int) (1 / ((float) 9) * 100));
        load.getProfileInfo(Globalconstant.get_profile + access_token);
        publishProgress((int) (2 / ((float) 9) * 100));
        load.getGroups(Globalconstant.get_groups_url + access_token);
        publishProgress((int) (3 / ((float) 9) * 100));
        load.getGroupDocs();
        publishProgress((int) (4 / ((float) 9) * 100));
        load.getUserLibrary(Globalconstant.get_user_library_url + access_token);
        publishProgress((int) (5 / ((float) 9) * 100));
        load.getFolders(Globalconstant.get_user_folders_url + access_token);
        publishProgress((int) (6 / ((float) 9) * 100));
        load.getDocNotes();
        publishProgress((int) (7 / ((float) 9) * 100));
        load.getCatalogId();
        publishProgress((int) (8 / ((float) 9) * 100));
        load.getFiles(Globalconstant.get_files + access_token);
        publishProgress((int) (9 / ((float) 9.6) * 100));



    }


    private void lockScreenOrientation() {
        int currentOrientation = this.activity.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }


}

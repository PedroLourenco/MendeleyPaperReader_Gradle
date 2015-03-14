package com.mendeleypaperreader.jsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.Globalconstant;
import com.mendeleypaperreader.utl.TypefaceSpan;


public class SyncDataAsync extends AsyncTask<String, Integer, String> {


    Activity activity;
    private static LoadData load;
    ProgressDialog dialog;
    String access_token;
    SessionManager session;


    public SyncDataAsync(Context context) {

        this.activity = (Activity)context;
        load = new LoadData(this.activity.getApplicationContext());
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        session = new SessionManager(this.activity.getApplicationContext());
        access_token = session.LoadPreference("access_token");

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        lockScreenOrientation();
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        
        if (!this.activity.isFinishing())
            dialog.show();

    }


    @Override
    protected void onProgressUpdate(Integer... values) {

        SpannableString ss1=  new SpannableString(this.activity.getResources().getString(R.string.sync_data) + values[0] + "%)");
        TypefaceSpan tf = new TypefaceSpan(this.activity, "Roboto-Regular.ttf");
        ss1.setSpan(tf, 0, ss1.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        dialog.setMessage(ss1);
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


        //long startTime = System.currentTimeMillis();
        publishProgress((int) (1 / ((float) 10) * 100));
        load.getProfileInfo(Globalconstant.get_profile + access_token);
        publishProgress((int) (2 / ((float) 10) * 100));
        load.getUserLibrary(Globalconstant.get_user_library_url + access_token);
        publishProgress((int) (3 / ((float) 10) * 100));
        load.getUserFolders(Globalconstant.get_user_folders_url + access_token);
        publishProgress((int) (4 / ((float) 10) * 100));
        load.getUserGroups(Globalconstant.get_groups_url + access_token);
        publishProgress((int) (5 / ((float) 10) * 100));
        load.getUserDocsInFolders();
        publishProgress((int) (6 / ((float) 10) * 100));
        load.getGroupDocs();
        publishProgress((int) (7 / ((float) 10) * 100));
        load.getNotes();
        publishProgress((int) (8 / ((float) 10) * 100));
        load.getCatalogId();
        publishProgress((int) (9 / ((float) 10) * 100));
        load.getFiles(Globalconstant.get_files + access_token);
        publishProgress((int) (10 / ((float) 10.6) * 100));
        //long endTime = System.currentTimeMillis();

        //long timeminuts = TimeUnit.MILLISECONDS.toMinutes((endTime - startTime));
        //Log.d(Globalconstant.TAG, "That took " + timeminuts + " minutes");


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

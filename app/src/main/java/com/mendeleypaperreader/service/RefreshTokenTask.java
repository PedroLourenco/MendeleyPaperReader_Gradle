package com.mendeleypaperreader.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.providers.ContentProvider;
import com.mendeleypaperreader.sessionManager.GetAccessToken;
import com.mendeleypaperreader.util.DateUtil;
import com.mendeleypaperreader.util.Globalconstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by pedro on 12/04/15.
 */
public class RefreshTokenTask extends AsyncTask<String, Integer, JSONObject> {

    private static final String TAG = "RefreshTokenTask";
    private static final boolean DEBUG = Globalconstant.DEBUG;

    private static String code;
    private static String refresh_token;

    private Context context;
    private Preferences sharedPreferences;
    private boolean isToSync;



    public RefreshTokenTask(Context context, boolean isToSync){

        this.context = context;
        this.isToSync = isToSync;

    }



    @Override
    protected JSONObject doInBackground(final String... args) {
        GetAccessToken jParser = new GetAccessToken();

        JSONObject jsonObject = null;

        if(DateUtil.TokenExpired(this.context))
            jsonObject = jParser.refresh_token(Globalconstant.TOKEN_URL, code, Globalconstant.CLIENT_ID, Globalconstant.CLIENT_SECRET, Globalconstant.REDIRECT_URI, Globalconstant.GRANT_TYPE, refresh_token);

        return jsonObject;
    }





    protected void onPreExecute() {

        sharedPreferences = new Preferences(context);

        code = sharedPreferences.LoadPreference("Code");
        refresh_token = sharedPreferences.LoadPreference("refresh_token");
    }




    protected void onPostExecute(final JSONObject json) {

        Calendar calendar = Calendar.getInstance();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (json != null) {
            try {
                String token = json.getString("access_token");
                String expire = json.getString("expires_in");
                String refresh = json.getString("refresh_token");


                // Save access token in shared preferences
                sharedPreferences.savePreferences("access_token", json.getString("access_token"));
                sharedPreferences.savePreferences("expires_in", json.getString("expires_in"));
                sharedPreferences.savePreferences("lastRefreshDate", sdf.format(calendar.getTime()));
                sharedPreferences.savePreferences("refresh_token", json.getString("refresh_token"));


                if (DEBUG) {
                    Log.d(TAG, "json: " + json.toString());
                    Log.d(TAG, "refresh_token - Expire: " + expire);
                    Log.d(TAG, "refresh_token - Refresh: " + refresh);
                    Log.d(TAG, "expire_date: " + sharedPreferences.LoadPreference("expire_date"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



         // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, 3600);

        sharedPreferences.savePreferences("expire_date", sdf.format(calendar.getTime()));




        if(isToSync){

            context.getContentResolver().delete(ContentProvider.CONTENT_URI_DELETE_DATA_BASE, null, null);

            Intent serviceIntent = new Intent(context, ServiceIntent.class);
            context.startService(serviceIntent);
        }

    }
}

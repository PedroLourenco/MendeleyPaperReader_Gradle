package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.Provider.ContentProvider;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.sessionManager.GetAccessToken;
import com.mendeleypaperreader.util.ConnectionDetector;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.RobotoRegularFontHelper;
import com.mendeleypaperreader.util.TypefaceSpan;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to display full abstract of pdf articles.
 *
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class AbstractDescriptionActivity extends Activity {

    private NumberProgressBar progressBar;
    private Preferences session;
    private static String code;
    private static String refresh_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abstract_description);

        TextView tvAbstractValue = (TextView) findViewById(R.id.abstract_description);
        RobotoRegularFontHelper.applyFont(getApplicationContext(),tvAbstractValue);
        tvAbstractValue.setText(getAbstract());

         session = new Preferences(getApplicationContext());

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);


            SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
            TypefaceSpan tf = new TypefaceSpan(this, "Roboto-Bold.ttf");

            s.setSpan(tf, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance

            actionBar.setTitle(s);
        }



         progressBar = (NumberProgressBar) findViewById(R.id.progress_bar);
            if(ServiceIntent.serviceState) {
                progressBar.setProgress(View.VISIBLE);
                progressBar.setProgress(session.LoadPreferenceInt("progress"));
            }

    }




    private String getAbstract() {

        Bundle bundle = getIntent().getExtras();

        return bundle.getString("abstract");
    }


    @Override
    public void onBackPressed() {
        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();

        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_refresh, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem refreshIcon = menu.findItem(R.id.menu_refresh);
        if(refreshIcon != null && ServiceIntent.serviceState)
            refreshIcon.setVisible(false);

        if(refreshIcon != null && !ServiceIntent.serviceState)
            refreshIcon.setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }


    //ActionBar Menu Options
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_refresh:

                if(!ServiceIntent.serviceState) {
                    refreshToken();
                }else{
                    Toast.makeText(this, "Sync in progress ", Toast.LENGTH_LONG).show();
                }

                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void syncData() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
        mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
        mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

        registerReceiver(mReceiver, mIntentFilter);

        Intent serviceIntent = new Intent(this, ServiceIntent.class);

        startService(serviceIntent);

    }


    public void onPause()
    {
        super.onPause();

        if(ServiceIntent.serviceState) {
            unregisterReceiver(mReceiver);
        }
    }


    public void onResume()
    {
        super.onResume();
        if(ServiceIntent.serviceState) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(session.LoadPreferenceInt("progress"));

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

            registerReceiver(mReceiver, mIntentFilter);
        }

        if(session.LoadPreferenceInt("progress") == 100) {
            progressBar.setVisibility(View.GONE);

        }



    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globalconstant.mBroadcastIntegerAction)) {

                Float progress = intent.getFloatExtra("Progress", 0);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress.intValue());
                session.savePreferencesInt("progress", progress.intValue());


            }

            if(progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);
                }



        }
    };


    private void refreshToken() {

        // check internet connection

        Boolean isInternetPresent;
        ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());

        isInternetPresent = connectionDetector.isConnectingToInternet();

        if (isInternetPresent) {
            getContentResolver().delete(ContentProvider.CONTENT_URI_DELETE_DATA_BASE, null, null);
            new ProgressTask().execute();
        } else {
            connectionDetector.showDialog(AbstractDescriptionActivity.this, ConnectionDetector.DEFAULT_DIALOG);
        }
    }

    //AsyncTask to download DATA from server

    class ProgressTask extends AsyncTask<String, Integer, JSONObject> {


        protected void onPreExecute() {
            session = new Preferences(AbstractDescriptionActivity.this);
            code = session.LoadPreference("Code");
            refresh_token = session.LoadPreference("refresh_token");
        }

        protected void onPostExecute(final JSONObject json) {


            if (json != null) {
                try {
                    // Save access token in shared preferences
                    session.savePreferences("access_token", json.getString("access_token"));
                    session.savePreferences("expires_in", json.getString("expires_in"));
                    session.savePreferences("refresh_token", json.getString("refresh_token"));


                    //Get data from server
                    syncData();




                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        protected JSONObject doInBackground(final String... args) {

            GetAccessToken jParser = new GetAccessToken();

            return jParser.refresh_token(Globalconstant.TOKEN_URL, code, Globalconstant.CLIENT_ID, Globalconstant.CLIENT_SECRET, Globalconstant.REDIRECT_URI, Globalconstant.GRANT_TYPE, refresh_token);

        }
    }

}

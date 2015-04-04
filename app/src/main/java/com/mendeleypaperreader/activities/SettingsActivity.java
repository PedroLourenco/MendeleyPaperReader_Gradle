package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.ServiceProvider.DataService;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.jsonParser.SyncDataAsync;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.GetDataBaseInformation;
import com.mendeleypaperreader.utl.Globalconstant;
import com.mendeleypaperreader.utl.TypefaceSpan;

public class SettingsActivity extends Activity {

    private GetDataBaseInformation getDataBaseInformation;
    private SessionManager sessionManager;
    private Typeface roboto;

    private Float progress;
    private NumberProgressBar progressBar;

    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        roboto = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Regular.ttf");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {

            getActionBar().setDisplayHomeAsUpEnabled(true);

            SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
            TypefaceSpan tf = new TypefaceSpan(this, "Roboto-Bold.ttf");

            s.setSpan(tf, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance

            actionBar.setTitle(s);
        }

        sessionManager = new SessionManager(getApplicationContext());

        progressBar = (NumberProgressBar) findViewById(R.id.progress_bar);
        if(DataService.serviceState) {
            progressBar.setProgress(View.VISIBLE);
            progressBar.setProgress(sessionManager.LoadPreferenceInt("progress"));
        }

        TextView tvProfileName = (TextView)findViewById(R.id.settings_profile_name_value);
        TextView tvUser = (TextView)findViewById(R.id.settings_user_text);
        TextView tvSync = (TextView)findViewById(R.id.settings_Sync_text);
        TextView tvtSyncHelp = (TextView)findViewById(R.id.settings_sync_help_text);
        CheckBox cbSync = (CheckBox)findViewById(R.id.settings_checkBox_sync_on_load);

        tvProfileName.setTypeface(roboto);
        tvUser.setTypeface(roboto);
        tvtSyncHelp.setTypeface(roboto);
        tvSync.setTypeface(roboto);
        cbSync.setTypeface(roboto);

        getDataBaseInformation = new GetDataBaseInformation(getApplicationContext());

        tvProfileName.setText(getDataBaseInformation.getProfileInformation(DatabaseOpenHelper.PROFILE_DISPLAY_NAME));


        if (sessionManager.LoadPreference("syncOnLoad").equals("true")){
            cbSync.setChecked(true);
        }
            
            
        
    }

    public void itemClicked(View v) {

        if (((CheckBox) v).isChecked()){
            sessionManager.savePreferences("syncOnLoad", "true");

        }else{
            sessionManager.savePreferences("syncOnLoad", "false");
        }
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

        return super.onCreateOptionsMenu(menu);
    }


    //ActionBar Menu Options
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            // up button
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onPause()
    {
        super.onPause();

        if(DataService.serviceState) {
            unregisterReceiver(mReceiver);
        }
    }


    public void onResume()
    {
        super.onResume();
        if(DataService.serviceState) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(sessionManager.LoadPreferenceInt("progress"));

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

            registerReceiver(mReceiver, mIntentFilter);

            if(sessionManager.LoadPreferenceInt("progress") == 100) {
                progressBar.setVisibility(View.GONE);
                DataService.serviceState = false;
            }
        }

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globalconstant.mBroadcastIntegerAction)) {

                progress = intent.getFloatExtra("Progress", 0);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress.intValue());
                sessionManager.savePreferencesInt("progress", progress.intValue());

            }

            if(progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);
                DataService.serviceState = false;
            }

        }
    };








}

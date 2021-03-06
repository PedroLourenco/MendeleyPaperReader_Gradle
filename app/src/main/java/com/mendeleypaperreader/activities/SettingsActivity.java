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
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.util.GetDataBaseInformation;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.TypefaceSpan;

public class SettingsActivity extends Activity {

    private GetDataBaseInformation getDataBaseInformation;
    private Preferences preferences;
    private NumberProgressBar progressBar;

    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Regular.ttf");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {

            getActionBar().setDisplayHomeAsUpEnabled(true);

            SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
            TypefaceSpan tf = new TypefaceSpan(this, "Roboto-Bold.ttf");

            s.setSpan(tf, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance

            actionBar.setTitle(s);
        }

        preferences = new Preferences(getApplicationContext());

        progressBar = (NumberProgressBar) findViewById(R.id.progress_bar);
        if(ServiceIntent.serviceState) {
            progressBar.setProgress(View.VISIBLE);
            progressBar.setProgress(preferences.LoadPreferenceInt("progress"));
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


        if (preferences.LoadPreference("syncOnLoad").equals("true")){
            cbSync.setChecked(true);
        }
            
            
        
    }

    public void itemClicked(View v) {

        if (((CheckBox) v).isChecked()){
            preferences.savePreferences("syncOnLoad", "true");

        }else{
            preferences.savePreferences("syncOnLoad", "false");
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

        if(ServiceIntent.serviceState) {
            unregisterReceiver(mReceiver);
        }
    }


    public void onResume()
    {
        super.onResume();
        if(ServiceIntent.serviceState) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(preferences.LoadPreferenceInt("progress"));

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastUpdateProgressBar);
            registerReceiver(mReceiver, mIntentFilter);


        }
        if(preferences.LoadPreferenceInt("progress") == 100) {
            progressBar.setVisibility(View.GONE);
        }

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globalconstant.mBroadcastUpdateProgressBar)) {

                Float progress = intent.getFloatExtra("Progress", 0);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress.intValue());
                preferences.savePreferencesInt("progress", progress.intValue());

            }

            if(progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);
            }

        }
    };








}

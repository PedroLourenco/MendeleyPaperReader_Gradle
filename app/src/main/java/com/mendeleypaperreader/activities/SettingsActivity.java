package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.GetDataBaseInformation;
import com.mendeleypaperreader.utl.TypefaceSpan;

public class SettingsActivity extends Activity {

    GetDataBaseInformation getDataBaseInformation;
    SessionManager sessionManager;
    private Typeface roboto;
    
    
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
        sessionManager = new SessionManager(getApplicationContext());


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

        
        
        
    
        
        
        
    

}

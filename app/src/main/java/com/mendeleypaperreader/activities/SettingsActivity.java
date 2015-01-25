package com.mendeleypaperreader.activities;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.GetDataBaseInformation;

public class SettingsActivity extends Activity {

    GetDataBaseInformation getDataBaseInformation;
    SessionManager sessionManager;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getDataBaseInformation = new GetDataBaseInformation(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        
        TextView profileName = (TextView)findViewById(R.id.textViewProfileName);
        profileName.setText(getDataBaseInformation.getProfileInformation(DatabaseOpenHelper.PROFILE_DISPLAY_NAME));

        CheckBox syncOnLoad = (CheckBox)findViewById(R.id.checkBoxSyncOnLoad);


        if (sessionManager.LoadPreference("syncOnLoad").equals("true")){
            syncOnLoad.setChecked(true);
        }
            
            
        
    }

    public void itemClicked(View v) {

        if (((CheckBox) v).isChecked()){
            sessionManager.savePreferences("syncOnLoad", "true");

        }else{
            sessionManager.savePreferences("syncOnLoad", "false");
        }
    }



        
        
        
    
        
        
        
    

}

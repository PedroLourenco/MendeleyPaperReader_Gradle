package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.ServiceProvider.DataService;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.Globalconstant;
import com.mendeleypaperreader.utl.RobotoRegularFontHelper;
import com.mendeleypaperreader.utl.TypefaceSpan;

/**
 * Class to display full abstract of pdf articles.
 *
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class AbstractDescriptionActivity extends Activity {

    private Intent serviceIntent;
    private Float progress;
    private NumberProgressBar progressBar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abstract_description);

        TextView tvAbstractValue = (TextView) findViewById(R.id.abstract_description);
        RobotoRegularFontHelper.applyFont(getApplicationContext(),tvAbstractValue);
        tvAbstractValue.setText(getAbstract());

         session = new SessionManager(getApplicationContext());

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
            if(DataService.serviceState) {
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
            progressBar.setProgress(session.LoadPreferenceInt("progress"));

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

            registerReceiver(mReceiver, mIntentFilter);

            if(session.LoadPreferenceInt("progress") == 100) {
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
                session.savePreferencesInt("progress", progress.intValue());

            }

            if(progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);
                DataService.serviceState = false;
            }

        }
    };




}

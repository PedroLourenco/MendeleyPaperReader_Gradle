package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.sessionManager.GetAccessToken;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.ConnectionDetector;
import com.mendeleypaperreader.utl.Globalconstant;
import com.mendeleypaperreader.utl.TypefaceSpan;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This activity displays the details using a MainMenuActivityFragmentDetails. This activity is started
 * by a MainMenuActivityFragmentList when a title in the list is selected.
 * The activity is used only if a MainMenuActivityFragmentDetails is not on the screen.
 *
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class DetailsActivity extends FragmentActivity {

    private static SessionManager session;
    private static String code;
    private static String refresh_token;
    private IntentFilter mIntentFilter;
    private NumberProgressBar progressBar;
    private Intent serviceIntent;
    private Float progress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);


            SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
            TypefaceSpan tf = new TypefaceSpan(this, "Roboto-Bold.ttf");

            s.setSpan(tf, 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance

            actionBar.setTitle(s);
        }


        session = new SessionManager(DetailsActivity.this);


        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            MainMenuActivityFragmentDetails details = new MainMenuActivityFragmentDetails();
            details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();

        }

    }




    @Override
    public void onBackPressed() {
        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();

        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }
    

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
       // MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.action_bar_search, menu);

        //return super.onCreateOptionsMenu(menu);
    //}




    //ActionBar Menu Options
   public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if(!Globalconstant.isTaskRunning)
                    refreshToken();
                return true;
            // up button
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }



   // public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

     //   SearchView searchView = (SearchView) menu.findItem(R.id.main_grid_default_search).getActionView();
    //}






    private void refreshToken() {

        //delete data from data base and get new access token to start sync

        // check internet connection
        Boolean isInternetPresent;
        ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());

        isInternetPresent = connectionDetector.isConnectingToInternet();

        if (isInternetPresent) {
            getContentResolver().delete(MyContentProvider.CONTENT_URI_DELETE_DATA_BASE, null, null);
            new ProgressTask().execute();
        } else {
            connectionDetector.showDialog(DetailsActivity.this, ConnectionDetector.DEFAULT_DIALOG);
        }
    }


    //AsyncTask to download DATA from server

    class ProgressTask extends AsyncTask<String, Integer, JSONObject> {


        protected void onPreExecute() {
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
                    //syncData();

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
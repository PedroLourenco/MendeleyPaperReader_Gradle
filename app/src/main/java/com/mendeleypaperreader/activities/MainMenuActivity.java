package com.mendeleypaperreader.activities;


import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.util.TypefaceSpan;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class MainMenuActivity extends FragmentActivity {

    private long mLastPressedTime;
    private static final int PERIOD = 2000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
        }



        SpannableString s = new SpannableString("Paper Reader");
        TypefaceSpan tf = new TypefaceSpan(this, "Roboto-Bold.ttf");
        
        s.setSpan(tf, 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance

        if (actionBar != null) 
            actionBar.setTitle(s);

    }


    // Exit APP when click back key twice
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (event.getDownTime() - mLastPressedTime < PERIOD) {
                        finish();

                    } else {

                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();
                        mLastPressedTime = event.getEventTime();
                    }
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 'Find' the menu items that should not be displayed - each Fragment's menu has been contributed at this point.
            MenuItem refreshIcon = null;
            if(menu.findItem(R.id.frag_menu_refresh) != null) {
                refreshIcon = menu.findItem(R.id.frag_menu_refresh);
                menu.findItem(R.id.frag_menu_refresh).setVisible(false);
            }

            if(menu.findItem(R.id.frag_grid_default_search) != null) {
                refreshIcon = menu.findItem(R.id.frag_grid_default_search);
                menu.findItem(R.id.frag_grid_default_search).setVisible(false);
            }


        }
        return super.onPrepareOptionsMenu(menu);
    }



    //ActionBar Menu Options
  /* public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.menu_About:
                Intent i_about = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(i_about);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;

            case R.id.menu_refresh:
                if(!Globalconstant.isTaskRunning)
                    refreshToken();
                return true;
            case R.id.menu_settings:
                Intent i_settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i_settings);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

	*/







/*

    //AsyncTask to download DATA from server

    class ProgressTask extends AsyncTask<String, Integer, JSONObject> {


        protected void onPreExecute() {
            code = session.LoadPreference("Code");
            refresh_token = session.LoadPreference("refresh_token");
        }


        protected void onPostExecute(final JSONObject json) {

            if (json != null) {
                try {
                    String token = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String refresh = json.getString("refresh_token");


                    // Save access token in shared preferences
                    session.savePreferences("access_token", json.getString("access_token"));
                    session.savePreferences("expires_in", json.getString("expires_in"));
                    session.savePreferences("refresh_token", json.getString("refresh_token"));

                    Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                    calendar.add(Calendar.SECOND, 3600);
                    session.savePreferences("expires_on", calendar.getTime().toString());

                    //Get data from server
                    syncData();

                    if (Globalconstant.LOG) {

                        Log.d("refresh_token - Expire", expire);
                        Log.d("refresh_token - Refresh", refresh);
                        Log.d("expires_on", json.getString("exwpires_on"));
                    }

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

*/
}
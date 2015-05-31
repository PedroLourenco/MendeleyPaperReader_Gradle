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

}
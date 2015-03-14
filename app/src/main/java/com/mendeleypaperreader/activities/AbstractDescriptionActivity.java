package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.utl.RobotoRegularFontHelper;
import com.mendeleypaperreader.utl.TypefaceSpan;

/**
 * Class to display full abstract of pdf articles.
 *
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class AbstractDescriptionActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abstract_description);

        TextView tvAbstractValue = (TextView) findViewById(R.id.abstract_description);
        RobotoRegularFontHelper.applyFont(getApplicationContext(),tvAbstractValue);
        tvAbstractValue.setText(getAbstract());


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

}

package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.RobotoRegularFontHelper;
import com.mendeleypaperreader.util.TypefaceSpan;

/**
 * This class display actual version, developer contact and version history.
 *
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class AboutActivity extends Activity {

    private NumberProgressBar progressBar;
    private Preferences session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);

            SpannableString s = new SpannableString("Paper Reader");
            TypefaceSpan tf = new TypefaceSpan(this, "Roboto-Bold.ttf");

            s.setSpan(tf, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance

            actionBar.setTitle(s);
        }

        session = new Preferences(getApplicationContext());
        
        TextView msg_about = (TextView) findViewById(R.id.about_msg_about_text);
        TextView txt_version3 = (TextView) findViewById(R.id.about_version3_text);
        TextView web_link = (TextView) findViewById(R.id.about_web_link_text);
        TextView email = (TextView) findViewById(R.id.about_email_value);
        TextView current_version = (TextView) findViewById(R.id.about_current_version_text);


        RobotoRegularFontHelper.applyFont(AboutActivity.this, current_version);
        RobotoRegularFontHelper.applyFont(AboutActivity.this, msg_about);
        RobotoRegularFontHelper.applyFont(AboutActivity.this, web_link);
        RobotoRegularFontHelper.applyFont(AboutActivity.this, txt_version3);
        RobotoRegularFontHelper.applyFont(AboutActivity.this, email);

        SpannableStringBuilder builder = new SpannableStringBuilder();

        String msg = getResources().getString(R.string.about_contact);
        SpannableString whiteSpannable = new SpannableString(msg);
        whiteSpannable.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, msg.length(), 0);
        builder.append(whiteSpannable);

        String e_mail = getResources().getString(R.string.about_email);
        SpannableString redSpannable = new SpannableString(e_mail);
        redSpannable.setSpan(new ForegroundColorSpan(Color.GRAY), 0, e_mail.length(), 0);
        builder.append(redSpannable);

        progressBar = (NumberProgressBar) findViewById(R.id.progress_bar);
        if(ServiceIntent.serviceState) {
            progressBar.setProgress(View.VISIBLE);
            progressBar.setProgress(session.LoadPreferenceInt("progress"));
        }

        
        
        email.setText(builder, BufferType.SPANNABLE);

        email.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:pdrolourenco@gmail.com"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                startActivity(intent);
            }
        });


        web_link.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.about_web_link)));
                startActivity(browserIntent);
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                startActivity(browserIntent);
            }
        });

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
            progressBar.setProgress(session.LoadPreferenceInt("progress"));

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastUpdateProgressBar);
            registerReceiver(mReceiver, mIntentFilter);
        }

        if(session.LoadPreferenceInt("progress") == 100) {
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
                session.savePreferencesInt("progress", progress.intValue());

            }

            if(progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);
                }

        }
    };




}
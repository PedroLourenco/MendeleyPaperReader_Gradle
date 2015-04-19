package com.mendeleypaperreader.activities;


import android.app.ActionBar;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.adapter.ListTitleAdapter;
import com.mendeleypaperreader.adapter.MergeAdapter;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.providers.ContentProvider;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.TypefaceSpan;

public class ReadersActivity extends ListActivity {

    private Cursor cursorAcademicStatus;
    private SimpleCursorAdapter mAdapterAcademicStatus;
    private NumberProgressBar progressBar;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readers);


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



        preferences = new Preferences(getApplicationContext());
;
        progressBar = (NumberProgressBar) findViewById(R.id.progress_bar);
        if(ServiceIntent.serviceState) {

            progressBar.setProgress(preferences.LoadPreferenceInt("progress"));
        }else{
            progressBar.setVisibility(View.GONE);

        }
        
        TextView redersValue = (TextView) findViewById(R.id.readersValue);
        redersValue.setText(getCounterValue());

        cursorAcademicStatus = getAcademicStatus();

        String[] dataColumnsAcademic = {"_id", DatabaseOpenHelper.COUNT};
        int[] viewIDsAcademic = {R.id.readersStatus, R.id.readersCount};
        mAdapterAcademicStatus = new SimpleCursorAdapter(getApplicationContext(), R.layout.readers_list, cursorAcademicStatus, dataColumnsAcademic, viewIDsAcademic, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // Add section to list and merge two adatpers
        MergeAdapter mergeAdapter = new MergeAdapter();

        mergeAdapter.addAdapter(new ListTitleAdapter(getApplicationContext(), getResources().getString(R.string.academicStatus), mAdapterAcademicStatus, R.layout.listview_section_header));
        mergeAdapter.addAdapter(mAdapterAcademicStatus);

        mergeAdapter.setNoItemsText("Nothing to display. This list is empty.");

        setListAdapter(mergeAdapter);


    }


    private String getDocId() {

        Bundle bundle = getIntent().getExtras();
        return bundle.getString("DOC_ID");
    }


    private String getCounterValue() {

        Bundle bundle = getIntent().getExtras();
        return bundle.getString("READER_VALUE");
    }


    private Cursor getAcademicStatus() {

        String docId = getDocId();

        String[] projection = new String[]{DatabaseOpenHelper.STATUS + " as _id", DatabaseOpenHelper.COUNT};
        String selection = DatabaseOpenHelper.DOC_DETAILS_ID + " = '" + docId + "'";
        String orderBy = DatabaseOpenHelper.STATUS + " ASC";
        Uri uri = Uri.parse(ContentProvider.CONTENT_URI_ACADEMIC_DOCS + "/id");

        return getApplicationContext().getContentResolver().query(uri, projection, selection, null, orderBy);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursorAcademicStatus.close();
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

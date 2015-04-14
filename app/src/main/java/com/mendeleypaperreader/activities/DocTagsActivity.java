package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.Provider.ContentProvider;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.RobotoBoldFontHelper;
import com.mendeleypaperreader.util.RobotoRegularFontHelper;
import com.mendeleypaperreader.util.TypefaceSpan;

public class DocTagsActivity extends ListActivity {

    private Cursor cursorDocTags;
    private CustomListSimpleCursorAdapter adapterDocTags;
    private Preferences preferences;
    private NumberProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_tags);

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

        cursorDocTags = getDocTags();

        TextView tvTag = (TextView) findViewById(R.id.tag_doc_text);
        RobotoRegularFontHelper.applyFont(getApplicationContext(), tvTag);
        tvTag.setText("Tags");
        
        TextView tvDocTagTitle = (TextView) findViewById(R.id.tag_doc_title);
        RobotoRegularFontHelper.applyFont(getApplicationContext(), tvDocTagTitle);
        tvDocTagTitle.setText(getDocTitle());


        String text = getResources().getString(R.string.list_title) + "  " + getDocTitle();
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, 0);
        tvDocTagTitle.setText(builder);



        String[] dataColumnsTags = {"_id"};
        int[] viewDocTags = {R.id.tagName};   //criar nova lista para as tags com um novo icon
        adapterDocTags = new CustomListSimpleCursorAdapter(getApplicationContext(), R.layout.list_row_with_image_tag, cursorDocTags, dataColumnsTags, viewDocTags, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        setListAdapter(adapterDocTags);

    }

    /**
     * @return document id
     */
    private String getDocId() {

        Bundle bundle = getIntent().getExtras();
        return bundle.getString("DOC_ID");
    }

    /**
     * @return document Title
     */
    private String getDocTitle() {

        Bundle bundle = getIntent().getExtras();
        return bundle.getString("DOC_TITLE", null);
    }


    /**
     * @return cursor with data to fill the listview
     */
    private Cursor getDocTags() {

        String docId = getDocId();

        String[] projection = new String[]{DatabaseOpenHelper.TAG_NAME + " as _id"};
        String selection = DatabaseOpenHelper._ID + " = '" + docId + "'";
        String orderBy = DatabaseOpenHelper.TAG_NAME + " ASC";
        Uri uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_TAGS + "/id");

        return getApplicationContext().getContentResolver().query(uri, projection, selection, null, orderBy);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Cursor tags = adapterDocTags.getCursor();
        String description = tags.getString(tags.getColumnIndex("_id"));

        Intent listDocTag = new Intent(getApplicationContext(), ListDocTagsActivity.class);
        listDocTag.putExtra("TAG_NAME", description);
        startActivity(listDocTag);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        

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
            mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

            registerReceiver(mReceiver, mIntentFilter);
        }

        if(preferences.LoadPreferenceInt("progress") == 100) {
            progressBar.setVisibility(View.GONE);

        }

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globalconstant.mBroadcastIntegerAction)) {

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










    private class CustomListSimpleCursorAdapter extends SimpleCursorAdapter
    {

        public CustomListSimpleCursorAdapter(final Context context, final int layout, final Cursor c, final String[] from, final int[] to, final int flags) {
            super(context, layout, c, from, to, flags);

        }



        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);

            final TextView tvTagName = (TextView) view.findViewById(R.id.tagName);
            RobotoBoldFontHelper.applyFont(context, tvTagName);

        }
    }


}
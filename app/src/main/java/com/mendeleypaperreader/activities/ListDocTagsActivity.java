package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
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

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.utl.RobotoBoldFontHelper;
import com.mendeleypaperreader.utl.RobotoRegularFontHelper;
import com.mendeleypaperreader.utl.TypefaceSpan;

public class ListDocTagsActivity extends ListActivity {

    private Cursor cursorDocTags;
    CustomListSimpleCursorAdapter adapterDocTags;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_doc_tags);

        


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

        cursorDocTags = getDocTags();

        TextView tag = (TextView) findViewById(R.id.docTag);
        RobotoRegularFontHelper.applyFont(getApplicationContext(), tag);
        tag.setText(R.string.documents);
        TextView docTagTitle = (TextView) findViewById(R.id.docTitle);
        RobotoRegularFontHelper.applyFont(getApplicationContext(), docTagTitle);
        String text = getResources().getString(R.string.list_tag) + "  " + getTagTitle();
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, 0);
        docTagTitle.setText(builder);

        String[] dataColumns = {"_id", DatabaseOpenHelper.AUTHORS, "data"};
        int[] viewIDs = {R.id.Doctitle, R.id.authors, R.id.data};
        adapterDocTags = new CustomListSimpleCursorAdapter(getApplicationContext(), R.layout.list_row_all_doc, cursorDocTags, dataColumns, viewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        setListAdapter(adapterDocTags);

    }


    /**
     * @return tag description
     */
    private String getTagTitle() {

        Bundle bundle = getIntent().getExtras();
        return bundle.getString("TAG_NAME", null);
    }


    /**
     * @return cursor with data to fill the listview
     */
    private Cursor getDocTags() {

        String docTag = getTagTitle();

        String[] projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
        String selection = DatabaseOpenHelper._ID + " in (SELECT " + DatabaseOpenHelper._ID + " FROM " + DatabaseOpenHelper.TABLE_DOC_TAGS + " WHERE " + DatabaseOpenHelper.TAG_NAME + " = '" + docTag + "')";
        Uri uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

        return getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Cursor tags = adapterDocTags.getCursor();
        String title = tags.getString(tags.getColumnIndex("_id"));

        Cursor c1 = getDocId(title);
        c1.moveToPosition(0);
        String doc_id = c1.getString(c1.getColumnIndex(DatabaseOpenHelper._ID));

        Intent doc_details = new Intent(getApplicationContext(), DocumentsDetailsActivity.class);
        doc_details.putExtra("DOC_ID", doc_id);
        startActivity(doc_details);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

    }


    private Cursor getDocId(String doc_title) {

        String[] projection = new String[]{DatabaseOpenHelper._ID};

        if (doc_title.contains("'")) {
            doc_title = doc_title.replaceAll("'", "''");
        }

        String selection = DatabaseOpenHelper.TITLE + " = '" + doc_title + "'";
        Uri uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

        return getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);
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
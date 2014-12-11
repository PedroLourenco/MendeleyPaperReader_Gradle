package com.mendeleypaperreader.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.utl.Globalconstant;

public class ListDocTagsActivity extends ListActivity {

    private Cursor cursorDocTags;
    SimpleCursorAdapter adapterDocTags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_doc_tags);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        cursorDocTags = getDocTags();

        TextView tag = (TextView) findViewById(R.id.docTag);
        tag.setText(R.string.documents);
        TextView docTagTitle = (TextView) findViewById(R.id.docTitle);
        String text = getResources().getString(R.string.list_tag) + "  " + getTagTitle();
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, 0);
        docTagTitle.setText(builder);

        String[] dataColumns = {"_id", DatabaseOpenHelper.AUTHORS, "data"};
        int[] viewIDs = {R.id.Doctitle, R.id.authors, R.id.data};
        adapterDocTags = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_row_all_doc, cursorDocTags, dataColumns, viewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

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


}
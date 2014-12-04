package com.mendeleypaperreader.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
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

public class DocTagsActivity extends ListActivity {

    private Cursor cursorDocTags;
    SimpleCursorAdapter adapterDocTags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_tags);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        cursorDocTags = getDocTags();

        TextView tag = (TextView) findViewById(R.id.docTag);
        tag.setText("Tags");
        TextView docTagTitle = (TextView) findViewById(R.id.docTitle);
        docTagTitle.setText(getDocTitle());


        String text = getResources().getString(R.string.list_title) + "  " + getDocTitle();
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, 0);
        docTagTitle.setText(builder);



        String[] dataColumnsTags = {"_id"};
        int[] viewDocTags = {R.id.tagName};   //criar nova lista para as tags com um novo icon
        adapterDocTags = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_row_with_image_tag, cursorDocTags, dataColumnsTags, viewDocTags, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

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
        Uri uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_TAGS + "/id");

        return getApplicationContext().getContentResolver().query(uri, projection, selection, null, orderBy);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Cursor tags = adapterDocTags.getCursor();
        String description = tags.getString(tags.getColumnIndex("_id"));

        Log.d(Globalconstant.TAG, " position: " + position);
        Log.d(Globalconstant.TAG, " tag name: " + description);


        Intent listDocTag = new Intent(getApplicationContext(), ListDocTagsActivity.class);
        listDocTag.putExtra("TAG_NAME", description);
        startActivity(listDocTag);
        // Otherwise we need to launch a new activity to display
        // the dialog fragment with selected text.


    }

}
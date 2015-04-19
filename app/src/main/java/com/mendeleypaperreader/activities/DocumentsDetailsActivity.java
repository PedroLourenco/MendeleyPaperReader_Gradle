package com.mendeleypaperreader.activities;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.providers.ContentProvider;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.parser.SyncDataAsync;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.service.DownloaderThread;
import com.mendeleypaperreader.service.RefreshTokenTask;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.util.GetDataBaseInformation;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.NetworkUtil;
import com.mendeleypaperreader.util.RobotoBoldFontHelper;
import com.mendeleypaperreader.util.RobotoRegularFontHelper;
import com.mendeleypaperreader.util.TypefaceSpan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class DocumentsDetailsActivity extends Activity {

    // private Cursor foldersAdapter;
    private TextView doc_abstract, doc_url, doc_pmid, doc_issn, doc_catalog, readerCounterValue, doc_tags, docNotes;
    private String docId, mAbstract, t_doc_url, issn, doi, pmid, doc_title, doc_authors_text, doc_source_text, readerValue, isDownloaded, tags, notes;
    private static Preferences session;
    private static String code;
    private static String refresh_token;
    private Boolean isInternetPresent = false;
    private Cursor cursorDetails;
    private Cursor cursorFile;
    private Thread downloaderThread;
    private ImageView download;
    private ProgressDialog progressDialog;
    private DocumentsDetailsActivity thisActivity;
    private GetDataBaseInformation getDataBaseInformation;
    private IntentFilter mIntentFilter;
    private NumberProgressBar progressBar;


    // Used to communicate state changes in the DownloaderThread
    public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
    public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
    public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
    public static final int MESSAGE_CONNECTING_STARTED = 1004;
    public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents_details);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);


            SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
            TypefaceSpan tf = new TypefaceSpan(this, "Roboto-Bold.ttf");

            s.setSpan(tf, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance

            actionBar.setTitle(s);
        }

        session = new Preferences(DocumentsDetailsActivity.this);

         progressBar = (NumberProgressBar) findViewById(R.id.progress_bar);
        if(ServiceIntent.serviceState) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(SyncDataAsync.progressBarValue);

        }

        //register receiver
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Globalconstant.mBroadcastUpdateProgressBar);

        registerReceiver(mReceiver, mIntentFilter);

        //final ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());

        thisActivity = this;
        downloaderThread = null;
        progressDialog = null;
        doc_abstract = new TextView(this);
        doc_url = new TextView(this);
        doc_pmid = new TextView(this);
        doc_issn = new TextView(this);
        doc_catalog = new TextView(this);
        readerCounterValue = new TextView(this);
        doc_tags = new TextView(this);
        docNotes = new TextView(this);
        getDataBaseInformation = new GetDataBaseInformation(getApplicationContext());


        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_abstract);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_url);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_pmid);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_issn);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_catalog);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, readerCounterValue);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_tags);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, docNotes);
        
        
        docId = getDocId();

        //Get to populate activity
        fillData(getdocDetails());
        getFile();

        //Onlcick on abstract
        OnClickListener click_on_abstract = new OnClickListener() {

            public void onClick(View v) {
                Intent abstract_intent = new Intent(DocumentsDetailsActivity.this, AbstractDescriptionActivity.class);
                abstract_intent.putExtra("abstract", mAbstract);
                startActivity(abstract_intent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        };


        if (!mAbstract.isEmpty()) {
            doc_abstract.setOnClickListener(click_on_abstract);
        }


        //Onlcick on NOTES
        OnClickListener click_on_notes = new OnClickListener() {

            public void onClick(View v) {

                Intent notesIntent = new Intent(getApplicationContext(), AbstractDescriptionActivity.class);
                notesIntent.putExtra("abstract", notes);
                startActivity(notesIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        };


        if (!notes.isEmpty()) {
            docNotes.setOnClickListener(click_on_notes);
        }


        //Onlcick on tag
        OnClickListener click_on_tag = new OnClickListener() {

            public void onClick(View v) {


                Intent tag_intent = new Intent(getApplicationContext(), DocTagsActivity.class);
                tag_intent.putExtra("DOC_TITLE", doc_title);
                tag_intent.putExtra("DOC_ID", docId);
                startActivity(tag_intent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                
            }
        };

        if (!tags.isEmpty()) {
            doc_tags.setOnClickListener(click_on_tag);
        }

        //Onlcick on readersCounter
        OnClickListener click_on_readers = new OnClickListener() {


            public void onClick(View v) {

                Intent readersIntent = new Intent(getApplicationContext(), ReadersActivity.class);
                readersIntent.putExtra("DOC_ID", docId);
                readersIntent.putExtra("READER_VALUE", readerValue);
                startActivity(readersIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        };

        if (!readerValue.equals("0"))
            readerCounterValue.setOnClickListener(click_on_readers);

        

        //onclick on url link
        OnClickListener click_on_url = new OnClickListener() {

            public void onClick(View v) {

                openBrowser(t_doc_url);
            }
        };

        doc_url.setOnClickListener(click_on_url);


        //onclick on PMID link
        OnClickListener click_on_pmid = new OnClickListener() {

            public void onClick(View v) {

                String url = Globalconstant.PMID_URL + pmid;
                openBrowser(url);

            }
        };

        doc_pmid.setOnClickListener(click_on_pmid);


        //onclick on ISSN link
        OnClickListener click_on_issn = new OnClickListener() {

            public void onClick(View v) {
                String url = Globalconstant.ISSN_URL + issn;
                openBrowser(url);
            }
        };


        doc_issn.setOnClickListener(click_on_issn);

        //onclick on DOI link
        OnClickListener click_on_doi = new OnClickListener() {

            public void onClick(View v) {
                String url = Globalconstant.DOI_URL + doi;
                openBrowser(url);
            }
        };
        doc_catalog.setOnClickListener(click_on_doi);


        ImageView share = (ImageView) findViewById(R.id.share);

        //onclick on Share button link
        OnClickListener click_on_share_icon = new OnClickListener() {

            public void onClick(View v) {

                // check internet connection

                isInternetPresent = NetworkUtil.isConnectingToInternet(getApplicationContext());

                if (isInternetPresent) {
                    onShareClick(v);
                } else {
                    NetworkUtil.NetWorkDialog(DocumentsDetailsActivity.this, NetworkUtil.DEFAULT_DIALOG);
                }

            }
        };
        share.setOnClickListener(click_on_share_icon);


        download = (ImageView) findViewById(R.id.download);

        if (cursorFile.getCount() <= 0) {
            download.setVisibility(View.INVISIBLE);

        }


        String flagDownload = cursorDetails.getString(cursorDetails.getColumnIndex(DatabaseOpenHelper.IS_DOWNLOAD));

        if (flagDownload != null && flagDownload.equals("YES")) {
            download.setTag("open");
            download.setImageResource(R.drawable.ic_action_read);

        }

        //CLICK onDownload ICON
        OnClickListener click_on_download_icon = new OnClickListener() {

            public void onClick(View v) {

                isInternetPresent = NetworkUtil.isConnectingToInternet(getApplicationContext());

                if (isInternetPresent) {
                    String fileNames = cursorFile.getString(cursorFile.getColumnIndex(DatabaseOpenHelper.FILE_NAME));
                    String mimeType = cursorFile.getString(cursorFile.getColumnIndex(DatabaseOpenHelper.FILE_MIME_TYPE));

                    if (isDownloaded == null) {
                        lockScreenOrientation();
                        String flileId = cursorFile.getString(cursorFile.getColumnIndex(DatabaseOpenHelper._ID));
                        new RefreshTokenTask(DocumentsDetailsActivity.this, false).execute();

                        String access_token = session.LoadPreference("access_token");
                        String url = Globalconstant.get_files_by_doc_id.replace("file_id", flileId) + access_token;
                        downloaderThread = new DownloaderThread(getApplicationContext(),thisActivity, url, flileId, true);
                        downloaderThread.start();
                    } else {

                        //Open file
                        File file = new File(thisActivity.getExternalFilesDir(null).getAbsolutePath() + "/" + fileNames);
                        Intent target = new Intent(Intent.ACTION_VIEW);
                        target.setDataAndType(Uri.fromFile(file), mimeType);
                        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        Intent intent = Intent.createChooser(target, "Open File");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {

                            Log.w(Globalconstant.TAG, " Error opening file");
                            // Instruct the user to install a PDF reader here, or something
                        }
                    }


                } else {
                    NetworkUtil.NetWorkDialog(DocumentsDetailsActivity.this, NetworkUtil.DEFAULT_DIALOG);
                }
            }
        };
        download.setOnClickListener(click_on_download_icon);


    }

    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
           setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
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
            case R.id.menu_refresh:

                if (NetworkUtil.isConnectingToInternet(getApplicationContext())) {
                    if (!ServiceIntent.serviceState) {
                        new RefreshTokenTask(DocumentsDetailsActivity.this, true).execute();
                    } else {
                        Toast.makeText(DocumentsDetailsActivity.this, getResources().getString(R.string.sync_alert_in_progress), Toast.LENGTH_LONG).show();
                    }
                }else{
                    NetworkUtil.NetWorkDialog(DocumentsDetailsActivity.this, NetworkUtil.DEFAULT_DIALOG);
                }
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


    private String getDocId() {

        Bundle bundle = getIntent().getExtras();
        return bundle.getString("DOC_ID");
    }


    private Cursor getdocDetails() {

        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getdocDetails - DOC_DETAILS");

        String[] projection = new String[]{DatabaseOpenHelper.TYPE + " as _id", DatabaseOpenHelper.TITLE, DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE, DatabaseOpenHelper.YEAR, DatabaseOpenHelper.VOLUME, DatabaseOpenHelper.PAGES, DatabaseOpenHelper.ISSUE, DatabaseOpenHelper.ABSTRACT, DatabaseOpenHelper.WEBSITE, DatabaseOpenHelper.DOI, DatabaseOpenHelper.PMID, DatabaseOpenHelper.ISSN, DatabaseOpenHelper.STARRED, DatabaseOpenHelper.READER_COUNT, DatabaseOpenHelper.IS_DOWNLOAD, DatabaseOpenHelper.TAGS};
        String selection = DatabaseOpenHelper._ID + " = '" + docId + "'";
        Uri uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

        cursorDetails = getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);

        return cursorDetails;

    }


    private String getdocNotes() {

        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getdocDetails - DOC_NOTES");


        String[] projection = new String[]{DatabaseOpenHelper.TEXT + " as _id"};
        String selection = DatabaseOpenHelper.DOCUMENT_ID + " = '" + docId + "'";
        Uri uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_NOTES + "/id");

        Cursor cursorNotes = getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);


        if (cursorNotes.getCount() > 0) {
            cursorNotes.moveToPosition(0);

            return cursorNotes.getString(cursorNotes.getColumnIndex(DatabaseOpenHelper._ID));
        }

        return "";

    }

    private Cursor getFile() {
        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getFile - DOC_DETAILS");


        String[] projection = new String[]{DatabaseOpenHelper.FILE_ID + " as _id", DatabaseOpenHelper.FILE_NAME, DatabaseOpenHelper.FILE_MIME_TYPE};
        String selection = DatabaseOpenHelper.FILE_DOC_ID + " = '" + docId + "'";
        Uri uri = Uri.parse(ContentProvider.CONTENT_URI_FILES + "/id");

        cursorFile = getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);
        cursorFile.moveToPosition(0);


        return cursorFile;

    }



    private void fillData(Cursor cursor) {

        cursor.moveToPosition(0);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativedoc);
        relativeLayout.setBackgroundColor(Color.parseColor("#e5e5e5"));

        TextView doc_type = (TextView) findViewById(R.id.docype);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_type);
        String doc_types = cursor.getString(cursor.getColumnIndex("_id"));

        if (doc_types.length() > 0) {
            doc_type.setText(doc_types.substring(0, 1).toUpperCase() + doc_types.substring(1));
        } else {
            doc_type.setText(doc_types);
        }


        isDownloaded = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.IS_DOWNLOAD));

        //Starred icon
        ImageView starred = (ImageView) findViewById(R.id.favorite_star);
        String aux_starred = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.STARRED));

        if (aux_starred.equals("true"))
            starred.setImageResource(R.drawable.ic_action_important);

        //Document Title
        TextView doc_tilte = new TextView(this);
        doc_tilte.setId(3);
        doc_title = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.TITLE));
        doc_tilte.setText(doc_title);
        doc_tilte.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        RobotoBoldFontHelper.applyFont(DocumentsDetailsActivity.this, doc_tilte);
        doc_tilte.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);

        RelativeLayout.LayoutParams layout_doc_tilte = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_tilte.addRule(RelativeLayout.BELOW, R.id.docype);
        layout_doc_tilte.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_tilte.setLayoutParams(layout_doc_tilte);
        relativeLayout.addView(doc_tilte);

        //Document Authors
        TextView doc_authors = new TextView(this);
        doc_authors.setId(4);
        doc_authors_text = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.AUTHORS));
        doc_authors.setText(doc_authors_text);
        doc_authors.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_authors);
        doc_authors.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        doc_authors.setTextColor(Color.parseColor("#000080"));
        RelativeLayout.LayoutParams layout_doc_authors = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_authors.addRule(RelativeLayout.BELOW, doc_tilte.getId());
        layout_doc_authors.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_authors.setLayoutParams(layout_doc_authors);
        relativeLayout.addView(doc_authors);

        //Document Source
        TextView doc_source = new TextView(this);
        doc_source.setId(5);
        doc_source_text = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.SOURCE));
        doc_source.setText(doc_source_text);
        doc_source.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        RobotoBoldFontHelper.applyFont(DocumentsDetailsActivity.this, doc_source);
        doc_source.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        RelativeLayout.LayoutParams layout_doc_source = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_source.addRule(RelativeLayout.BELOW, doc_authors.getId());
        layout_doc_source.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_source.setLayoutParams(layout_doc_source);
        relativeLayout.addView(doc_source);

        //Document Year
        TextView doc_year = new TextView(this);
        doc_year.setId(6);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, doc_year);
        String aux_year = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.YEAR));
        String aux_volume = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.VOLUME));
        String aux_pages = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.PAGES));
        String aux_issue = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.ISSUE));
        String aux_line;


        if (!aux_year.isEmpty() && !aux_volume.isEmpty() && aux_pages.isEmpty() && !aux_issue.isEmpty()) {
            aux_line = aux_year + " vol." + aux_volume + " (" + aux_issue + ")";

        } else if (!aux_year.isEmpty() && !aux_volume.isEmpty() && !aux_pages.isEmpty() && aux_issue.isEmpty()) {
            aux_line = aux_year + " vol." + aux_volume + " pp." + aux_pages;

        } else if (!aux_year.isEmpty() && aux_volume.isEmpty() && !aux_pages.isEmpty() && !aux_issue.isEmpty()) {
            aux_line = aux_year + " (" + aux_issue + ") pp." + aux_pages;

        } else if (aux_year.isEmpty() && !aux_volume.isEmpty() && !aux_pages.isEmpty() && !aux_issue.isEmpty()) {
            aux_line = "vol." + aux_volume + " (" + aux_issue + ") pp." + aux_pages;


        } else if (!aux_year.isEmpty() && aux_volume.isEmpty() && aux_pages.isEmpty() && aux_issue.isEmpty()) {
            aux_line = aux_year;

        } else if (!aux_year.isEmpty() && aux_volume.isEmpty() && !aux_pages.isEmpty() && aux_issue.isEmpty()) {
            aux_line = aux_year + " pp." + aux_pages;


        } else {
            aux_line = aux_year + " vol." + aux_volume + " (" + aux_issue + ") pp." + aux_pages;
        }


        doc_year.setText(aux_line);
        doc_year.setMaxLines(1);
        doc_year.setEllipsize(TruncateAt.END);
        doc_year.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        doc_year.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        RelativeLayout.LayoutParams layout_doc_year = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_year.addRule(RelativeLayout.BELOW, doc_source.getId());
        layout_doc_year.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_year.setLayoutParams(layout_doc_year);
        relativeLayout.addView(doc_year);


        //Line
        RelativeLayout relativeLayout_line = new RelativeLayout(this);
        relativeLayout_line.setId(7);
        relativeLayout_line.setBackgroundColor(Color.parseColor("#cccccc"));
        RelativeLayout.LayoutParams relativeLayout_lines = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.line_height));
        relativeLayout_lines.addRule(RelativeLayout.BELOW, doc_year.getId());
        relativeLayout_line.setLayoutParams(relativeLayout_lines);
        relativeLayout.addView(relativeLayout_line);


        //Document Abstract
        mAbstract = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.ABSTRACT));
        doc_abstract.setId(8);
        doc_abstract.setText(mAbstract);
        doc_abstract.setMaxLines(5);
        doc_abstract.setMinLines(1);
        doc_abstract.setEllipsize(TruncateAt.END);
        doc_abstract.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        doc_abstract.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);

        //Resize arraw
        Drawable image = getApplicationContext().getResources().getDrawable(R.drawable.arrow);
        image.setBounds(0, 0, 20, 20);

        if (!mAbstract.isEmpty())
            doc_abstract.setCompoundDrawables(null, null, image, null);
        RelativeLayout.LayoutParams layout_doc_abstract = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_abstract.addRule(RelativeLayout.BELOW, relativeLayout_line.getId());
        layout_doc_abstract.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_abstract.setLayoutParams(layout_doc_abstract);
        relativeLayout.addView(doc_abstract);


        //Line
        RelativeLayout relativeLayout_line_f = new RelativeLayout(this);
        relativeLayout_line_f.setId(10);
        relativeLayout_line_f.setBackgroundColor(Color.parseColor("#cccccc"));
        RelativeLayout.LayoutParams relativeLayout_lines_f = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.line_height));
        relativeLayout_lines_f.addRule(RelativeLayout.BELOW, doc_abstract.getId());
        relativeLayout_line_f.setLayoutParams(relativeLayout_lines_f);
        relativeLayout.addView(relativeLayout_line_f);


        //Document Tags
        TextView doc_tag_title = new TextView(this);
        doc_tag_title.setId(11);
        doc_tag_title.setText(getResources().getString(R.string.tags));
        RobotoBoldFontHelper.applyFont(DocumentsDetailsActivity.this, doc_tag_title);
        doc_tag_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        doc_tag_title.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);

        RelativeLayout.LayoutParams layout_doc_tag_title = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_tag_title.addRule(RelativeLayout.BELOW, relativeLayout_line_f.getId());
        layout_doc_tag_title.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_tag_title.setLayoutParams(layout_doc_tag_title);
        relativeLayout.addView(doc_tag_title);


        tags = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.TAGS));
        doc_tags.setId(12);
        doc_tags.setBackgroundColor(Color.WHITE);
        doc_tags.setMinLines(1);
        doc_tags.setMaxLines(2);
        doc_tags.setEllipsize(TruncateAt.END);
        if (!tags.isEmpty())
            doc_tags.setCompoundDrawables(null, null, image, null);
        doc_tags.setText(tags);
        doc_tags.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        doc_tags.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        RelativeLayout.LayoutParams layout_doc_tags = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_tags.addRule(RelativeLayout.BELOW, doc_tag_title.getId());
        layout_doc_tags.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_tags.setLayoutParams(layout_doc_tags);
        relativeLayout.addView(doc_tags);

        //Document Notes

        TextView doc_note_title = new TextView(this);
        doc_note_title.setId(13);
        doc_note_title.setText(getResources().getString(R.string.notes));
        RobotoBoldFontHelper.applyFont(DocumentsDetailsActivity.this, doc_note_title);
        doc_note_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        doc_note_title.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        RelativeLayout.LayoutParams layout_doc_note_title = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_note_title.addRule(RelativeLayout.BELOW, doc_tags.getId());
        layout_doc_note_title.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        doc_note_title.setLayoutParams(layout_doc_note_title);
        relativeLayout.addView(doc_note_title);


        notes = getdocNotes();
        docNotes.setId(14);
        docNotes.setBackgroundColor(Color.WHITE);
        docNotes.setMinLines(1);
        docNotes.setMaxLines(2);
        docNotes.setEllipsize(TruncateAt.END);

        if (!notes.isEmpty()) {
            docNotes.setCompoundDrawables(null, null, image, null);
            docNotes.setText(notes);
        }


        docNotes.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        docNotes.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        RelativeLayout.LayoutParams layout_doc_notes = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_doc_notes.addRule(RelativeLayout.BELOW, doc_note_title.getId());
        layout_doc_notes.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
        docNotes.setLayoutParams(layout_doc_notes);
        relativeLayout.addView(docNotes);


        issn = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.ISSN));
        pmid = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.PMID));
        doi = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.DOI));

        TextView doc_catalog_title = new TextView(this);

        if (!issn.isEmpty() || !doi.isEmpty() || !pmid.isEmpty()) {

            //Document Catalog IDS
            doc_catalog_title.setId(15);
            RobotoBoldFontHelper.applyFont(DocumentsDetailsActivity.this, doc_catalog_title);
            doc_catalog_title.setText(getResources().getString(R.string.catalog_ids));
            doc_catalog_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
            doc_catalog_title.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
            RelativeLayout.LayoutParams layout_doc_catalog_title = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layout_doc_catalog_title.addRule(RelativeLayout.BELOW, docNotes.getId());
            layout_doc_catalog_title.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
            doc_catalog_title.setLayoutParams(layout_doc_catalog_title);
            relativeLayout.addView(doc_catalog_title);


            //Document Catalog DOI
            doc_catalog.setId(16);
            doc_catalog.setBackgroundColor(Color.WHITE);
            doc_catalog.setText(getResources().getString(R.string.doi) + "\t\t" + doi);
            doc_catalog.setMaxLines(1);
            doc_catalog.setEllipsize(TruncateAt.END);
            doc_catalog.setCompoundDrawables(null, null, image, null);
            doc_catalog.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
            doc_catalog.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
            RelativeLayout.LayoutParams layout_doc_catalog = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layout_doc_catalog.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
            doc_catalog.setLayoutParams(layout_doc_catalog);

            //Document Catalog PMID
            doc_pmid.setId(18);
            doc_pmid.setBackgroundColor(Color.WHITE);
            doc_pmid.setText(getResources().getString(R.string.pmid) + "\t\t" + pmid);
            doc_pmid.setMaxLines(1);
            doc_pmid.setEllipsize(TruncateAt.END);
            doc_pmid.setCompoundDrawables(null, null, image, null);
            doc_pmid.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
            doc_pmid.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
            RelativeLayout.LayoutParams layout_doc_pmid = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layout_doc_pmid.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
            doc_pmid.setLayoutParams(layout_doc_pmid);

            //Document Catalog ISSN
            doc_issn.setId(20);
            doc_issn.setBackgroundColor(Color.WHITE);
            doc_issn.setText(getResources().getString(R.string.issn) + "\t\t" + issn);
            doc_issn.setMaxLines(1);
            doc_issn.setEllipsize(TruncateAt.END);
            doc_issn.setCompoundDrawables(null, null, image, null);
            doc_issn.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
            doc_issn.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
            RelativeLayout.LayoutParams layout_doc_issn = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layout_doc_issn.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
            doc_issn.setLayoutParams(layout_doc_issn);


            if (!issn.isEmpty() && !doi.isEmpty() && !pmid.isEmpty()) {

                //DOI
                relativeLayout.addView(doc_catalog);
                layout_doc_catalog.addRule(RelativeLayout.BELOW, doc_catalog_title.getId());

                //PMID
                relativeLayout.addView(doc_pmid);
                layout_doc_pmid.addRule(RelativeLayout.BELOW, doc_catalog.getId());

                //ISSN
                layout_doc_issn.addRule(RelativeLayout.BELOW, doc_pmid.getId());
                relativeLayout.addView(doc_issn);
            } else if (!issn.isEmpty() && !doi.isEmpty() && pmid.isEmpty()) {

                //DOI
                relativeLayout.addView(doc_catalog);
                layout_doc_catalog.addRule(RelativeLayout.BELOW, doc_catalog_title.getId());

                //Document Catalog ISSN
                layout_doc_issn.addRule(RelativeLayout.BELOW, doc_catalog.getId());
                relativeLayout.addView(doc_issn);
            } else if (issn.isEmpty() && !doi.isEmpty() && !pmid.isEmpty()) {

                //DOI
                relativeLayout.addView(doc_catalog);
                layout_doc_catalog.addRule(RelativeLayout.BELOW, doc_catalog_title.getId());

                //PMID
                relativeLayout.addView(doc_pmid);
                layout_doc_pmid.addRule(RelativeLayout.BELOW, doc_catalog.getId());
            } else if (!issn.isEmpty() && doi.isEmpty() && !pmid.isEmpty()) {

                //PMID
                relativeLayout.addView(doc_pmid);
                layout_doc_pmid.addRule(RelativeLayout.BELOW, doc_catalog_title.getId());

                //Document Catalog ISSN
                layout_doc_issn.addRule(RelativeLayout.BELOW, doc_pmid.getId());
                relativeLayout.addView(doc_issn);
            } else if (issn.isEmpty() && !doi.isEmpty() && pmid.isEmpty()) {

                //DOI
                relativeLayout.addView(doc_catalog);
                layout_doc_catalog.addRule(RelativeLayout.BELOW, doc_catalog_title.getId());
            } else if (issn.isEmpty() && doi.isEmpty() && !pmid.isEmpty()) {

                //PMID
                relativeLayout.addView(doc_pmid);
                layout_doc_pmid.addRule(RelativeLayout.BELOW, doc_catalog_title.getId());
            } else if (!issn.isEmpty() && doi.isEmpty() && pmid.isEmpty()) {

                //Document Catalog ISSN
                layout_doc_issn.addRule(RelativeLayout.BELOW, doc_catalog_title.getId());
                relativeLayout.addView(doc_issn);
            }

        }


        t_doc_url = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.WEBSITE));

        TextView doc_url_title = new TextView(this);
        RobotoBoldFontHelper.applyFont(DocumentsDetailsActivity.this, doc_url_title);
        RelativeLayout.LayoutParams layout_doc_url_title;
        RelativeLayout.LayoutParams layout_doc_url;
        RelativeLayout.LayoutParams layout_reader_count;

        //fix related bug when url is null
        if (t_doc_url == null) {
            t_doc_url = "";
        }


        if (!t_doc_url.isEmpty()) {

            //Document URL Title
            doc_url_title.setId(22);
            doc_url_title.setText(getResources().getString(R.string.urls));
            doc_url_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
            doc_url_title.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
            layout_doc_url_title = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            doc_url_title.setLayoutParams(layout_doc_url_title);
            relativeLayout.addView(doc_url_title);

            //Document URL
            doc_url.setId(23);
            doc_url.setBackgroundColor(Color.WHITE);
            doc_url.setText(t_doc_url);
            doc_url.setMaxLines(1);
            doc_url.setEllipsize(TruncateAt.END);
            doc_url.setCompoundDrawables(null, null, image, null);
            doc_url.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
            doc_url.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
            layout_doc_url = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layout_doc_url.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingTop), 0, getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingBottom));
            doc_url.setLayoutParams(layout_doc_url);
            layout_doc_url.addRule(RelativeLayout.BELOW, doc_url_title.getId());
            relativeLayout.addView(doc_url);


            if (!issn.isEmpty()) {
                //Document URL Title
                layout_doc_url_title.addRule(RelativeLayout.BELOW, doc_issn.getId());
            } else if (issn.isEmpty() && !pmid.isEmpty()) {
                //Document URL
                layout_doc_url_title.addRule(RelativeLayout.BELOW, doc_pmid.getId());
            } else if (issn.isEmpty() && pmid.isEmpty() && !doi.isEmpty()) {
                //Document URL
                layout_doc_url_title.addRule(RelativeLayout.BELOW, doc_catalog.getId());
            } else {
                //Document URL
                layout_doc_url_title.addRule(RelativeLayout.BELOW, docNotes.getId());
            }

        }


        ///////READER COUNTER - BREAK
        TextView readerCounter = new TextView(this);

        readerCounter.setId(24);
        RobotoRegularFontHelper.applyFont(DocumentsDetailsActivity.this, readerCounter);
        readerCounter.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        readerCounter.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        layout_reader_count = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        readerCounter.setLayoutParams(layout_reader_count);
        relativeLayout.addView(readerCounter);


        //READER COUNTER - VALUE

        RelativeLayout.LayoutParams layoutReaderCounterValue;
        readerCounterValue.setBackgroundColor(Color.WHITE);
        readerValue = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.READER_COUNT));

        //fix related bug when readervalue is null
        if (readerValue == null) {
            readerValue = "0";
        }


        if (!readerValue.equals("0"))
            readerCounterValue.setCompoundDrawables(null, null, image, null);

        readerCounterValue.setId(25);
        readerCounterValue.setText(getResources().getString(R.string.readers) + "\t\t" + readerValue);
        readerCounterValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen.doc_details));
        readerCounterValue.setPadding(getResources().getDimensionPixelOffset(R.dimen.doc_type_paddingLeft), 0, 0, 0);
        layoutReaderCounterValue = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        readerCounterValue.setLayoutParams(layoutReaderCounterValue);
        relativeLayout.addView(readerCounterValue);
        layoutReaderCounterValue.addRule(RelativeLayout.BELOW, readerCounter.getId());


        if (!t_doc_url.isEmpty()) {
            layout_reader_count.addRule(RelativeLayout.BELOW, doc_url.getId());
        } else if (t_doc_url.isEmpty() && !issn.isEmpty()) {
            layout_reader_count.addRule(RelativeLayout.BELOW, doc_issn.getId());
        } else if (t_doc_url.isEmpty() && issn.isEmpty() && !pmid.isEmpty()) {
            layout_reader_count.addRule(RelativeLayout.BELOW, doc_pmid.getId());
        } else if (t_doc_url.isEmpty() && issn.isEmpty() && pmid.isEmpty() && !doi.isEmpty()) {
            layout_reader_count.addRule(RelativeLayout.BELOW, doc_catalog.getId());
        } else {
            layout_reader_count.addRule(RelativeLayout.BELOW, docNotes.getId());
        }
    }


	/*
     * onShareClick - Click on shared icon
	 * 
	 */

    public void onShareClick(View v) {
        Resources resources = getResources();
        String url = "";

        if (!t_doc_url.isEmpty()) {

            url = t_doc_url;

        } else if (!issn.isEmpty()) {

            url = Globalconstant.ISSN_URL + issn;
        } else if (!pmid.isEmpty()) {
            url = Globalconstant.PMID_URL + pmid;
        } else if (!doi.isEmpty()) {
            url = Globalconstant.DOI_URL + doi;

        }


        String email_text = resources.getString(R.string.email_text) + "<br/><br/><b>" + doc_title + "</b><br/><br/>" + resources.getString(R.string.email_authors) + doc_authors_text + "<br/><br/>" + resources.getString(R.string.email_publication) + doc_source_text + "<br/><br/>" + "<br/><br/>" + t_doc_url + "<br/><br/>" + resources.getString(R.string.email_mendeley_profile) + getDataBaseInformation.getProfileInformation(DatabaseOpenHelper.PROFILE_LINK) + "<br/><br/>" + resources.getString(R.string.email_play_store);
        String email_subject_text = getDataBaseInformation.getProfileInformation(DatabaseOpenHelper.PROFILE_DISPLAY_NAME) + resources.getString(R.string.email_subject);
        String sms_text = doc_title;


        if (sms_text.length() > 85) {

            sms_text = sms_text.substring(0, Math.min(sms_text.length(), 85)) + "...";
        }

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(email_text));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, email_subject_text);
        emailIntent.setType("message/rfc822");

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");


        Intent openInChooser = Intent.createChooser(emailIntent, "Chooser Options");

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            } else if (packageName.contains("twitter") || packageName.contains("facebook") || packageName.contains("mms") || packageName.contains("android.gm")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, sms_text + " " + url);
                } else if (packageName.contains("facebook")) {
                    // Warning: Facebook IGNORES our text. They say "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
                    // One workaround is to use the Facebook SDK to post, but that doesn't allow the user to choose how they want to share. We can also make a custom landing page, and the link
                    // will show the <meta content ="..."> text from that page with our link in Facebook.
                    intent.putExtra(Intent.EXTRA_TEXT, sms_text + " " + url);
                } else if (packageName.contains("mms")) {
                    intent.putExtra(Intent.EXTRA_TEXT, sms_text + " " + url);
                } else if (packageName.contains("android.gm")) {
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(email_text));
                    intent.putExtra(Intent.EXTRA_SUBJECT, email_subject_text);
                    intent.setType("message/rfc822");
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
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


    private void openBrowser(String url) {

        Uri uri = Uri.parse(url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setDataAndType(uri, "text/html");
        browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(browserIntent);

    }



    public void onStop() {
        super.onStop();

       cursorDetails.close();
       cursorFile.close();

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

            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastUpdateProgressBar);
            registerReceiver(mReceiver, mIntentFilter);

        }


        if(session.LoadPreferenceInt("progress") == 100) {
            progressBar.setVisibility(View.GONE);
            ServiceIntent.serviceState = false;

        }
    }


    /**
     * This is the Handler for this activity. It will receive messages from the
     * DownloaderThread and make the necessary updates to the UI.
     */
    public Handler activityHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                            /*
                             * Handling MESSAGE_UPDATE_PROGRESS_BAR:
                             * 1. Get the current progress, as indicated in the arg1 field
                             *    of the Message.
                             * 2. Update the progress bar.
                             */
                case MESSAGE_UPDATE_PROGRESS_BAR:
                    if (progressDialog != null) {
                        int currentProgress = msg.arg1;
                        progressDialog.setProgress(currentProgress);
                    }
                    break;
                            
                            /*
                             * Handling MESSAGE_CONNECTING_STARTED:
                             * 1. Get the URL of the file being downloaded. This is stored
                             *    in the obj field of the Message.
                             * 2. Create an indeterminate progress bar.
                             * 3. Set the message that should be sent if user cancels.
                             * 4. Show the progress bar.
                             */
                case MESSAGE_CONNECTING_STARTED:
                    if (msg.obj != null && msg.obj instanceof String) {
                        String url = (String) msg.obj;
                        // truncate the url
                        if (url.length() > 16) {
                            String tUrl = url.substring(0, 15);
                            tUrl += "...";
                            url = tUrl;
                        }
                        String pdTitle = getString(R.string.progress_dialog_title_connecting);
                        String pdMsg = thisActivity.getString(R.string.progress_dialog_message_prefix_connecting);
                        pdMsg += " " + url;

                        dismissCurrentProgressDialog();
                        progressDialog = new ProgressDialog(thisActivity);
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setIndeterminate(true);
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
                        if (!isFinishing())
                            progressDialog.show();
                    }
                    break;
                                    
                            /*
                             * Handling MESSAGE_DOWNLOAD_STARTED:
                             * 1. Create a progress bar with specified max value and current
                             *    value 0; assign it to progressDialog. The arg1 field will
                             *    contain the max value.
                             * 2. Set the title and text for the progress bar. The obj
                             *    field of the Message will contain a String that
                             *    represents the name of the file being downloaded.
                             * 3. Set the message that should be sent if dialog is canceled.
                             * 4. Make the progress bar visible.
                             */
                case MESSAGE_DOWNLOAD_STARTED:
                    // obj will contain a String representing the file name
                    if (msg.obj != null && msg.obj instanceof String) {
                        int maxValue = msg.arg1;
                        String fileName = (String) msg.obj;
                        String pdTitle = thisActivity.getString(R.string.progress_dialog_title_downloading);
                        String pdMsg = thisActivity.getString(R.string.progress_dialog_message_prefix_downloading);
                        pdMsg += " " + fileName;

                        dismissCurrentProgressDialog();
                        progressDialog = new ProgressDialog(thisActivity);
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setProgress(0);
                        progressDialog.setMax(maxValue);
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
                        progressDialog.setCancelable(true);
                        if (!isFinishing())
                            progressDialog.show();
                    }
                    break;
                            
                            /*
                             * Handling MESSAGE_DOWNLOAD_COMPLETE:
                             * 1. Remove the progress bar from the screen.
                             * 2. Display Toast that says download is complete.
                             */
                case MESSAGE_DOWNLOAD_COMPLETE:
                    dismissCurrentProgressDialog();
                    displayMessage(getString(R.string.user_message_download_complete));
                    //change icon
                    download.setTag("open");
                    download.setImageResource(R.drawable.ic_action_read);
                    isDownloaded = "YES";
                    //update column - table documents_details - is_download
                    ContentValues values = new ContentValues();
                    Uri uri_ = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
                    values.put(DatabaseOpenHelper.IS_DOWNLOAD, "YES");
                    String where = DatabaseOpenHelper._ID + " = '" + docId + "'";
                    thisActivity.getContentResolver().update(uri_, values, where, null);
                    break;
                                    
                            /*
                             * Handling MESSAGE_DOWNLOAD_CANCELLED:
                             * 1. Interrupt the downloader thread.
                             * 2. Remove the progress bar from the screen.
                             * 3. Display Toast that says download is complete.
                             */
                case MESSAGE_DOWNLOAD_CANCELED:
                    if (downloaderThread != null) {
                        downloaderThread.interrupt();
                    }
                    dismissCurrentProgressDialog();
                    displayMessage(getString(R.string.user_message_download_canceled));
                    break;
                            
                            /*
                             * Handling MESSAGE_ENCOUNTERED_ERROR:
                             * 1. Check the obj field of the message for the actual error
                             *    message that will be displayed to the user.
                             * 2. Remove any progress bars from the screen.
                             * 3. Display a Toast with the error message.
                             */
                case MESSAGE_ENCOUNTERED_ERROR:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    break;

                default:
                    // nothing to do here
                    break;
            }
        }
    };

    /**
     * If there is a progress dialog, dismiss it and set progressDialog to
     * null.
     */
    public void dismissCurrentProgressDialog() {
        if (progressDialog != null) {
            progressDialog.hide();
           // progressDialog.dismiss();
            progressDialog = null;
            unlockScreenOrientation();
        }
    }

    /**
     * Displays a message to the user, in the form of a Toast.
     *
     * @param message Message to be displayed.
     */
    public void displayMessage(String message) {
        if (message != null) {
            Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
        }
    }


}

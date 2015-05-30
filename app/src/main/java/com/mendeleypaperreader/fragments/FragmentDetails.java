package com.mendeleypaperreader.fragments;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.implments.SwipeItemAdapterMangerImpl;
import com.daimajia.swipe.interfaces.SwipeAdapterInterface;
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface;
import com.daimajia.swipe.util.Attributes;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.activities.AboutActivity;
import com.mendeleypaperreader.activities.DocumentsDetailsActivity;
import com.mendeleypaperreader.activities.SettingsActivity;
import com.mendeleypaperreader.db.Data;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.providers.ContentProvider;
import com.mendeleypaperreader.service.RefreshTokenTask;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.NetworkUtil;
import com.mendeleypaperreader.util.RobotoBoldFontHelper;
import com.mendeleypaperreader.util.RobotoRegularFontHelper;
import com.mendeleypaperreader.util.TypefaceSpan;

import java.util.List;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class FragmentDetails extends Fragment implements LoaderCallbacks<Cursor> {

    private static final String TAG = "FragmentDetails";
    private static final boolean DEBUG = Globalconstant.DEBUG;

    boolean mDualPane;
    private CustomListSimpleCursorAdapter mAdapter;
    private TextView title;
    private SearchView searchView;
    private static final int DETAILS_LOADER = 2;



    private Preferences session;

    private IntentFilter mIntentFilter;
    private NumberProgressBar progressBar;

    private ListView lv;
    private  int selectedPosition;

    private String documentID;


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {

            setHasOptionsMenu(true);

            SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
            TypefaceSpan tf = new TypefaceSpan(getActivity(), "Roboto-Bold.ttf");

            s.setSpan(tf, 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance

            actionBar.setTitle(s);
        }

        session = new Preferences(getActivity().getApplicationContext());

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Globalconstant.mBroadcastUpdateProgressBar);

        getActivity().registerReceiver(mReceiver, mIntentFilter);








    }


    public static FragmentDetails newInstance(int index, String description, int foldersCount) {
        FragmentDetails f = new FragmentDetails();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        args.putString("description", description);
        args.putInt("foldersCount", foldersCount);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        View detailsFrame = getActivity().findViewById(R.id.details);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        int index = getShownIndex();

        String description = getShownDescription();

        View view = inflater.inflate(R.layout.activity_main_menu_details, container, false);
         lv = (ListView) view.findViewById(android.R.id.list);


        if (mDualPane) {
            progressBar = (NumberProgressBar) getActivity().findViewById(R.id.progress_bar_land);
        } else {
            progressBar = (NumberProgressBar) view.findViewById(R.id.progress_bar);
        }

        if (ServiceIntent.serviceState) {
            session = new Preferences(getActivity().getApplicationContext());

            if (progressBar != null) {

                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(session.LoadPreferenceInt("progress"));
            }
        }

        title = (TextView) view.findViewById(R.id.detailTitle);
        RobotoRegularFontHelper.applyFont(getActivity(), title);

        title.setText(description);
        String[] dataColumns = {"_id", DatabaseOpenHelper.AUTHORS, "data"};
        int[] viewIDs = {R.id.Doctitle, R.id.authors, R.id.data};
        mAdapter = new CustomListSimpleCursorAdapter(getActivity(), R.layout.list_row_all_doc, null, dataColumns, viewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        lv.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);

        getActivity().getSupportLoaderManager().initLoader(DETAILS_LOADER, null, this);

       // if (DEBUG) LoaderManager.enableDebugLogging(true);




        return view;

    }


    private String grid_currentQuery = null; // holds the current query...

    final public SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextChange(String newText) {


            View detailsFrame = getActivity().findViewById(R.id.details);
            mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

            grid_currentQuery = newText;

            if (grid_currentQuery != null) {
                getLoaderManager().restartLoader(DETAILS_LOADER, null, FragmentDetails.this);
            }

            return false;
        }


        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
    };


    /**
     * Used when activity are in dual pane
     *
     * @param search - query string
     */
    public void showResults(String search) {

        grid_currentQuery = search;
        getLoaderManager().restartLoader(DETAILS_LOADER, null, FragmentDetails.this);
    }



    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


        String query = getQuery();

        View detailsFrame = getActivity().findViewById(R.id.details);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (mDualPane) {

            inflater.inflate(R.menu.main_menu_activity_actions, menu);
            searchView = (SearchView) menu.findItem(R.id.main_grid_default_search).getActionView();
        } else {
            inflater.inflate(R.menu.action_bar_search, menu);
            searchView = (SearchView) menu.findItem(R.id.frag_grid_default_search).getActionView();
        }


        //chamado quando a action search vem da activity principal
        if (getSearchActivity().equals("true")) {

            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.requestFocus();
            searchView.requestFocusFromTouch();
            searchView.setQuery(query, false);
            grid_currentQuery = query;

            getLoaderManager().restartLoader(DETAILS_LOADER, null, FragmentDetails.this);
            searchView.setOnQueryTextListener(queryListener);
        }

        if (getSearchActivity().equals("false")) {
            searchView.setOnQueryTextListener(queryListener);
        }

    }


    //ActionBar Menu Options
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.menu_About:
                Intent i_about = new Intent(getActivity().getApplicationContext(), AboutActivity.class);
                startActivity(i_about);
                getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;
            case R.id.menu_logout:
                showDialog();
                return true;
            case R.id.frag_menu_refresh:

                if(NetworkUtil.isConnectingToInternet(getActivity().getApplicationContext()))
                    if (!ServiceIntent.serviceState) {
                        //getActivity().getContentResolver().delete(ContentProvider.CONTENT_URI_DELETE_DATA_BASE, null, null);
                        new RefreshTokenTask(getActivity(), false).execute();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.sync_alert_in_progress), Toast.LENGTH_LONG).show();
                    }
                else{
                    NetworkUtil.NetWorkDialog(getActivity(), NetworkUtil.DEFAULT_DIALOG);
                }
                return true;
            case R.id.menu_settings:
                Intent i_settings = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
                startActivity(i_settings);
                getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }




    public void showDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity().getApplicationContext());
        builder.setTitle(getResources().getString(R.string.log_out));
        builder.setMessage(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.word_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Globalconstant.syncAbort = true;
                        session.deleteAllPreferences();
                        getActivity().getContentResolver().delete(ContentProvider.CONTENT_URI_DELETE_DATA_BASE, null, null);
                        ServiceIntent.serviceState = false;
                        getActivity().finish();
                    }
                });

        // on pressing cancel button
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // show dialog
        builder.show();
    }


    public int getShownIndex() {

        int position = getArguments().getInt("index", 1);

        if (position == 0) {
            position = position + 1;
        }

        return position;
    }

    public int getFoldersCount() {

        int position = getArguments().getInt("foldersCount", 1);

        if (position == 0) {
            position = position + 1;
        }

        return position;
    }


    public String getShownDescription() {
        return getArguments().getString("description", "All Documents");
    }

    public String getQuery() {
        return getArguments().getString("searchQuery", "null");
    }

    public String getSearchActivity() {
        return getArguments().getString("searchActivity", "null");
    }

    private Cursor getDocId(String doc_title) {

        String[] projection = new String[]{DatabaseOpenHelper._ID};

        if (doc_title.contains("'")) {
            doc_title = doc_title.replaceAll("'", "''");
        }

        String selection = DatabaseOpenHelper.TITLE + " = '" + doc_title + "'";
        Uri uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

        return getActivity().getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);
    }


    public void onPause() {
        super.onPause();

        if (mDualPane) {
            NumberProgressBar progressBarDual = (NumberProgressBar) getActivity().findViewById(R.id.progress_bar);
            progressBarDual.setVisibility(View.GONE);

        }


    }

    public void onResume() {
        super.onResume();

        // Restart loader so that it refreshes displayed items according to database
        if (DEBUG) Log.d(TAG, "onResume");

        View detailsFrame = getActivity().findViewById(R.id.details);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (mDualPane) {
            getLoaderManager().restartLoader(DETAILS_LOADER, null, this);
        }


        if (mDualPane) {
            progressBar = (NumberProgressBar) getActivity().findViewById(R.id.progress_bar_land);

        } else {
            progressBar = (NumberProgressBar) getActivity().findViewById(R.id.progress_bar);

        }


        if (ServiceIntent.serviceState) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(session.LoadPreferenceInt("progress"));

            mIntentFilter.addAction(Globalconstant.mBroadcastUpdateProgressBar);

            getActivity().registerReceiver(mReceiver, mIntentFilter);

        }
        if (session.LoadPreferenceInt("progress") == 100) {
            progressBar.setVisibility(View.GONE);


        }
        getActivity().invalidateOptionsMenu();


    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globalconstant.mBroadcastUpdateProgressBar)) {

                Float progress = intent.getFloatExtra("Progress", 0);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress.intValue());
                session.savePreferencesInt("progress", progress.intValue());

                getActivity().invalidateOptionsMenu();
            }

            if (progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);
                ServiceIntent.serviceState = false;


            }


        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = null;
        String selection = null;
        int index = getShownIndex();

        if (DEBUG) Log.d(TAG, "index: " + index + "  -  " + "Folder Count: " + getFoldersCount());


        Uri uri = null;

        if (!TextUtils.isEmpty(grid_currentQuery)) {
            String sort = DatabaseOpenHelper.TITLE + " ASC";
            String[] grid_columns = {DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            uri = Uri.parse(ContentProvider.CONTENT_URI_UNION_SEARCH + "/" + DatabaseOpenHelper.TITLE + "/" + DatabaseOpenHelper.AUTHORS);
            return new CursorLoader(getActivity().getApplicationContext(), uri, grid_columns, null, new String[]{"'%" + grid_currentQuery + "%'"}, sort);

        } else if (index == 1) { //All doc

            title.setText(Globalconstant.MYLIBRARY[0]);
            projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            selection = DatabaseOpenHelper.GROUP_ID + " = '' and " + DatabaseOpenHelper.TRASH + " = 'false'";
            uri = ContentProvider.CONTENT_URI_DOC_DETAILS;
        } else if (index == 2) { //added

            title.setText(Globalconstant.MYLIBRARY[1]);
            projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            selection = DatabaseOpenHelper.ADDED + " >= datetime('now', '-30 day')";
            uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
        } else if (index == 3) { //Starred = true

            title.setText(Globalconstant.MYLIBRARY[2]);
            projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            selection = DatabaseOpenHelper.STARRED + " = 'true' and " + DatabaseOpenHelper.GROUP_ID + " = ''";
            uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
        } else if (index == 4) { //Authored = true

            title.setText(Globalconstant.MYLIBRARY[3]);
            projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            selection = DatabaseOpenHelper.AUTHORED + " = 'true'";
            uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
        } else if (index == 5) { //Trash

            title.setText(Globalconstant.MYLIBRARY[4]);
            projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            selection = DatabaseOpenHelper.TRASH + " = 'true'";
            uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");




        } else if (index > 5 && index <= getFoldersCount() + 7) {

            String folderName = getShownDescription();
            if (folderName.contains("'")) {
                folderName = folderName.replaceAll("'", "''");
            }

            title.setText(getShownDescription());
            projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            selection = DatabaseOpenHelper._ID + " in (select doc_details_id from " + DatabaseOpenHelper.TABLE_FOLDERS_DOCS + " where " + DatabaseOpenHelper.FOLDER_ID + " in (select folder_id from " + DatabaseOpenHelper.TABLE_FOLDERS + " where " + DatabaseOpenHelper.FOLDER_NAME + " = '" + folderName + "'))";

            uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
        } else if (index > getFoldersCount()) {

            String groupName = getShownDescription();
            if (groupName.contains("'")) {
                groupName = groupName.replaceAll("'", "''");
            }

            title.setText(getShownDescription());
            projection = new String[]{DatabaseOpenHelper.TITLE + " as _id", DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
            selection = DatabaseOpenHelper.GROUP_ID + " in (select _id from " + DatabaseOpenHelper.TABLE_GROUPS + " where " + DatabaseOpenHelper.GROUPS_NAME + " =  '" + groupName + "')";

            uri = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
        }

        return new CursorLoader(getActivity().getApplicationContext(), uri, projection, selection, null, null);

    }


    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

        if (isAdded() && !cursor.isClosed()) {
            mAdapter.changeCursor(cursor);
        } else {
            mAdapter.swapCursor(null);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

        if (isAdded()) {
            getLoaderManager().restartLoader(DETAILS_LOADER, null, this);
        } else {
            mAdapter.swapCursor(null);
        }
    }






    private String getDocIdFromAdapter(int position){

        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);
        String title = c.getString(c.getColumnIndex("_id"));
        Cursor c1 = getDocId(title);
        c1.moveToPosition(0);
        return c1.getString(c1.getColumnIndex(DatabaseOpenHelper._ID));

    }



    private class CustomListSimpleCursorAdapter extends SimpleCursorAdapter implements SwipeItemMangerInterface, SwipeAdapterInterface {

        private SwipeItemAdapterMangerImpl mItemManger = new SwipeItemAdapterMangerImpl(this);
        //DIALOG
        public static final int TRASH_DIALOG = 1;
        public static final int DELETE_DIALOG = 2;

        public CustomListSimpleCursorAdapter(final Context context, final int layout, final Cursor c, final String[] from, final int[] to, final int flags) {
            super(context, layout, c, from, to, flags);

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            boolean convertViewIsNull = convertView == null;
            View view = super.getView(position, convertView, parent);
            TextView delete = (TextView) view.findViewById(R.id.delete);

            if(convertViewIsNull) {

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        view.setSelected(false);
                        searchView.setQuery("", false);
                        //cursor with My Library information
                        Cursor c = mAdapter.getCursor();
                        c.moveToPosition(position);
                        String title = c.getString(c.getColumnIndex("_id"));

                        //cursor with Folders information
                        Cursor c1 = getDocId(title);
                        c1.moveToPosition(0);
                        String doc_id = c1.getString(c1.getColumnIndex(DatabaseOpenHelper._ID));

                        Intent doc_details = new Intent(getActivity().getApplicationContext(), DocumentsDetailsActivity.class);
                        doc_details.putExtra("DOC_ID", doc_id);
                        startActivity(doc_details);
                        getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

                    }

                });


            }else{

                view = convertView;
            }
            view.setTag(position);

            if(isDocumentOnMyLibrary(position))
                delete.setText("Trash");
            else
                delete.setText("Delete");


            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        selectedPosition = Integer.parseInt(v.getTag().toString());
                    }
                    return false;
                }
            });





            LinearLayout ll = (LinearLayout) view.findViewById(R.id.llTrashSwipe);

            View.OnClickListener deleteListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isDocumentOnMyLibrary(selectedPosition))
                        deleteDocumentDialog(TRASH_DIALOG, selectedPosition);
                    else
                        deleteDocumentDialog(DELETE_DIALOG, selectedPosition);

                }
            };
            ll.setOnClickListener(deleteListener);


            return view;
        }





        private boolean isDocumentOnMyLibrary(int position){

            Cursor c = mAdapter.getCursor();
            c.moveToPosition(position);
            String title = c.getString(c.getColumnIndex("_id"));
            Cursor c1 = getDocId(title);
            c1.moveToPosition(0);

            if(c1.getCount() <= 0)
                return false;

            documentID = c1.getString(c1.getColumnIndex(DatabaseOpenHelper._ID));

            return Data.isDocumentOnMyLibrary(documentID, getActivity().getApplicationContext());
        }

        private void deleteDocumentDialog(int id,  int position){

                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            switch (id) {

                case TRASH_DIALOG:


                builder.setTitle(getResources().getString(R.string.alert_document_to_trash_title));
                builder.setMessage(getResources().getString(R.string.alert_document_to_trash))
                        .setPositiveButton(getResources().getString(R.string.word_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String documentId =  getDocIdFromAdapter(selectedPosition);

                                Data.sendDocumentToTrash(getActivity().getApplicationContext(), documentId);
                                mAdapter.notifyDataSetChanged();

                            }
                        });

                // on pressing cancel button
                builder.setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAdapter.notifyDataSetChanged();
                                dialog.cancel();
                            }
                        });

                    break;

                case DELETE_DIALOG:


                    builder.setTitle(getResources().getString(R.string.alert_document_delete_title));
                    builder.setMessage(getResources().getString(R.string.alert_document_delete))
                            .setPositiveButton(getResources().getString(R.string.word_ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    String documentId =  getDocIdFromAdapter(selectedPosition);

                                    Data.deleteTrashDocumentById(getActivity().getApplicationContext(), documentId);
                                    mAdapter.notifyDataSetChanged();

                                }
                            });

                    // on pressing cancel button
                    builder.setNegativeButton(getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mAdapter.notifyDataSetChanged();
                                    dialog.cancel();
                                }
                            });

                    break;

                default:
                    builder = null;

            }

                // show dialog
                builder.show();

        }


        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);

            final TextView tvDocTitle = (TextView) view.findViewById(R.id.Doctitle);
            final TextView tvAuthors = (TextView) view.findViewById(R.id.authors);
            final TextView tvData = (TextView) view.findViewById(R.id.data);

            final TextView delete = (TextView) view.findViewById(R.id.delete);

            RobotoBoldFontHelper.applyFont(context, tvDocTitle);
            RobotoRegularFontHelper.applyFont(context, tvAuthors);
            RobotoRegularFontHelper.applyFont(context, tvData);
            RobotoRegularFontHelper.applyFont(context, delete);

        }

        @Override
        public int getSwipeLayoutResourceId(int i) {
            return 0;
        }

        @Override
        public void openItem(int position) {

            mItemManger.openItem(position);
        }

        @Override
        public void closeItem(int position) {
            mItemManger.closeItem(position);
        }

        @Override
        public void closeAllExcept(SwipeLayout swipeLayout) {
            mItemManger.closeAllExcept(swipeLayout);
        }

        @Override
        public void closeAllItems() {

        }

        @Override
        public List<Integer> getOpenItems() {
            return mItemManger.getOpenItems();
        }

        @Override
        public List<SwipeLayout> getOpenLayouts() {
            return mItemManger.getOpenLayouts();
        }

        @Override
        public void removeShownLayouts(SwipeLayout swipeLayout) {

        }

        @Override
        public boolean isOpen(int position) {
            return mItemManger.isOpen(position);
        }

        @Override
        public Attributes.Mode getMode() {
            return mItemManger.getMode();
        }

        @Override
        public void setMode(Attributes.Mode mode) {
            mItemManger.setMode(mode);
        }
    }


}
package com.mendeleypaperreader.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mendeleypaperreader.Provider.ContentProvider;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.activities.AboutActivity;
import com.mendeleypaperreader.activities.DetailsActivity;
import com.mendeleypaperreader.activities.SettingsActivity;
import com.mendeleypaperreader.adapter.ListTitleAdapter;
import com.mendeleypaperreader.adapter.MergeAdapter;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.preferences.Preferences;
import com.mendeleypaperreader.service.RefreshTokenTask;
import com.mendeleypaperreader.service.ServiceIntent;
import com.mendeleypaperreader.util.Globalconstant;
import com.mendeleypaperreader.util.RobotoRegularFontHelper;
import com.mendeleypaperreader.util.TypefaceSpan;

import java.util.Arrays;
import java.util.List;

/**
 * Classname MainMenuFragmentList
 *
 * @author PedroLourenco (pdrolourenco@gmail.com)
 * @date July 8, 2014
 */

public class FragmentList extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String TAG = "FragmentList";
    private static final boolean DEBUG = Globalconstant.DEBUG;


    private boolean mDualPane;
    private int mCurCheckPosition = 0;
    private CustomListSimpleCursorAdapter foldersAdapter;
    private CustomListSimpleCursorAdapter groupsAdapter;
    private String description;
    private int foldersCount;
    private CustomAdapterLibrary lAdapter;
    private static final int FOLDERS_LOADER = 0;
    private static final int GROUPS_LOADER = 1;
    private SearchView searchView;
    private FragmentDetails details;
    private NumberProgressBar progressBar;

    private IntentFilter mIntentFilter;
    private Float progress;
    private static String code;
    private static String refresh_token;
    private Preferences session;


    Integer[] imageId = {R.drawable.alldocuments, R.drawable.clock, R.drawable.starim, R.drawable.person, R.drawable.empty_trash};

    @Override
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
        mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
        mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
        mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

        getActivity().registerReceiver(mReceiver, mIntentFilter);

        //Start upload data from server
        String firstLoad = session.LoadPreference("IS_DB_CREATED");

        if (DEBUG) Log.d(FragmentList.TAG, "firstLoad: " + firstLoad);

        if (!firstLoad.equals("YES")) {

            if (DEBUG) Log.d(FragmentList.TAG, "First Sync");

            new RefreshTokenTask(getActivity(), true).execute();
        }


        // Use a custom adapter so we can have something more than the just the text view filled in.
        lAdapter = new CustomAdapterLibrary(getActivity(), R.id.title, Arrays.asList(Globalconstant.MYLIBRARY));

        String[] foldersDataColumns = {"_id"}; //column DatabaseOpenHelper.FOLDER_NAME
        int[] folderViewIDs = {R.id.title};

        String[] groupsDataColumns = {"_id"}; //column DatabaseOpenHelper.GROUP_NAME

        //foldersAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_row_with_image, null, foldersDataColumns, folderViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        foldersAdapter = new CustomListSimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_row_with_image, null, foldersDataColumns, folderViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        groupsAdapter = new CustomListSimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_row_with_image_groups, null, groupsDataColumns, folderViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // Add section to list and merge two adatpers
        MergeAdapter mergeAdapter = new MergeAdapter();
        mergeAdapter.addAdapter(new ListTitleAdapter(getActivity().getApplicationContext(), getResources().getString(R.string.my_library), lAdapter, R.layout.listview_section_header));
        mergeAdapter.addAdapter(lAdapter);
        mergeAdapter.addAdapter(new ListTitleAdapter(getActivity().getApplicationContext(), getResources().getString(R.string.my_folders), foldersAdapter, R.layout.listview_section_header));
        mergeAdapter.addAdapter(foldersAdapter);
        mergeAdapter.addAdapter(new ListTitleAdapter(getActivity().getApplicationContext(), getResources().getString(R.string.my_groups), groupsAdapter, R.layout.listview_section_header));
        mergeAdapter.addAdapter(groupsAdapter);

        mergeAdapter.setNoItemsText("Nothing to display. This list is empty.");

        setListAdapter(mergeAdapter);

        getActivity().getSupportLoaderManager().initLoader(FOLDERS_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(GROUPS_LOADER, null, this);

        //if (DEBUG) LoaderManager.enableDebugLogging(true);


        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.details);

        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 1);
        }


        if (mDualPane) {

            progressBar = (NumberProgressBar) getActivity().findViewById(R.id.progress_bar_land);
            if (ServiceIntent.serviceState) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(session.LoadPreferenceInt("progress"));
            }

            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showDetails(mCurCheckPosition, description, foldersCount);
        } else {
            progressBar = (NumberProgressBar) getActivity().findViewById(R.id.progress_bar);
            if (ServiceIntent.serviceState) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(session.LoadPreferenceInt("progress"));
            }
        }


        if (ServiceIntent.serviceState && !mDualPane) {
            progressBar.setProgress(session.LoadPreferenceInt("progress"));

            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

            getActivity().registerReceiver(mReceiver, mIntentFilter);


        }

    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (!mDualPane) {
            inflater.inflate(R.menu.main_menu_activity_actions, menu);
            searchView = (SearchView) menu.findItem(R.id.main_grid_default_search).getActionView();
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
            case R.id.main_menu_refresh:
                if (!ServiceIntent.serviceState) {
                       new RefreshTokenTask(getActivity(), true).execute();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.sync_alert_in_progress), Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.menu_settings:
                Intent i_settings = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
                startActivity(i_settings);
                getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;
            // up button

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setTitle(getResources().getString(R.string.log_out));
        builder.setMessage(getResources().getString(R.string.warning))
                .setPositiveButton(getResources().getString(R.string.word_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        session.deleteAllPreferences();
                        getActivity().getContentResolver().delete(ContentProvider.CONTENT_URI_DELETE_DATA_BASE, null, null);
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


    private String grid_currentQuery = null; // holds the current query...

    final private SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextChange(String newText) {

            if (TextUtils.isEmpty(newText)) {
                grid_currentQuery = null;

            } else {
                grid_currentQuery = newText;

            }


            FragmentDetails mDetails = (FragmentDetails)
                    getFragmentManager().findFragmentById(R.id.details);


            if (mDualPane) {
                mDetails.showResults(grid_currentQuery);

            } else if (!mDualPane && !TextUtils.isEmpty(newText)) {
                // Otherwise we need to launch a new activity to display
                // the dialog fragment with selected text.

                Intent intent = new Intent();
                intent.setClass(getActivity(), DetailsActivity.class);
                intent.putExtra("index", 1);
                intent.putExtra("description", "All Documents");
                intent.putExtra("foldersCount", foldersCount);
                intent.putExtra("searchQuery", grid_currentQuery);
                intent.putExtra("searchActivity", "true");
                startActivity(intent);
            }

            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {

            return false;
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        int aux_position;
        Cursor c = foldersAdapter.getCursor();
        foldersCount = c.getCount();

        if (searchView != null)
            searchView.setQuery("", false);

        v.setSelected(true);

        // change the background color of the selected element
        l.setSelector(R.drawable.ripple);

        // position <= 5 - fixed folders
        if (position > 0 && position < 5) {
            aux_position = position - 1;
            description = lAdapter.getItem(aux_position);
            showDetails(position, description, foldersCount);

        } else if (position > 6 && position < c.getCount() + 7) {
            //get position from folders cursor
            aux_position = position - 7;
            c.moveToPosition(aux_position);
            description = c.getString(c.getColumnIndex("_id"));
            showDetails(position, description, foldersCount);

        } else if (position > foldersCount + 7) {  //Group information
            Cursor groups = groupsAdapter.getCursor();
            description = groups.getString(c.getColumnIndex("_id"));
            showDetails(position, description, foldersCount);
        }
    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    void showDetails(int index, String description, int foldersCount) {
        mCurCheckPosition = index;


        if (mDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            details = (FragmentDetails)
                    getFragmentManager().findFragmentById(R.id.details);


            if (details == null || details.getShownIndex() != index) {


                // Make new fragment to show this selection.
                details = FragmentDetails.newInstance(index, description, foldersCount);

                // Execute a transaction, replacing any existing fragment
                // with this one inside the frame.
                android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.activity_back_in, R.anim.activity_back_out, R.anim.activity_in, R.anim.activity_out);
                ft.replace(R.id.details, details);
                ft.addToBackStack(null);
                ft.commit();
            }

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.

            Intent intent = new Intent();
            intent.setClass(getActivity(), DetailsActivity.class);
            intent.putExtra("index", index);
            intent.putExtra("description", description);
            intent.putExtra("foldersCount", foldersCount);
            intent.putExtra("searchActivity", "false");   //parametro enviado para definir se a searchView vem do DetailsActivity
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {

        switch (loaderID) {
            case FOLDERS_LOADER:

                String[] folderProjection = {DatabaseOpenHelper.FOLDER_NAME + " as _id"};
                if (DEBUG) Log.d(FragmentList.TAG, "LoaderCreate Folders");

                return new CursorLoader(getActivity().getApplicationContext(), ContentProvider.CONTENT_URI_FOLDERS, folderProjection, null, null, null);

            case GROUPS_LOADER:

                String[] groupProjection = {DatabaseOpenHelper.GROUPS_NAME + " as _id"};
                if (DEBUG) Log.d(FragmentList.TAG, "LoaderCreate  Groups");

                return new CursorLoader(getActivity().getApplicationContext(), ContentProvider.CONTENT_URI_GROUPS, groupProjection, null, null, null);


            default:
                // An invalid id was passed in
                return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {
            case FOLDERS_LOADER:
                foldersAdapter.swapCursor(cursor);
                break;

            case GROUPS_LOADER:
                groupsAdapter.swapCursor(cursor);
                break;
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()) {
            case FOLDERS_LOADER:

                if (isAdded()) {
                    getLoaderManager().restartLoader(FOLDERS_LOADER, null, this);
                } else {
                    foldersAdapter.swapCursor(null);
                }
                break;
            case GROUPS_LOADER:

                if (isAdded()) {
                    getLoaderManager().restartLoader(GROUPS_LOADER, null, this);
                } else {
                    groupsAdapter.swapCursor(null);
                }
                break;

        }
    }


    public void onResume() {
        super.onResume();
        // Restart loader so that it refreshes displayed items according to database
        getLoaderManager().restartLoader(FOLDERS_LOADER, null, this);
        getLoaderManager().restartLoader(GROUPS_LOADER, null, this);


        if (ServiceIntent.serviceState && !mDualPane) {
            progressBar.setProgress(session.LoadPreferenceInt("progress"));

            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Globalconstant.mBroadcastStringAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastIntegerAction);
            mIntentFilter.addAction(Globalconstant.mBroadcastArrayListAction);

            getActivity().registerReceiver(mReceiver, mIntentFilter);


        }
        if (session.LoadPreferenceInt("progress") == 100) {
            progressBar.setVisibility(View.GONE);
            //ServiceIntent.serviceState = false;
        }


    }

    public void onPause() {
        super.onPause();
        //if(ServiceIntent.serviceState && !mDualPane) {
        //    getActivity().unregisterReceiver(mReceiver);
        //}
    }

/*
    public void syncData() {
        Intent serviceIntent = new Intent(getActivity(), ServiceIntent.class);
        getActivity().startService(serviceIntent);

    }
*/

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globalconstant.mBroadcastIntegerAction)) {
                if (progressBar != null) {
                    progress = intent.getFloatExtra("Progress", 0);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress.intValue());
                }

                if (progress != null)
                    session.savePreferencesInt("progress", progress.intValue());
            }

            if (progressBar != null && progressBar.getProgress() == 100) {
                progressBar.setVisibility(View.GONE);

            }


        }
    };
/*
    private void refreshToken() {

        // check internet connection

        Boolean isInternetPresent;
        ConnectionDetector connectionDetector = new ConnectionDetector(getActivity().getApplicationContext());

        isInternetPresent = connectionDetector.isConnectingToInternet();

        if (isInternetPresent) {
            getActivity().getContentResolver().delete(ContentProvider.CONTENT_URI_DELETE_DATA_BASE, null, null);
            new ProgressTask().execute();
        } else {
            connectionDetector.showDialog(getActivity(), ConnectionDetector.DEFAULT_DIALOG);
        }
    }

*/
    //AsyncTask to download DATA from server
/*
    class ProgressTask extends AsyncTask<String, Integer, JSONObject> {


        protected void onPreExecute() {
            code = session.LoadPreference("Code");
            refresh_token = session.LoadPreference("refresh_token");
        }


        protected void onPostExecute(final JSONObject json) {

            if (json != null) {
                try {
                    String token = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String refresh = json.getString("refresh_token");


                    // Save access token in shared preferences
                    session.savePreferences("access_token", json.getString("access_token"));
                    session.savePreferences("expires_in", json.getString("expires_in"));
                    session.savePreferences("refresh_token", json.getString("refresh_token"));

                    Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                    calendar.add(Calendar.SECOND, 3600);
                    session.savePreferences("expires_on", calendar.getTime().toString());

                    //Get data from server
                    syncData();

                    if (DEBUG) {

                        Log.d(FragmentList.TAG, "refresh_token - Expire: " + expire);
                        Log.d(FragmentList.TAG, "refresh_token - Refresh: " + refresh);
                        Log.d(FragmentList.TAG, "expires_on" + json.getString("exwpires_on"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        protected JSONObject doInBackground(final String... args) {

            GetAccessToken jParser = new GetAccessToken();

            return jParser.refresh_token(Globalconstant.TOKEN_URL, code, Globalconstant.CLIENT_ID, Globalconstant.CLIENT_SECRET, Globalconstant.REDIRECT_URI, Globalconstant.GRANT_TYPE, refresh_token);

        }
    }
*/

    /**
     * CustomAdapter
     */
    private class CustomAdapterLibrary extends ArrayAdapter<String> {

        private Context mContext;
        private Typeface roboto;

        /**
         * Constructor
         */

        public CustomAdapterLibrary(Context context, int textViewResourceId, List<String> items) {
            super(context, textViewResourceId, items);
            mContext = context;

            roboto = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

        }


        /**
         * getView
         * <p/>
         * Return a view that displays an item in the array.
         */

        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_row_with_image, null, true);
            }


            View itemView = v;
            itemView.setSelected(true);
            TextView txtTitle = (TextView) itemView.findViewById(R.id.title);
            txtTitle.setTypeface(roboto);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.list_image);
            txtTitle.setText(Globalconstant.MYLIBRARY[position]);

            imageView.setImageResource(imageId[position]);

            return itemView;
        }


    }

    private class CustomListSimpleCursorAdapter extends SimpleCursorAdapter {

        public CustomListSimpleCursorAdapter(final Context context, final int layout, final Cursor c, final String[] from, final int[] to, final int flags) {
            super(context, layout, c, from, to, flags);

        }


        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);

            final TextView tvTitle = (TextView) view.findViewById(R.id.title);

            RobotoRegularFontHelper.applyFont(context, tvTitle);

        }
    }


}






package com.mendeleypaperreader.activities;

import java.util.Arrays;
import java.util.List;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.adapter.ListTitleAdapter;
import com.mendeleypaperreader.adapter.MergeAdapter;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.utl.GlobalConstant;

/**
 * Classname MainMenuFragmentList 
 * 	 
 * @date July 8, 2014
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class MainMenuFragmentList extends ListFragment implements LoaderCallbacks<Cursor>{


	boolean mDualPane;
	int mCurCheckPosition = 0;
	SimpleCursorAdapter foldersAdapter;
    SimpleCursorAdapter groupsAdapter;
	private String description;
    private int foldersCount;
	CustomAdapterLibrary  lAdapter;
    private static final int FOLDERS_LOADER = 0;
    private static final int GROUPS_LOADER = 1;


	Integer[] imageId = {R.drawable.alldocuments, R.drawable.clock,	R.drawable.starim, R.drawable.person, R.drawable.empty_trash};

	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);



		// Use a custom adapter so we can have something more than the just the text view filled in.
		lAdapter =  new CustomAdapterLibrary (getActivity (),  R.id.title, Arrays.asList (GlobalConstant.MYLIBRARY));

		String[] foldersDataColumns = {"_id"}; //column DatabaseOpenHelper.FOLDER_NAME
		int[] folderViewIDs = { R.id.title };

        String[] groupsDataColumns = {"_id"}; //column DatabaseOpenHelper.GROUP_NAME

		foldersAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_row_with_image, null, foldersDataColumns, folderViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        groupsAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_row_with_image_groups, null, groupsDataColumns, folderViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

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

		if (GlobalConstant.LOG)
			LoaderManager.enableDebugLogging(true);     



		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.details);

		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurCheckPosition = savedInstanceState.getInt("curChoice", 1);
		}

		if (mDualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// Make sure our UI is in the correct state.
			showDetails(mCurCheckPosition, description, foldersCount);
		}
	}




	@Override 
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		int aux_position;
        Cursor c = foldersAdapter.getCursor();
        foldersCount =  c.getCount();

		if (GlobalConstant.LOG){
			Log.d(GlobalConstant.TAG, "mDualPane  FOLDERS:" + mDualPane);
			Log.d(GlobalConstant.TAG, "mDualPane  position:" + position);
            Log.d(GlobalConstant.TAG, "Folder Count: " + foldersCount);
		}



		// position <= 5 - fixed folders
		if( position > 0 && position < 5){
			aux_position = position - 1;
			description = lAdapter.getItem(aux_position);
			showDetails(position, description, foldersCount);

		}
		else if(position > 6 && position < c.getCount()+7){
			//get position from folders cursor
			aux_position = position - 7;
			c.moveToPosition(aux_position);
			description = c.getString(c.getColumnIndex("_id"));
			showDetails(position, description, foldersCount);
		}
        else if(position > foldersCount + 7){  //Group information
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


		if (GlobalConstant.LOG)
			Log.d(GlobalConstant.TAG, "ITEM SELECTED: " + description);


		if (mDualPane) {

			if (GlobalConstant.LOG)
				Log.d(GlobalConstant.TAG, "mDualPane: " + mDualPane);

			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.
			getListView().setItemChecked(index, true);

			// Check what fragment is currently shown, replace if needed.
			MainMenuActivityFragmentDetails details = (MainMenuActivityFragmentDetails)
					getFragmentManager().findFragmentById(R.id.details);

			if (details == null || details.getShownIndex() != index) {
				// Make new fragment to show this selection.
				details = MainMenuActivityFragmentDetails.newInstance(index, description, foldersCount);

				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
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
			startActivity(intent);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {


        switch (loaderID) {
            case FOLDERS_LOADER:

                String[] folderProjection = {DatabaseOpenHelper.FOLDER_NAME + " as _id"};
                if (GlobalConstant.LOG)
                    Log.d(GlobalConstant.TAG, "onCreateLoader  Folders");

                return new CursorLoader(getActivity().getApplicationContext(), MyContentProvider.CONTENT_URI_FOLDERS, folderProjection, null, null, null);


            case GROUPS_LOADER:

                String[] groupProjection = {DatabaseOpenHelper.GROUPS_NAME + " as _id"};
                if (GlobalConstant.LOG)
                    Log.d(GlobalConstant.TAG, "onCreateLoader  Groups");

                return new CursorLoader(getActivity().getApplicationContext(), MyContentProvider.CONTENT_URI_GROUPS, groupProjection, null, null, null);



            default:
                // An invalid id was passed in
                return null;
        }

    }

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch(loader.getId()) {
            case FOLDERS_LOADER:

                foldersAdapter.swapCursor(cursor);
            break;
            case GROUPS_LOADER:

                groupsAdapter.swapCursor(cursor);
        }

	}



	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

        switch(loader.getId()) {
            case FOLDERS_LOADER:

                if(isAdded()){
                    getLoaderManager().restartLoader(FOLDERS_LOADER, null, this);
                }
                else{
                    foldersAdapter.swapCursor(null);
                }
                break;
            case GROUPS_LOADER:

                if(isAdded()){
                    getLoaderManager().restartLoader(GROUPS_LOADER, null, this);
                }
                else{
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
	} 


	/**
	 * CustomAdapter
	 *
	 */
	private class CustomAdapterLibrary extends ArrayAdapter<String> {

		private Context mContext;

		/**
		 * Constructor
		 */

		public CustomAdapterLibrary(Context context, int textViewResourceId, List<String> items) 
		{
			super(context, textViewResourceId, items);
			mContext = context;
		}

		/**
		 * getView
		 *
		 * Return a view that displays an item in the array.
		 *
		 */

		public View getView (int position, View convertView, ViewGroup parent) 
		{

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate (R.layout.list_row_with_image, null, true);
			}

			View itemView = v;

			TextView txtTitle = (TextView) itemView.findViewById(R.id.title);
			ImageView imageView = (ImageView) itemView.findViewById(R.id.list_image);
			txtTitle.setText(GlobalConstant.MYLIBRARY[position]);

			imageView.setImageResource(imageId[position]);

			return itemView;
		}

	} 
}






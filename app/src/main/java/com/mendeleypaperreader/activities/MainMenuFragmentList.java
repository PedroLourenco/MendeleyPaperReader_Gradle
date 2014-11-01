package com.mendeleypaperreader.activities;

import java.util.Arrays;
import java.util.List;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import com.mendeleypaperreader.utl.Globalconstant;

/**
 * Classname MainMenuFragmentList 
 * 	 
 * @date July 8, 2014
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class MainMenuFragmentList extends ListFragment implements LoaderCallbacks<Cursor>{


	boolean mDualPane;
	int mCurCheckPosition = 0;
	SimpleCursorAdapter mAdapter;
	private String description;  
	CustomAdapterLibrary  lAdapter;


	Integer[] imageId = {R.drawable.alldocuments, R.drawable.clock,	R.drawable.starim, R.drawable.person, R.drawable.empty_trash};

	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);



		// Use a custom adapter so we can have something more than the just the text view filled in.
		lAdapter =  new CustomAdapterLibrary (getActivity (),  R.id.title, Arrays.asList (Globalconstant.MYLIBRARY));

		String[] dataColumns = {"_id"}; //column DatabaseOpenHelper.FOLDER_NAME
		int[] viewIDs = { R.id.title };


		mAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_row_with_image, null, dataColumns, viewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		// Add section to list and merge two adatpers
		MergeAdapter mergeAdapter = new MergeAdapter();
		mergeAdapter.addAdapter(new ListTitleAdapter(getActivity().getApplicationContext(), getResources().getString(R.string.my_library), lAdapter, R.layout.listview_section));
		mergeAdapter.addAdapter(lAdapter);
		mergeAdapter.addAdapter(new ListTitleAdapter(getActivity().getApplicationContext(), getResources().getString(R.string.my_folders), mAdapter, R.layout.listview_section));
		mergeAdapter.addAdapter(mAdapter);

		mergeAdapter.setNoItemsText("Nothing to display. This list is empty.");

		setListAdapter(mergeAdapter);

		getActivity().getSupportLoaderManager().initLoader(0, null, this);

		if (Globalconstant.LOG)
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
			showDetails(mCurCheckPosition, description);
		}
	}




	@Override 
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		int aux_position;
		if (Globalconstant.LOG){
			Log.d(Globalconstant.TAG, "mDualPane  FOLDERS:" + mDualPane);  
			Log.d(Globalconstant.TAG, "mDualPane  position:" + position);
		}

		// position <= 5 - fixed folders
		if( position > 0 && position < 5){
			aux_position = position - 1;
			description = lAdapter.getItem(aux_position);
			showDetails(position, description);

		}
		else if(position > 6){
			//get position from folders cursor
			aux_position = position - 7;

			Cursor c = mAdapter.getCursor();
			c.moveToPosition(aux_position);
			description = c.getString(c.getColumnIndex("_id"));
			showDetails(position, description);
		}
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a
	 * whole new activity in which it is displayed.
	 */
	void showDetails(int index, String description ) {
		mCurCheckPosition = index;


		if (Globalconstant.LOG) 
			Log.d(Globalconstant.TAG, "ITEM SELECTED: " + description);


		if (mDualPane) {

			if (Globalconstant.LOG) 
				Log.d(Globalconstant.TAG, "mDualPane: " + mDualPane);

			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.
			getListView().setItemChecked(index, true);

			// Check what fragment is currently shown, replace if needed.
			MainMenuActivityFragmentDetails details = (MainMenuActivityFragmentDetails)
					getFragmentManager().findFragmentById(R.id.details);

			if (details == null || details.getShownIndex() != index) {
				// Make new fragment to show this selection.
				details = MainMenuActivityFragmentDetails.newInstance(index, description);

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
			startActivity(intent);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		String[] projection = {DatabaseOpenHelper.FOLDER_NAME + " as _id"};
		if (Globalconstant.LOG)
			Log.d(Globalconstant.TAG,"onCreateLoader  Folders");
		Uri uri = MyContentProvider.CONTENT_URI_FOLDERS;
		return new CursorLoader(getActivity().getApplicationContext(), uri, projection, null, null, null);
	}



	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}



	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {

		if(isAdded()){
			getLoaderManager().restartLoader(0, null, this);
		}
		else{
			mAdapter.swapCursor(null);
		}	

	}

	public void onResume() {
		super.onResume();
		// Restart loader so that it refreshes displayed items according to database

		getLoaderManager().restartLoader(0, null, this);
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
			txtTitle.setText(Globalconstant.MYLIBRARY[position]);

			imageView.setImageResource(imageId[position]);

			return itemView;
		}

	} 
}






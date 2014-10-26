package com.mendeleypaperreader;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
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
import android.widget.ListView;
import android.widget.TextView;

import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.utl.Globalconstant;

/**
 * Classname: MainMenuActivityFragmentDetails 
 * 	
 * 
 * @date July 8, 2014
 * @author PedroLourenco (pdrolourenco@gmail.com)
 *
 */

public class MainMenuActivityFragmentDetails  extends ListFragment  implements LoaderCallbacks<Cursor> {

	boolean mDualPane;
	ListView mListView;
	SimpleCursorAdapter mAdapter;
	private CursorLoader mcursor;
	private String description = null;
	TextView title;



	public static MainMenuActivityFragmentDetails newInstance(int index , String description) {
		MainMenuActivityFragmentDetails f = new MainMenuActivityFragmentDetails();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		args.putString("description", description);
		f.setArguments(args);

		return f;
	}



	public int getShownIndex() {

		int position = getArguments().getInt("index", 1);

		if(position == 0){
			position = position+1;
		}

		return position;
	}


	public String getShownDescription() {
		return getArguments().getString("description", "All Documents");
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

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

		int index = getShownIndex();


		description = getShownDescription();

		if (Globalconstant.LOG){
			Log.d(Globalconstant.TAG,"Description Details: " + description );
			Log.d(Globalconstant.TAG,"index Details: " + index );	
		}


		View view = inflater.inflate(R.layout.activity_main_menu_details, container, false);
		ListView lv = (ListView) view.findViewById(android.R.id.list);

		title = (TextView) view.findViewById(R.id.detailTitle);
		title.setTypeface(null, Typeface.BOLD);
		title.setText(description);
		String[] dataColumns = {"_id", Globalconstant.AUTHORS, "data"}; 
		int[] viewIDs = { R.id.Doctitle ,R.id.authors, R.id.data };
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_row_all_doc, null, dataColumns, viewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		lv.setAdapter(mAdapter);

		if (Globalconstant.LOG)
			Log.d(Globalconstant.TAG,"onCreateView  Details");

		getActivity().getSupportLoaderManager().initLoader(1, null, this);

		if (Globalconstant.LOG)
			LoaderManager.enableDebugLogging(true);     

		return view;

	}




	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		if (Globalconstant.LOG)
			Log.d(Globalconstant.TAG, "position: " + position);

		//cursor with My Library information
		Cursor c = mAdapter.getCursor();
		c.moveToPosition(position);
		String title = c.getString(c.getColumnIndex("_id"));

		//cursor with Folders information
		Cursor c1 = getDocId(title);
		c1.moveToPosition(0);
		String doc_id = c1.getString(c1.getColumnIndex(DatabaseOpenHelper._ID));

		if (Globalconstant.LOG) {
			Log.d(Globalconstant.TAG, "doc_id: " + doc_id);
			Log.d(Globalconstant.TAG, "title_description: " + title);
		}

		Intent doc_details = new Intent(getActivity().getApplicationContext(), DocumentsDetailsActivity.class);
		doc_details.putExtra("doc_id", doc_id);
		startActivity(doc_details);

	}




	private Cursor getDocId (String doc_title){

		String[] projection = null;
		String selection = null;

		projection = new String[] {DatabaseOpenHelper._ID };
		
		if (doc_title.contains("'")) {
			doc_title = doc_title.replaceAll("'", "''");
		}
		
		selection = DatabaseOpenHelper.TITLE + " = '" + doc_title +"'";
		Uri uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

		return getActivity().getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);
	}



	public void onResume() {
		super.onResume();
		
		// Restart loader so that it refreshes displayed items according to database
		if (Globalconstant.LOG)
			Log.d(Globalconstant.TAG,"onResume");

		View detailsFrame = getActivity().findViewById(R.id.details);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		if (mDualPane) {
			if (Globalconstant.LOG)
				Log.d(Globalconstant.TAG,"mDualPane");
			getLoaderManager().restartLoader(1, null, this);
		}
	}




	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		String[] projection = null;
		String selection = null;
		int index = getShownIndex();
		if (Globalconstant.LOG){
			Log.d(Globalconstant.TAG,"Loader  Details");
			Log.d(Globalconstant.TAG,"index: " + index );			
		}

		Uri uri = null;

		if(getShownIndex() == 1) { //All doc

			title.setText(Globalconstant.MYLIBRARY[0]);
			projection = new String[] {DatabaseOpenHelper.TITLE + " as _id",  DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"}; 
			uri = MyContentProvider.CONTENT_URI_DOC_DETAILS;
		}
		else if (getShownIndex() == 2){ //added

			title.setText(Globalconstant.MYLIBRARY[1]);
			projection = new String[] {DatabaseOpenHelper.TITLE + " as _id",  DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
			selection = DatabaseOpenHelper.ADDED + " >= datetime('now', 'start of month')";
			uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
		}
		else if (getShownIndex() == 3){ //Starred = true

			title.setText(Globalconstant.MYLIBRARY[2]);
			projection = new String[] {DatabaseOpenHelper.TITLE + " as _id",  DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
			selection = DatabaseOpenHelper.STARRED + " = 'true'";
			uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
		}
		else if (getShownIndex() == 4){ //Authored = true

			title.setText(Globalconstant.MYLIBRARY[3]);
			projection = new String[] {DatabaseOpenHelper.TITLE + " as _id",  DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"};
			selection = DatabaseOpenHelper.AUTHORED + " = 'true'";
			uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
		}

		else if (getShownIndex() == 5){ //Trash

			title.setText(Globalconstant.MYLIBRARY[4]);
		}

		else if (getShownIndex() > 5){

			String folderName = getShownDescription();			
			if (folderName.contains("'")) {
				 folderName = folderName.replaceAll("'", "''");
			}
			
			title.setText(getShownDescription());			
			projection = new String[] {DatabaseOpenHelper.TITLE + " as _id",  DatabaseOpenHelper.AUTHORS, DatabaseOpenHelper.SOURCE + "||" + "' '" + "||" + DatabaseOpenHelper.YEAR + " as data"}; 
			selection = DatabaseOpenHelper._ID + " in (select doc_details_id from " + DatabaseOpenHelper.TABLE_FOLDERS_DOCS +  " where " + DatabaseOpenHelper.FOLDER_ID + " in (select folder_id from " + DatabaseOpenHelper.TABLE_FOLDERS + " where " + DatabaseOpenHelper.FOLDER_NAME + " = '" + folderName + "'))";
			
			uri = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
		}

		mcursor = new CursorLoader(getActivity().getApplicationContext(), uri, projection, selection, null, null);

		return mcursor;
	}





	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {


		if (Globalconstant.LOG)
			Log.d(Globalconstant.TAG,"onLoadFinished  Details - count: " + cursor.getCount() +" - " + isAdded());
		if(isAdded() && !cursor.isClosed()){
			mAdapter.changeCursor(cursor);
		}
		else{
			mAdapter.swapCursor(null);
		}	
	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if (Globalconstant.LOG)
			Log.d(Globalconstant.TAG,"onLoaderReset  Details");
		if(isAdded()){
			getLoaderManager().restartLoader(1, null, this);
		}
		else{
			mAdapter.swapCursor(null);
		}
	}


	public void restarLoader(){

		getLoaderManager().restartLoader(1, null, this);
	}



}
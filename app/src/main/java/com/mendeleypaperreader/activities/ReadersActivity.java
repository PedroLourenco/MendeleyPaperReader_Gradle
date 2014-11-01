package com.mendeleypaperreader.activities;


import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.adapter.ListTitleAdapter;
import com.mendeleypaperreader.adapter.MergeAdapter;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;

public class ReadersActivity extends ListActivity {

	private Cursor cursorAcademicStatus;
	//private Cursor cursorCountryStatus;
	SimpleCursorAdapter mAdapterAcademicStatus;
	//SimpleCursorAdapter mAdapterCountryStatus;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_readers);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		TextView redersValue = (TextView) findViewById(R.id.readersValue);
		redersValue.setText(getCounterValue());

		cursorAcademicStatus = getAcademicStatus();
		//cursorCountryStatus = getCountryStatus();

		String[] dataColumnsAcademic = {"_id", DatabaseOpenHelper.COUNT}; 
		int[] viewIDsAcademic = { R.id.readersStatus, R.id.readersCount };

		//String[] dataColumnsCountry = {"_id", DatabaseOpenHelper.COUNT};
		//int[] viewIDsCountry = { R.id.readersStatus, R.id.readersCount };

		mAdapterAcademicStatus = new SimpleCursorAdapter(getApplicationContext(), R.layout.readers_list, cursorAcademicStatus, dataColumnsAcademic, viewIDsAcademic, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		//mAdapterCountryStatus = new SimpleCursorAdapter(getApplicationContext(), R.layout.readers_list, cursorCountryStatus, dataColumnsCountry, viewIDsCountry, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);


		// Add section to list and merge two adatpers
		MergeAdapter mergeAdapter = new MergeAdapter();

		//mergeAdapter.addAdapter(new ListTitleAdapter(getApplicationContext(), getResources().getString(R.string.countryStatus), mAdapterCountryStatus, R.layout.listview_section));
		//mergeAdapter.addAdapter(mAdapterCountryStatus);
		mergeAdapter.addAdapter(new ListTitleAdapter(getApplicationContext(), getResources().getString(R.string.academicStatus), mAdapterAcademicStatus, R.layout.listview_section));
		mergeAdapter.addAdapter(mAdapterAcademicStatus);

		mergeAdapter.setNoItemsText("Nothing to display. This list is empty.");

		setListAdapter(mergeAdapter);


	}



	private String getDocId(){

		Bundle bundle = getIntent().getExtras();
		return bundle.getString("DOC_ID");
	}


	private String getCounterValue(){

		Bundle bundle = getIntent().getExtras();
		return bundle.getString("READER_VALUE");
	}


	private Cursor getAcademicStatus(){

		String docId = getDocId();
		String[] projection = null;
		String selection = null;
		String orderBy = null;

		projection = new String[] {DatabaseOpenHelper.STATUS + " as _id",  DatabaseOpenHelper.COUNT};
		selection = DatabaseOpenHelper.DOC_DETAILS_ID + " = '" + docId +"'";
		orderBy =  DatabaseOpenHelper.STATUS + " ASC";
		Uri  uri = Uri.parse(MyContentProvider.CONTENT_URI_ACADEMIC_DOCS + "/id");

		return getApplicationContext().getContentResolver().query(uri, projection, selection, null, orderBy);

	}

	/*private Cursor getCountryStatus(){

		String docId = getDocId();
		String[] projection = null;
		String selection = null;
		String orderBy = null;
		
		projection = new String[] {DatabaseOpenHelper.COUNTRY + " as _id",  DatabaseOpenHelper.COUNT};
		selection = DatabaseOpenHelper.DOC_DETAILS_ID + " = '" + docId +"'";
		orderBy =  DatabaseOpenHelper.COUNTRY + " ASC";
		Uri  uri = Uri.parse(MyContentProvider.CONTENT_URI_COUNTRY_DOCS + "/id");

		return getApplicationContext().getContentResolver().query(uri, projection, selection, null, orderBy);

	}
*/
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    cursorAcademicStatus.close();
	    //cursorCountryStatus.close();
	}
	
}

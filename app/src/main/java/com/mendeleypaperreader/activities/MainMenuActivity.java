package com.mendeleypaperreader.activities;


import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.jsonParser.SyncDataAsync;
import com.mendeleypaperreader.sessionManager.GetAccessToken;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.ConnectionDetector;
import com.mendeleypaperreader.utl.Globalconstant;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class MainMenuActivity extends FragmentActivity
{

	private long mLastPressedTime;
	private static final int PERIOD = 2000;
	// Session Manager Class
	private static SessionManager session;
	private static String refresh_token;
	private static String code;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_menu);
		session = new SessionManager(getApplicationContext()); 

		//Start upload data from server	       
        String db_uploded_flag = session.LoadPreference("IS_DB_CREATED");
		if(!db_uploded_flag.equals("YES")){

			refreshToken();
		}
	}



	// Exit APP when click back key twice
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

			switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				if (event.getDownTime() - mLastPressedTime < PERIOD) {
					finish();

				} else {

					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();
					mLastPressedTime = event.getEventTime();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		//MenuInflater inflater = getMenuInflater();
		//inflater.inflate(R.menu.main_menu_activity_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	//ActionBar Menu Options 
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
		case R.id.menu_About:
			Intent i_about = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(i_about);
			return true;

		case R.id.menu_logout:
			showDialog();
			return true;		
		case R.id.menu_refresh :


			refreshToken();
			return true;	
		default:
			return super.onOptionsItemSelected(item);
		}
	}





	/*
	 * Show dialog to prompt user when logout button is pressed
	 */

	public void showDialog() {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainMenuActivity.this);
		builder.setTitle(getResources().getString(R.string.log_out));
		builder.setMessage(getResources().getString(R.string.warning))
		.setPositiveButton(getResources().getString(R.string.word_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				session.deletePreferences();
				getContentResolver().delete(MyContentProvider.CONTENT_URI_DELETE_DATA_BASE,null, null);
				finish();
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


	public void syncData(){

		new SyncDataAsync(MainMenuActivity.this, MainMenuActivity.this).execute();
	}


	private void refreshToken(){

		// check internet connection

        Boolean isInternetPresent;
        ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());

		isInternetPresent = connectionDetector.isConnectingToInternet();

		if(isInternetPresent){
			getContentResolver().delete(MyContentProvider.CONTENT_URI_DELETE_DATA_BASE,null, null);
			new ProgressTask().execute();
		}
		else{
			connectionDetector.showDialog(this, ConnectionDetector.DEFAULT_DIALOG);
		}
	}


	//AsyncTask to download DATA from server

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

					if (Globalconstant.LOG) {
						Log.d("refresh_token - Token Access", token);
						Log.d("refresh_token - Expire", expire);
						Log.d("refresh_token - Refresh", refresh);	
						Log.d("expires_on", json.getString("expires_on"));
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


}
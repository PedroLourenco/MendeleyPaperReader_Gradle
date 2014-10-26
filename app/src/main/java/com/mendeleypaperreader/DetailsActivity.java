package com.mendeleypaperreader;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.jsonParser.SyncDataAsync;
import com.mendeleypaperreader.sessionManager.GetAccessToken;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.ConnectionDetector;
import com.mendeleypaperreader.utl.Globalconstant;

/**
 * Classname: DetailsActivity 
 * 	This activity displays the details using a MainMenuActivityFragmentDetails. This activity is started
 * 	by a MainMenuActivityFragmentList when a title in the list is selected.
 * 	The activity is used only if a MainMenuActivityFragmentDetails is not on the screen.  
 * 
 * @date July 8, 2014
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */



public class DetailsActivity extends FragmentActivity {

	private static SessionManager session;
	private static String code;
	private static String refresh_token;
	private Boolean isInternetPresent = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		session = new SessionManager(DetailsActivity.this); 


		//verify orientation permissions
		if(getResources().getBoolean(R.bool.portrait_only)){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			MainMenuActivityFragmentDetails details = new MainMenuActivityFragmentDetails();
			details.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
		}
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
			refreshToken();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}



	private void syncData(){

		new SyncDataAsync(DetailsActivity.this, DetailsActivity.this).execute();
	}


	private void refreshToken(){

		//delete data from data base and get new access token to start sync

		// check internet connection
		ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());

		isInternetPresent = connectionDetector.isConnectingToInternet();

		if(isInternetPresent){
			getContentResolver().delete(MyContentProvider.CONTENT_URI_DELETE_DATA_BASE,null, null);
			new ProgressTask().execute();
		}
		else{
			connectionDetector.showDialog(DetailsActivity.this, ConnectionDetector.DEFAULT_DIALOG);
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
					
					// Save access token in shared preferences
					session.savePreferences("access_token", json.getString("access_token"));
					session.savePreferences("expires_in", json.getString("expires_in"));
					session.savePreferences("refresh_token", json.getString("refresh_token"));

					//Get data from server
					syncData();
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}



		protected JSONObject doInBackground(final String... args) {

			GetAccessToken jParser = new GetAccessToken();

			JSONObject json = jParser.refresh_token(Globalconstant.TOKEN_URL, code, Globalconstant.CLIENT_ID, Globalconstant.CLIENT_SECRET, Globalconstant.REDIRECT_URI, Globalconstant.GRANT_TYPE, refresh_token);

			return json;


		} 

	}	  

}




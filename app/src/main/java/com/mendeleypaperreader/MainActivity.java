package com.mendeleypaperreader;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.mendeleypaperreader.sessionManager.GetAccessToken;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.ConnectionDetector;
import com.mendeleypaperreader.utl.Globalconstant;

/**
 * Classname: MainActivity 
 * 	 
 * 
 * @date July 8, 2014
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */



public class MainActivity extends Activity {

	private static String CLIENT_ID = "177";
	// Use your own client id

	private static String OAUTH_URL = "https://api-oauth2.mendeley.com/oauth/authorize?";
	private static String OAUTH_SCOPE = "all";
	private Dialog auth_dialog;
	private Boolean isInternetPresent = false;
	// Session Manager Class
	SessionManager session;

	WebView web;
	Button auth;
	SharedPreferences pref;

	// TextView Access;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// verify orientation permissions
		if (getResources().getBoolean(R.bool.portrait_only)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		// Session Manager
		session = new SessionManager(MainActivity.this);

		//delete peferences on update app
		Integer version = 1;
		try
		{
		    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		    Log.d(Globalconstant.TAG, "version: " + version);
		} 
		catch (NameNotFoundException e)
		{
		    e.printStackTrace();
		}

		if(!version.toString().equals(session.LoadPreference("versionCode")))
		{
		    session.deletePreferences();
		    session.savePreferences("versionCode", version.toString());
		}
		
		
		// check internet connection
		final ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());

		isInternetPresent = connectionDetector.isConnectingToInternet();


		// If logged skip login layout	
		if (session.isLogged()) {
			Intent options = new Intent(getApplicationContext(), MainMenuActivity.class);
			startActivity(options);
		}

		// Click on "Sign in" Button and make login
		auth = (Button) findViewById(R.id.auth);
		auth.setOnClickListener(new View.OnClickListener() {
			Dialog auth_dialog;

			@Override
			public void onClick(View arg0) {

				if (isInternetPresent) {

					auth_dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
					auth_dialog.setContentView(R.layout.webviewoauth);
					web = (WebView) auth_dialog.findViewById(R.id.webview);
					web.getSettings().setJavaScriptEnabled(true);
					web.loadUrl(OAUTH_URL + "client_id=" + CLIENT_ID + "&redirect_uri=" + Globalconstant.REDIRECT_URI + "&response_type=code&scope=" + OAUTH_SCOPE);
					web.setWebViewClient(new WebViewClient() {
						boolean authComplete = false;
						Intent resultIntent = new Intent();

						@Override
						public void onPageStarted(WebView view, String url,
								Bitmap favicon) {
							super.onPageStarted(view, url, favicon);
						}

						String authCode;

						@Override
						public void onPageFinished(WebView view, String url) {
							super.onPageFinished(view, url);
							if (url.contains("?code=") && authComplete != true) {
								Uri uri = Uri.parse(url);
								authCode = uri.getQueryParameter("code");

								if (Globalconstant.LOG)
									Log.i(Globalconstant.TAG, "CODE : " + authCode);
								authComplete = true;
								resultIntent.putExtra("code", authCode);
								MainActivity.this.setResult(Activity.RESULT_OK,	resultIntent);
								setResult(Activity.RESULT_CANCELED, resultIntent);

								// save access code in shared preferences
								session.savePreferences("Code", authCode);
								auth_dialog.dismiss();

								new TokenGet().execute();

								//Toast.makeText(getApplicationContext(), "Authorization Code is: " + authCode, Toast.LENGTH_SHORT).show();
							} else if (url.contains("error=access_denied")) {

								if (Globalconstant.LOG)
									Log.i(Globalconstant.TAG, "ACCESS_DENIED_HERE");
								resultIntent.putExtra("code", authCode);
								authComplete = true;
								setResult(Activity.RESULT_CANCELED, resultIntent);
								Toast.makeText(getApplicationContext(),	"Error Occured", Toast.LENGTH_SHORT).show();
							}
						}
					});
					auth_dialog.show();
					auth_dialog.setCancelable(true);
				} else {

					if (Globalconstant.LOG)
						Log.w(Globalconstant.TAG, "NO INTERNET");
					showDialog();
				}
			}
		});

		// New Account Button
		Button newAccount = (Button) findViewById(R.id.NewAccount);
		newAccount.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (isInternetPresent) {
					Intent myWebLink = new Intent(
							android.content.Intent.ACTION_VIEW);
					myWebLink.setData(Uri.parse("https://www.mendeley.com/join/?_section=header&_specific="));
					startActivity(myWebLink);

				} else {
					connectionDetector.showDialog(MainActivity.this, ConnectionDetector.NETWORK_DIALOG);
					showDialog();
				}
			}
		});
	}

	/*
	 * AsyncTask to get access token
	 */
	private class TokenGet extends AsyncTask<String, String, JSONObject> {
		private ProgressDialog pDialog;
		String code;


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage(getResources().getString(R.string.contacting_mendeley));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);

			code = session.LoadPreference("Code");
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... args) {
			GetAccessToken jParser = new GetAccessToken();
			JSONObject json = jParser.getToken(Globalconstant.TOKEN_URL, code, Globalconstant.CLIENT_ID, Globalconstant.CLIENT_SECRET, Globalconstant.REDIRECT_URI, "authorization_code");

			return json;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			pDialog.dismiss();
			if (json != null) {
				try {
					
					// Save access token in shared preferences
					
					session.savePreferences("access_token", json.getString("access_token"));
					session.savePreferences("expires_in", json.getString("expires_in"));
					session.savePreferences("refresh_token", json.getString("refresh_token"));
					Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
					calendar.add(Calendar.SECOND, 3600);
					session.savePreferences("expires_on", calendar.getTime().toString());

					if (Globalconstant.LOG) {
						Log.d(Globalconstant.TAG, "NOW: " + calendar.getTime().toString());
						Log.d(Globalconstant.TAG, "Expires on: " + calendar.getTime());
						Log.d("Token Access", json.getString("access_token"));
						Log.d("Expire", json.getString("expires_in"));
						Log.d("Refresh", json.getString("refresh_token"));
						

					}

					if (!json.getString("access_token").isEmpty()) {

						Intent options = new Intent(getApplicationContext(), MainMenuActivity.class);
						startActivity(options);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				pDialog.dismiss();
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();


		if (auth_dialog != null) {
			auth_dialog.dismiss();
			auth_dialog = null;
		}

	}

	public void showDialog() {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(getResources().getString(R.string.no_network_connection));
		builder.setMessage(getResources().getString(R.string.check_internet_connection)).setPositiveButton(getResources().getString(R.string.wifi_settings),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int which) {

				// Activity transfer to wifi settings
				startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

			}
		});

		builder.setNeutralButton(getResources().getString(R.string.settings_3G),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// Activity transfer to wifi settings
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				MainActivity.this.startActivity(intent);
			}
		});
		// on pressing cancel button
		builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		// show dialog
		builder.show();
	}
}

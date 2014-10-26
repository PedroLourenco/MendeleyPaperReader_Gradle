package com.mendeleypaperreader.utl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.mendeleypaperreader.R;

/**
 * Classname: ConnectionDetector 
 * 	 
 * 
 * @date July 8, 2014
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class ConnectionDetector {

	private Context context;
	public static final int NETWORK_DIALOG = 1; 
	public static final int DEFAULT_DIALOG = 2;

	public ConnectionDetector(Context context){
		this.context = context;
	}

	public boolean isConnectingToInternet(){
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) 
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) 
				for (int i = 0; i < info.length; i++) 
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}

		}
		return false;
	}



	public void showDialog(final Activity activity, int id) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);


		switch(id){

		case NETWORK_DIALOG:
			builder.setTitle(activity.getResources().getString(R.string.no_network_connection));
			builder.setMessage(activity.getResources().getString(R.string.check_internet_connection)).setPositiveButton(activity.getResources().getString(R.string.wifi_settings),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {

					// Activity transfer to wifi settings
					activity.startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

				}
			});

			builder.setNeutralButton(activity.getResources().getString(R.string.settings_3G),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					// Activity transfer to wifi settings
					Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
					activity.startActivity(intent);
				}
			});
			// on pressing cancel button
			builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			break;

		case DEFAULT_DIALOG :
			builder.setTitle(activity.getResources().getString(R.string.no_network_connection));
			builder.setMessage(activity.getResources().getString(R.string.check_internet_connection)).setPositiveButton(activity.getResources().getString(R.string.word_ok),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {

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




}

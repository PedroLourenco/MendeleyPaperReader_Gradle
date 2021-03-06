package com.mendeleypaperreader.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mendeleypaperreader.util.NetworkUtil;

/**
 * Created by pedro on 11/04/15.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int isConnected = NetworkUtil.getConnectivityStatus(context);

        if (isConnected != 0 && ServiceIntent.serviceState)  {
            Intent serviceIntent = new Intent(context, ServiceIntent.class);
            context.startService(serviceIntent);
        }

    }
}

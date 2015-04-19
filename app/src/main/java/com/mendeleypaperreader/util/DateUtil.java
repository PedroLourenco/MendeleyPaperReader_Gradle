package com.mendeleypaperreader.util;

import android.content.Context;
import android.util.Log;

import com.mendeleypaperreader.preferences.Preferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pedro on 13/04/15.
 */
public class DateUtil {

    private static final String TAG = "DateUtil";
    private static final boolean DEBUG = Globalconstant.DEBUG;



    public static boolean TokenExpired(Context context){

        Calendar calendar = Calendar.getInstance();

        Preferences preferences = new Preferences(context);
        String expiresOn = preferences.LoadPreference("expire_date");
        String lastRefresh = preferences.LoadPreference("lastRefreshDate");

        Calendar calExpiresOn = Calendar.getInstance();
        Calendar calLastRefresh = Calendar.getInstance();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date dateExpiresOn = null;
        Date dateLastRefresh = null;


        if(DEBUG) Log.d(TAG, "lastRefresh: " + lastRefresh + "  -  expire_date: " + expiresOn);


        if(expiresOn.equals(""))
            return true;



        try {

            dateExpiresOn = sdf.parse(expiresOn);
            dateLastRefresh = sdf.parse(lastRefresh);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calExpiresOn.setTime(dateExpiresOn);
        calLastRefresh.setTime(dateLastRefresh);

        if(DEBUG) Log.d(TAG, "calendar: " + calLastRefresh.getTime() + "  -  expiresOnDate: " + calExpiresOn.getTime());


        if(calLastRefresh.compareTo(calExpiresOn) > 0){

            if(DEBUG) Log.d(TAG, "Token is valid.");
            return false;
        }else{
            if(DEBUG) Log.d(TAG, "Token expired.");
            return true;
        }

    }

}

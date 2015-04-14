package com.mendeleypaperreader.util;

import android.content.Context;
import android.util.Log;

import com.mendeleypaperreader.preferences.Preferences;

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
        String expiresOn = preferences.LoadPreference("expires_on");

        Calendar expiresOnDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date dateText = null;
        try {
            dateText = sdf.parse(expiresOn);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        expiresOnDate.setTime(dateText);

        if(DEBUG) Log.d(TAG, "calendar: " + calendar.getTime() + "expiresOnDate: " + expiresOnDate.getTime());

        if(calendar.compareTo(expiresOnDate) > 0){
            if(DEBUG) Log.d(TAG, "Token is valid.");
            return false;
        }else{
            if(DEBUG) Log.d(TAG, "Token expired.");
            return true;
        }

    }

}

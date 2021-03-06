package com.mendeleypaperreader.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.mendeleypaperreader.util.Globalconstant;

public class Preferences {

    private static final String TAG = "Preferences";
    private static final boolean DEBUG = Globalconstant.DEBUG;

    // Shared Preferences
    SharedPreferences sharedPreferences;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "MendeleyPaperReaderPREF";


    // Constructor
    public Preferences(Context context) {
        this._context = context;
        sharedPreferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }


    /**
     * @param key
     * @param value
     */

    public void savePreferences(String key, String value) {

        editor.putString(key, value);
        editor.commit();

    }
    public void savePreferencesInt(String key, Integer value) {

        editor.putInt(key, value);
        editor.commit();

    }


    /**
     * @param Key
     * @return
     */

    public String LoadPreference(String Key) {

        return sharedPreferences.getString(Key, "");

    }

    public Integer LoadPreferenceInt(String Key) {

        return sharedPreferences.getInt(Key, 0);

    }

    public void deletePreferences(String Key) {
        editor.remove(Key);
        editor.commit();
    }
    
    

    /**
     * delete all shared preferences
     */
    public void deleteAllPreferences() {
        // Clearing all data from Shared Preferences

        if(DEBUG) Log.d(TAG, "deleteAllPreferences");

        editor.clear();
        editor.commit();

    }

    /**
     * @return
     */
    public boolean isLogged() {
        String access_token = LoadPreference("access_token");

        if (!access_token.isEmpty())
            return true;


        return false;

    }


}

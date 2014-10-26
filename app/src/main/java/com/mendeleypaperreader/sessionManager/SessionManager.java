package com.mendeleypaperreader.sessionManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
    
	
	// Shared Preferences
    SharedPreferences pref;
     
    // Editor for Shared preferences
    Editor editor;
     
    // Context
    Context _context;
     
    // Shared pref mode
    int PRIVATE_MODE = 0;
     
    // Sharedpref file name
    private static final String PREF_NAME = "MendeleyPaperReaderPREF";
     
   
   
    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
    
   
    
    /**
     * Save login session
     * 
     * @param context
     * @param key
     * @param value
     * @param file_name
     */
    
    public void savePreferences( String key, String value) {

		editor.putString(key, value);
		editor.commit();

	}
    
    
    /**
     * Get shared preferences
     * 
     * @param context
     * @param Key
     * @param file_name
     * @return
     */
    public String LoadPreference(String Key) {

		return pref.getString(Key, "");

	}
    
    /**
     * delete shared preferences
     * 
     */
    public void deletePreferences(){
    	 // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
		
	}
    
   /**
    *  
    * @return
    */
    public boolean isLogged(){
	String access_token = LoadPreference("access_token");
	
	if(!access_token.isEmpty())
	    return true;
	    
		
	return false;
	
    }
    
    
   
}

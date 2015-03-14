package com.mendeleypaperreader.utl;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;

/**
 * Created by pedro on 29/12/14.
 */
public class GetDataBaseInformation {

    Context mContext;
    
    public GetDataBaseInformation(Context context){

        mContext = context;
        
    }


    public String getProfileInformation(String columnName) {

        String[] projection;
        String selection = null;

        projection = new String[]{columnName + " as _id"};
        Uri uri = MyContentProvider.CONTENT_URI_PROFILE;

        Cursor cursorProfiel = mContext.getContentResolver().query(uri, projection, selection, null, null);
        //cursorProfiel.moveToPosition(0);

        if( cursorProfiel != null && cursorProfiel.moveToFirst() ) {
            return cursorProfiel.getString(cursorProfiel.getColumnIndex(DatabaseOpenHelper._ID));
        }else{
            return "";
            
        }
        

    }

    public Cursor getFile() {
        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getFile - DOC_DETAILS");


        String[] projection = new String[]{DatabaseOpenHelper.FILE_ID + " as _id", DatabaseOpenHelper.FILE_NAME, DatabaseOpenHelper.FILE_MIME_TYPE, DatabaseOpenHelper.DOCUMENT_ID};

        String selection = null;
        Uri uri = MyContentProvider.CONTENT_URI_FILES;

        Cursor cursorFile = mContext.getContentResolver().query(uri, projection, selection, null, null);
        cursorFile.moveToPosition(0);


        return cursorFile;

    }
    
    
}

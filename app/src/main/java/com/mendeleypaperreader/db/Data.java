package com.mendeleypaperreader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mendeleypaperreader.parser.JSONParser;
import com.mendeleypaperreader.providers.ContentProvider;
import com.mendeleypaperreader.util.Globalconstant;

import java.util.HashMap;

/**
 * Created by pedro on 29/12/14.
 */
public class Data {

    Context mContext;

    private static final String TAG = "FragmentDetails";
    private static final boolean DEBUG = Globalconstant.DEBUG;


    public static String getProfileInformation(Context context, String columnName) {

        String[] projection;
        String selection = null;

        projection = new String[]{columnName + " as _id"};
        Uri uri = ContentProvider.CONTENT_URI_PROFILE;

        Cursor cursorProfiel = context.getContentResolver().query(uri, projection, selection, null, null);
        //cursorProfiel.moveToPosition(0);

        if (cursorProfiel != null && cursorProfiel.moveToFirst()) {
            return cursorProfiel.getString(cursorProfiel.getColumnIndex(DatabaseOpenHelper._ID));
        } else {
            return "";

        }


    }

    public static Cursor getFile(Context context) {
        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getFile - DOC_DETAILS");


        String[] projection = new String[]{DatabaseOpenHelper.FILE_ID + " as _id", DatabaseOpenHelper.FILE_NAME, DatabaseOpenHelper.FILE_MIME_TYPE, DatabaseOpenHelper.DOCUMENT_ID};

        String selection = null;
        Uri uri = ContentProvider.CONTENT_URI_FILES;

        Cursor cursorFile = context.getContentResolver().query(uri, projection, selection, null, null);
        cursorFile.moveToPosition(0);


        return cursorFile;

    }


    public static boolean isDocumentOnMyLibrary(String documentId, Context context) {

        String[] projection = new String[]{DatabaseOpenHelper._ID + " as _id"};

        String selection = DatabaseOpenHelper.GROUP_ID + " = '' and " + DatabaseOpenHelper._ID + " = '" + documentId + "' and " + DatabaseOpenHelper.TRASH + " = 'false'";
        Uri uri = ContentProvider.CONTENT_URI_DOC_DETAILS;

        Cursor cursorDocumentId = context.getContentResolver().query(uri, projection, selection, null, null);

        if (cursorDocumentId != null && cursorDocumentId.moveToFirst()) {
            return true;
        } else {
            return false;

        }

    }

    public static void insertRequest(Context context, String documentId, String method, String service) {

        //insert Sync request
        ContentValues values = new ContentValues();

        values.put(DatabaseOpenHelper.DOCUMENT_ID, documentId);
        values.put(DatabaseOpenHelper.METHOD, method);
        values.put(DatabaseOpenHelper.SERVICE, service);

        String url = service.replace("##", documentId);


        values.put(DatabaseOpenHelper.URL, url);

        Uri uri = context.getContentResolver().insert(ContentProvider.CONTENT_URI_SYNC_REQUEST, values);


    }


    public static Cursor getSynRequests(Context context) {

        String[] projection = null;  //new String[]{DatabaseOpenHelper.FILE_ID + " as _id", DatabaseOpenHelper.FILE_NAME, DatabaseOpenHelper.FILE_MIME_TYPE, DatabaseOpenHelper.DOCUMENT_ID};

        String selection = null;
        Uri uri = ContentProvider.CONTENT_URI_SYNC_REQUEST;

        Cursor cSyncRequest = context.getContentResolver().query(uri, projection, selection, null, null);


        return cSyncRequest;
    }

    public static Cursor getDocIdInTrash(Context context) {

        String[] projection = new String[]{DatabaseOpenHelper._ID};

        String selection = DatabaseOpenHelper.TRASH + " = 'true'";
        ;
        Uri uri = ContentProvider.CONTENT_URI_DOC_DETAILS;

        Cursor cTrashDocId = context.getContentResolver().query(uri, projection, selection, null, null);


        return cTrashDocId;
    }


    public static HashMap<String, String> getAllDocumentDetailsId(Context context) {

        String[] projection = new String[]{DatabaseOpenHelper._ID + " as _id", DatabaseOpenHelper.LAST_MODIFIED};

        String selection = null;//DatabaseOpenHelper.TRASH + " = 'false'";
        Uri uri = ContentProvider.CONTENT_URI_DOC_DETAILS;

        Cursor c_docId = context.getContentResolver().query(uri, projection, selection, null, null);

        HashMap<String, String> names = new HashMap<String, String>();


        if (c_docId != null) {

            if (c_docId.moveToFirst()) {
                do {

                    String docId = c_docId.getString(c_docId.getColumnIndexOrThrow("_id"));
                    String lastModifiedDate = c_docId.getString(c_docId.getColumnIndexOrThrow(DatabaseOpenHelper.LAST_MODIFIED));
                    names.put(docId, lastModifiedDate);


                } while (c_docId.moveToNext());
            }
            c_docId.close();
        }


        return names;
    }

    public static HashMap<String, String> getAllTrashDocumentDetailsId(Context context) {

        String[] projection = new String[]{DatabaseOpenHelper._ID + " as _id", DatabaseOpenHelper.LAST_MODIFIED};

        String selection = null; //DatabaseOpenHelper.TRASH + " = 'true'";
        Uri uri = ContentProvider.CONTENT_URI_DOC_DETAILS;

        Cursor c_docId = context.getContentResolver().query(uri, projection, selection, null, null);

        HashMap<String, String> names = new HashMap<String, String>();


        if (c_docId != null) {

            if (c_docId.moveToFirst()) {
                do {

                    String docId = c_docId.getString(c_docId.getColumnIndexOrThrow("_id"));
                    String lastModifiedDate = c_docId.getString(c_docId.getColumnIndexOrThrow(DatabaseOpenHelper.LAST_MODIFIED));
                    names.put(docId, lastModifiedDate);


                } while (c_docId.moveToNext());
            }
            c_docId.close();
        }


        return names;
    }


    public static void deleteDocumentByDoId(Context context, String documentId) {

        Uri uri_ = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");
        String selection = DatabaseOpenHelper._ID + " = '' and " + DatabaseOpenHelper._ID + " = '" + documentId + "'";


        context.getContentResolver().delete(uri_, selection, null);

    }


    public static Cursor getDocumentsIdmodifiedFromdate(Context context) {

        String[] projection = new String[]{DatabaseOpenHelper._ID + " as _id"};

        String selection = DatabaseOpenHelper.LAST_MODIFIED + " >= datetime('now', '-1 hours')";
        Uri uri = ContentProvider.CONTENT_URI_DOC_DETAILS;

        return context.getContentResolver().query(uri, projection, selection, null, null);


    }

    public static void deleteSyncRequests(Context context) {

        context.getContentResolver().delete(ContentProvider.CONTENT_URI_SYNC_REQUEST, null, null);

    }


    public static void deleteTrashDocumentById(Context context, String docId) {
        String where = DatabaseOpenHelper.TRASH + " = 'true' and " + DatabaseOpenHelper._ID + " = '" + docId + "'";
        Uri uri_ = Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

        context.getContentResolver().delete(uri_, where, null);
        Data.insertRequest(context.getApplicationContext(), docId, JSONParser.DELETE, Globalconstant.post_delete_document_from_trash);
    }


    public static void sendDocumentToTrash(Context context, String documentID) {
        Data.insertRequest(context.getApplicationContext(), documentID, JSONParser.POST, Globalconstant.post_move_document_to_trash);
        //update this document
        String where = DatabaseOpenHelper._ID + " = '" + documentID + "'";
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.TRASH, "true");
        context.getApplicationContext().getContentResolver().update(Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id"), values, where, null);
    }

    public static void deleteDocumentFromTrash(Context context, String documentID) {
        Data.insertRequest(context.getApplicationContext(), documentID, JSONParser.DELETE, Globalconstant.post_delete_document_from_trash);
        //update this document
        String where = DatabaseOpenHelper._ID + " = '" + documentID + "'";
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.TRASH, "trues");
        context.getApplicationContext().getContentResolver().update(Uri.parse(ContentProvider.CONTENT_URI_DOC_DETAILS + "/id"), values, where, null);


    }


}
